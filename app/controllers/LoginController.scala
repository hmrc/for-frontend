/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import connectors.Audit
import form.persistence.FormDocumentRepository
import form.{Errors, MappingSupport}
import models.*
import models.pages.SummaryBuilder
import models.serviceContracts.submissions.Address
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.*
import play.api.libs.json.{Format, Json}
import play.api.mvc.*
import config.LoginToHODAction
import security.{DocumentPreviouslySaved, NoExistingDocument}
import uk.gov.hmrc.http.HeaderNames.trueClientIp
import uk.gov.hmrc.http.{HeaderCarrier, SessionId, SessionKeys, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import util.DateUtil.nowInUK
import views.html.{login, loginFailed}

import java.time.{ZoneOffset, ZonedDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class LoginDetails(referenceNumber: String, postcode: String, startTime: ZonedDateTime)

object LoginController {

  val loginForm: Form[LoginDetails] = Form(
    mapping(
      // format of reference number should be 7 or 8 digits then / then 3 digits
      "referenceNumber" -> text.verifying(
        Errors.invalidRefNum,
        x => {
          val cleanRefNumber = x.replaceAll("\\D+", "")
          cleanRefNumber.length > 9 && cleanRefNumber.length < 12
        }
      ),
      "postcode"        -> text.verifying(
        Errors.invalidPostcodeOnLetter,
        pc => {
          var cleanPostcode = pc.replaceAll("[^\\w\\d]", "")
          cleanPostcode = cleanPostcode.patch(cleanPostcode.length - 3, " ", 0).toUpperCase
          cleanPostcode.matches(MappingSupport.postcodeRegex)
        }
      ),
      "start-time"      -> default(
        localDateTime("yyyy-MM-dd'T'HH:mm:ss.SSS")
          .transform[ZonedDateTime](_.atZone(ZoneOffset.UTC), _.toLocalDateTime),
        nowInUK
      )
    )(LoginDetails.apply)(o => Some(Tuple.fromProductTyped(o)))
  )
}

class LoginController @Inject() (
  audit: Audit,
  documentRepo: FormDocumentRepository,
  loginToHOD: LoginToHODAction,
  cc: MessagesControllerComponents,
  login: login,
  errorView: views.html.error.error,
  loginFailedView: loginFailed,
  lockedOutView: views.html.lockedOut
)(implicit ec: ExecutionContext
) extends FrontendController(cc)
  with Logging {

  import LoginController.loginForm

  def show: Action[AnyContent] = Action { implicit request =>
    Ok(login(loginForm.fill(LoginDetails("", "", nowInUK))))
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    val refNum                     = request.session.get("refNum").getOrElse("-")
    val refNumJson                 = Json.obj(Audit.referenceNumber -> refNum)
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match {
      case Some(sessionId) =>
        documentRepo.findById(sessionId.value, refNum).map {
          case Some(doc) => refNumJson ++ Addresses.addressJson(SummaryBuilder.build(doc))
          case None      => refNumJson
        }.map(jsObject => audit.sendExplicitAudit("Logout", jsObject))
      case None            =>
        audit.sendExplicitAudit("Logout", refNumJson)
    }

    Redirect(routes.LoginController.show).withNewSession
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(login(formWithErrors))),
      loginData => verifyLogin(loginData.referenceNumber, loginData.postcode, loginData.startTime)
    )
  }

  def verifyLogin(referenceNumber: String, postcode: String, startTime: ZonedDateTime)(implicit r: MessagesRequest[AnyContent]): Future[Result] = {
    val sessionId = java.util.UUID.randomUUID().toString // TODO - Why new session? Why manually?

    implicit val hc2: HeaderCarrier = hc.copy(sessionId = Some(SessionId(sessionId)))
    val cleanedRefNumber            = referenceNumber.replaceAll("[^0-9]", "")
    var cleanPostcode               = postcode.replaceAll("[^\\w\\d]", "")
    cleanPostcode = cleanPostcode.patch(cleanPostcode.length - 4, " ", 0)
    loginToHOD(using hc2, ec)(cleanedRefNumber, cleanPostcode, startTime).flatMap {
      case DocumentPreviouslySaved(token, address) =>
        auditLogin(cleanedRefNumber, returnUser = true, address)(using hc2)
        withNewSession(Redirect(routes.SaveForLaterController.login), token, cleanedRefNumber, sessionId)
      case NoExistingDocument(token, address)      =>
        auditLogin(cleanedRefNumber, returnUser = false, address)(using hc2)
        withNewSession(Redirect(dataCapturePages.routes.PageController.showPage(0)), token, cleanedRefNumber, sessionId)
    }.recover {
      case UpstreamErrorResponse(_, 409, _, _)    => Conflict(errorView(409))
      case UpstreamErrorResponse(_, 403, _, _)    => Conflict(errorView(403))
      case UpstreamErrorResponse(body, 401, _, _) =>
        val failed            = Json.parse(body).as[FailedLoginResponse]
        val remainingAttempts = failed.numberOfRemainingTriesUntilIPLockout
        logger.warn(s"Failed login: RefNum: $cleanedRefNumber Attempts remaining: $remainingAttempts")
        if (remainingAttempts < 1) {
          val clientIP = r.headers.get(trueClientIp).getOrElse("")
          auditLockedOut(cleanedRefNumber, postcode, cleanPostcode, clientIP)(using hc2)

          Redirect(routes.LoginController.lockedOut)
        } else {
          Redirect(routes.LoginController.loginFailed(remainingAttempts))
        }
    }
  }

  private def auditLogin(refNumber: String, returnUser: Boolean, address: Address)(implicit hc: HeaderCarrier): Unit = {
    val json = Json.obj("returningUser" -> returnUser, Audit.referenceNumber -> refNumber, Audit.address -> Json.toJsObject(address))
    audit.sendExplicitAudit("UserLogin", json)
  }

  private def auditLockedOut(refNumber: String, postcode: String, postcodeCleaned: String, lockedIP: String)(implicit hc: HeaderCarrier): Unit = {
    val detailJson = Json.obj(
      Audit.referenceNumber -> refNumber,
      "postcode"            -> postcode,
      "postcodeCleaned"     -> postcodeCleaned,
      "lockedIP"            -> lockedIP
    )
    audit.sendExplicitAudit("LockedOut", detailJson)
  }

  def lockedOut: Action[AnyContent] = Action { implicit request =>
    Unauthorized(lockedOutView())
  }

  def loginFailed(attemptsRemaining: Int): Action[AnyContent] = Action { implicit request =>
    Unauthorized(loginFailedView(attemptsRemaining))
  }

  private def withNewSession(r: Result, token: String, ref: String, sessionId: String)(implicit req: Request[AnyContent]) =
    r.withSession(
      (req.session.data ++ Seq(SessionKeys.sessionId -> sessionId, SessionKeys.authToken -> token, "refNum" -> ref)).toSeq*
    )
}

object FailedLoginResponse {
  implicit val f: Format[FailedLoginResponse] = Json.format[FailedLoginResponse]
}

case class FailedLoginResponse(numberOfRemainingTriesUntilIPLockout: Int)
