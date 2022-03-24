/*
 * Copyright 2022 HM Revenue & Customs
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
import models.Addresses
import models.pages.SummaryBuilder
import models.serviceContracts.submissions.Address

import javax.inject.Inject
import org.joda.time.DateTime
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.JodaForms._
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import playconfig.LoginToHODAction
import security.{DocumentPreviouslySaved, NoExistingDocument}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId, SessionKeys, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.{login, loginFailed}

import scala.concurrent.{ExecutionContext, Future}

case class LoginDetails(referenceNumber: String, postcode: String, startTime: DateTime)

object LoginController {
  val loginForm = Form(
    mapping(
      //format of reference number should be 7 or 8 digits then / then 3 digits
      "referenceNumber" -> text.verifying(Errors.invalidRefNum, x => {
        val cleanRefNumber = x.replaceAll("\\D+", "")
        val validLength = cleanRefNumber.length > 9 && cleanRefNumber.length < 12: Boolean
        validLength
      }),
      "postcode" -> text.verifying(Errors.invalidPostcodeOnLetter, pc => {
        var cleanPostcode = pc.replaceAll("[^\\w\\d]", "")
        cleanPostcode = cleanPostcode.patch(cleanPostcode.length - 3, " ", 0).toUpperCase
        val isValid = cleanPostcode.matches(MappingSupport.postcodeRegex): Boolean
        isValid
      }),
      "start-time" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    )(LoginDetails.apply)(LoginDetails.unapply))
}


class LoginController @Inject()(
  audit: Audit,
  documentRepo: FormDocumentRepository,
  loginToHOD: LoginToHODAction,
  cc: MessagesControllerComponents,
  login: login, 
  errorView: views.html.error.error,
  loginFailedView: loginFailed,
  lockedOutView: views.html.lockedOut
)
(implicit ec: ExecutionContext) extends FrontendController(cc) {

  import LoginController.loginForm


  def show = Action { implicit request =>
    Ok(login(loginForm))
  }

  def logout = Action { implicit request =>
    val refNum = request.session.get("refNum").getOrElse("-")
    val refNumJson = Json.obj(Audit.referenceNumber -> refNum)
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId.map(sessionId =>
      documentRepo.findById(sessionId.value, refNum).flatMap {
        case Some(doc) => refNumJson ++ Addresses.addressJson(SummaryBuilder.build(doc))
        case None => refNumJson
      }.map(jsObject => audit.sendExplicitAudit("Logout", jsObject))
    ).getOrElse {
      audit.sendExplicitAudit("Logout", refNumJson)
    }

    Redirect(routes.LoginController.show).withNewSession
  }

  def submit = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(login(formWithErrors))),
      loginData => verifyLogin(loginData.referenceNumber, loginData.postcode, loginData.startTime)
    )
  }

  def verifyLogin(referenceNumber: String, postcode: String, startTime: DateTime)(implicit r: MessagesRequest[AnyContent]) = {
    val sessionId = java.util.UUID.randomUUID().toString //TODO - Why new session? Why manually?

    implicit val hc2: HeaderCarrier = hc.copy(sessionId = Some(SessionId(sessionId)))
    val cleanedRefNumber = referenceNumber.replaceAll("[^0-9]", "")
    val (ref1, ref2) = cleanedRefNumber.splitAt(cleanedRefNumber.length - 3)
    var cleanPostcode = postcode.replaceAll("[^\\w\\d]", "")
    cleanPostcode = cleanPostcode.patch(cleanPostcode.length - 4, " ", 0)
    //TODO - refactor
    loginToHOD(hc2, ec)(ref1, ref2, cleanPostcode, startTime).flatMap {
      case DocumentPreviouslySaved(doc, token, address) =>
        auditLogin(ref1 + ref2, true, address)(hc2)
        withNewSession(Redirect(routes.SaveForLaterController.login), token, s"$ref1$ref2", sessionId)
      case NoExistingDocument(token, address) =>
        auditLogin(ref1 + ref2, false, address)(hc2)
        withNewSession(Redirect(dataCapturePages.routes.PageController.showPage(0)), token, s"$ref1$ref2", sessionId)
    }.recover {
      case Upstream4xxResponse(_, 409, _, _) => Conflict(errorView(409))
      case Upstream4xxResponse(_, 403, _, _) => Conflict(errorView(403))
      case Upstream4xxResponse(body, 401, _, _) =>
        val failed = Json.parse(body).as[FailedLoginResponse]
        Logger.info(s"Failed login: RefNum: $ref1$ref2 Attempts remaining: ${failed.numberOfRemainingTriesUntilIPLockout}")
        if (failed.numberOfRemainingTriesUntilIPLockout < 1)
          Redirect(routes.LoginController.lockedOut)
        else
          Redirect(routes.LoginController.loginFailed(failed.numberOfRemainingTriesUntilIPLockout))
    }
  }

  private def auditLogin(refNumber: String, returnUser: Boolean, address: Address)(implicit hc: HeaderCarrier): Unit = {
    val json = Json.obj("returningUser" -> returnUser, Audit.referenceNumber -> refNumber, Audit.address -> address)
    audit.sendExplicitAudit("UserLogin", json)
  }

  def lockedOut = Action { implicit request =>
    Unauthorized(lockedOutView())
  }

  def loginFailed(attemptsRemaining: Int) = Action { implicit request =>
    Unauthorized(loginFailedView(attemptsRemaining))
  }

  private def withNewSession(r: Result, token: String, ref: String, sessionId: String)(implicit req: Request[AnyContent]) = {
    r.withSession(
      (req.session.data ++ Seq(SessionKeys.sessionId -> sessionId, SessionKeys.authToken -> token, "refNum" -> ref)).toSeq: _*
    )
  }
}

object FailedLoginResponse {
  implicit val f: Format[FailedLoginResponse] = Json.format[FailedLoginResponse]
}

case class FailedLoginResponse(numberOfRemainingTriesUntilIPLockout: Int)
