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

import actions.RefNumAction
import connectors.{Audit, EmailConnector, HODConnector}
import controllers.dataCapturePages.{RedirectTo, UrlFor}
import crypto.MongoHasher
import form.CustomUserPasswordForm
import form.persistence.FormDocumentRepository
import models.Addresses

import javax.inject.{Inject, Singleton}
import models.journeys._
import models.pages.SummaryBuilder
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import views.html.{customPasswordSaveForLater, saveForLaterLogin, savedForLater}
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import config.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.{ErrorRetrievingSavedDocument, IncorrectPassword, PasswordsMatch}

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import play.api.mvc
import play.api.mvc.AnyContent

object SaveForLaterController {
  val s4lIndicator = "s4l"
}

@Singleton
class SaveForLaterController @Inject() (
  cc: MessagesControllerComponents,
  audit: Audit,
  refNumAction: RefNumAction,
  emailConnector: EmailConnector,
  configuration: Configuration,
  saveForLaterLogin: saveForLaterLogin,
  savedForLater: savedForLater,
  customPasswordSaveForLaterView: customPasswordSaveForLater,
  errorView: views.html.error.error
)(implicit ec: ExecutionContext,
  hodConnector: HODConnector,
  repository: FormDocumentRepository,
  mongoHasher: MongoHasher
) extends FrontendController(cc) {

  import SaveForLaterController._

  private val expiryDateInDays = configuration.get[String]("savedForLaterExpiryDays").toInt

  def continue(implicit hc: HeaderCarrier): ContinueWithSavedSubmission = config.ContinueWithSavedSubmission()

  def saveForLater(exitPath: String): mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).flatMap {
      case Some(doc) =>
        val sum        = SummaryBuilder.build(doc)
        val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
        if doc.saveForLaterPassword.isDefined then
          val saveSubmissionForLater = config.SaveForLater(doc.saveForLaterPassword.get)
          saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
            audit.sendSavedForLater(sum, exitPath)
            val email = sum.customerDetails.map(_.contactDetails.email)
            emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)

            Ok(savedForLater(sum, pw, expiryDate))
          }
        else
          Ok(customPasswordSaveForLaterView(sum, expiryDate, CustomUserPasswordForm.customUserPassword, exitPath)) // TODO - pass path
      case None      =>
        InternalServerError(errorView(500))
    }
  }

  def customPasswordSaveForLater(exitPath: String): mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).flatMap {
      case Some(doc) =>
        val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
        val sum        = SummaryBuilder.build(doc)
        CustomUserPasswordForm.customUserPassword.bindFromRequest().fold(
          formErrors =>
            Ok(customPasswordSaveForLaterView(sum, expiryDate, formErrors, exitPath)),
          validData => {
            val saveSubmissionForLater = config.SaveForLater(validData.password)
            saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
              audit.sendSavedForLater(sum, exitPath)
              val email = sum.customerDetails.map(_.contactDetails.email)
              emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)

              Ok(savedForLater(sum, pw, expiryDate))

            }
          }
        )
      case None      =>
        InternalServerError(errorView(500))
    }
  }

  def login: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    Ok(saveForLaterLogin(saveForLaterForm))
  }

  def resume: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    saveForLaterForm.bindFromRequest().fold(
      formWithErrors => BadRequest(saveForLaterLogin(formWithErrors)),
      s4l =>
        continue(using hc)(s4l.password, request.refNum) flatMap {
          case PasswordsMatch(pageToResumeAt) => RedirectTo(pageToResumeAt, request.headers).flashing((s4lIndicator, s4lIndicator))
          case IncorrectPassword              =>
            val formWithLoginErrors = saveForLaterForm.withError("password", Messages("saveForLater.invalidPassword"))
            BadRequest(saveForLaterLogin(formWithLoginErrors))
          case ErrorRetrievingSavedDocument   => Redirect(controllers.routes.SaveForLaterController.login)
        }
    )
  }

  def immediateResume: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).flatMap {
      case Some(doc) =>
        val sum = SummaryBuilder.build(doc)
        Redirect(UrlFor(Journey.pageToResumeAt(sum), request.headers)).flashing((s4lIndicator, s4lIndicator))
      case None      =>
        InternalServerError
    }
  }

  def timeout: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(config.SessionId(using hc), request.refNum).flatMap {
      case Some(doc) =>
        val saveSubmissionForLater = doc.saveForLaterPassword.fold(config.SaveForLater())(config.SaveForLater(_))
        saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
          val sum        = SummaryBuilder.build(doc)
          val json       = Json.obj(Audit.referenceNumber -> sum.referenceNumber) ++ Addresses.addressJson(sum)
          audit.sendExplicitAudit("UserTimeout", json)
          val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          val email      = sum.customerDetails.map(_.contactDetails.email)

          emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)
          Ok(savedForLater(sum, pw, expiryDate, hasTimedOut = true))
        }
      case None      => Redirect(routes.LoginController.logout)
    }
  }

  private val saveForLaterForm: Form[SaveForLaterLogin] = Form(
    mapping(
      "password" -> nonEmptyText
    )(SaveForLaterLogin.apply)(o => Some(o.password))
  )

}

case class SaveForLaterLogin(password: String)
