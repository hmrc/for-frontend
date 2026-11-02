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

import config.LoginToHODAction
import form.persistence.FormDocumentRepository
import form.{Errors, MappingSupport}
import jakarta.inject.Inject
import models.*
import models.event.{LockedOut, Logout, UserLogin}
import models.pages.{Summary, SummaryBuilder}
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.*
import play.api.libs.json.{Format, Json}
import play.api.mvc.*
import security.{DocumentPreviouslySaved, NoExistingDocument}
import uk.gov.hmrc.http.HeaderNames.trueClientIp
import uk.gov.hmrc.http.{HeaderCarrier, SessionId, SessionKeys, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.vo.service.connectors.AuditService
import uk.gov.hmrc.vo.service.conversion.DateConversion.*
import views.html.{login, loginFailed}

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

case class LoginDetails(referenceNumber: String, postcode: String, startTime: ZonedDateTime)

object LoginController:

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

class LoginController @Inject() (
  audit: AuditService,
  documentRepo: FormDocumentRepository,
  loginToHOD: LoginToHODAction,
  cc: MessagesControllerComponents,
  login: login,
  errorView: views.html.error.error,
  loginFailedView: loginFailed,
  lockedOutView: views.html.lockedOut
)(using ec: ExecutionContext
) extends FrontendController(cc)
  with Logging:

  import LoginController.loginForm

  def show: Action[AnyContent] = Action { implicit request =>
    Ok(login(loginForm.fill(LoginDetails("", "", nowInUK))))
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    val refNum = request.session.get("refNum").getOrElse("-")

    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match
      case Some(sessionId) =>
        documentRepo.findById(sessionId.value, refNum).map {
          case Some(doc) => auditSendLogout(refNum, SummaryBuilder.build(doc))
          case None      => auditSendLogout(refNum, None)
        }
      case None            => auditSendLogout(refNum, None)

    Redirect(routes.LoginController.show).withNewSession
  }

  private def auditSendLogout(referenceNumber: String, summary: Option[Summary])(using hc: HeaderCarrier): Unit =
    audit.send(
      Logout(
        referenceNumber = referenceNumber,
        address = summary.flatMap(_.address),
        updatedAddress = summary.flatMap(Addresses.updatedAddress)
      )
    )

  def submit: Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(login(formWithErrors))),
      loginData => verifyLogin(loginData.referenceNumber, loginData.postcode, loginData.startTime)
    )
  }

  def verifyLogin(referenceNumber: String, postcode: String, startTime: ZonedDateTime)(using r: MessagesRequest[AnyContent]): Future[Result] =
    val sessionId = UUID.randomUUID().toString // TODO - Why new session? Why manually?

    given hc2: HeaderCarrier = hc.copy(sessionId = Some(SessionId(sessionId)))

    val cleanedRefNumber = referenceNumber.replaceAll("[^0-9]", "")
    var cleanPostcode    = postcode.replaceAll("[^\\w\\d]", "")
    cleanPostcode = cleanPostcode.patch(cleanPostcode.length - 4, " ", 0)

    loginToHOD(using hc2, ec)(cleanedRefNumber, cleanPostcode, startTime).flatMap {
      case DocumentPreviouslySaved(token, address) =>
        audit.send(UserLogin(returningUser = true, cleanedRefNumber, address))
        withNewSession(Redirect(routes.SaveForLaterController.login), token, cleanedRefNumber, sessionId)
      case NoExistingDocument(token, address)      =>
        audit.send(UserLogin(returningUser = false, cleanedRefNumber, address))
        withNewSession(Redirect(dataCapturePages.routes.PageController.showPage(0)), token, cleanedRefNumber, sessionId)
    }.recover {
      case UpstreamErrorResponse(_, 409, _, _)    => Conflict(errorView(409))
      case UpstreamErrorResponse(_, 403, _, _)    => Conflict(errorView(403))
      case UpstreamErrorResponse(body, 401, _, _) =>
        val failed            = Json.parse(body).as[FailedLoginResponse]
        val remainingAttempts = failed.numberOfRemainingTriesUntilIPLockout
        logger.warn(s"Failed login: RefNum: $cleanedRefNumber Attempts remaining: $remainingAttempts")
        if remainingAttempts < 1 then
          audit.send(
            LockedOut(
              referenceNumber = cleanedRefNumber,
              postcode = postcode,
              postcodeCleaned = cleanPostcode,
              lockedIP = r.headers.get(trueClientIp).getOrElse("")
            )
          )

          Redirect(routes.LoginController.lockedOut)
        else
          Redirect(routes.LoginController.loginFailed(remainingAttempts))
    }

  def lockedOut: Action[AnyContent] = Action { implicit request =>
    Unauthorized(lockedOutView())
  }

  def loginFailed(attemptsRemaining: Int): Action[AnyContent] = Action { implicit request =>
    Unauthorized(loginFailedView(attemptsRemaining))
  }

  private def withNewSession(r: Result, token: String, ref: String, sessionId: String)(using req: Request[AnyContent]) =
    r.withSession(
      (req.session.data ++ Seq(SessionKeys.sessionId -> sessionId, SessionKeys.authToken -> token, "refNum" -> ref)).toSeq*
    )

object FailedLoginResponse:
  implicit val f: Format[FailedLoginResponse] = Json.format

case class FailedLoginResponse(numberOfRemainingTriesUntilIPLockout: Int)
