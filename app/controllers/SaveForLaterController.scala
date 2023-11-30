/*
 * Copyright 2023 HM Revenue & Customs
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
import views.html.{customPasswordSaveForLater, saveForLaterLogin, saveForLaterLoginFailed, savedForLater}
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import playconfig.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases.{ErrorRetrievingSavedDocument, IncorrectPassword, PasswordsMatch}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

object SaveForLaterController {
  val s4lIndicator = "s4l"
}

@Singleton
class SaveForLaterController @Inject()
(cc: MessagesControllerComponents,
  audit: Audit, refNumAction: RefNumAction,
  emailConnector: EmailConnector, config: Configuration,
  saveForLaterLogin: saveForLaterLogin,
  saveForLaterLoginFailed: saveForLaterLoginFailed,
  savedForLater:savedForLater,
  customPasswordSaveForLaterView: customPasswordSaveForLater,
 errorView: views.html.error.error
)
(implicit ec: ExecutionContext, hodConnector: HODConnector, repository: FormDocumentRepository) extends FrontendController(cc) {

  import SaveForLaterController._

  val expiryDateInDays = config.get[String]("savedForLaterExpiryDays").toInt

  lazy val s4l: SaveInProgressSubmissionForLater = playconfig.SaveForLater()

  def continue(implicit hc: HeaderCarrier): ContinueWithSavedSubmission = playconfig.ContinueWithSavedSubmission()

  def saveForLater(exitPath: String) = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
      case Some(doc) => {
        val sum = SummaryBuilder.build(doc)
        val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          if (doc.saveForLaterPassword.isDefined) {
            val saveSubmissionForLater = playconfig.SaveForLater(doc.saveForLaterPassword.get)
            saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
              audit.sendSavedForLater(sum, exitPath)
              val email = sum.customerDetails.map(_.contactDetails.email)
              emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)

              Ok(savedForLater(sum, pw, expiryDate))
            }
          } else {
            Ok(customPasswordSaveForLaterView(sum, expiryDate, CustomUserPasswordForm.customUserPassword, exitPath)) //TODO - pass path
          }
      }
      case None =>
        InternalServerError(errorView(500))
    }
  }

  def customPasswordSaveForLater(exitPath: String) = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
        case Some(doc) => {
          val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          val sum = SummaryBuilder.build(doc)
          CustomUserPasswordForm.customUserPassword.bindFromRequest().fold(
            formErrors => {
              Ok(customPasswordSaveForLaterView(sum, expiryDate, formErrors, exitPath))
            },
            validData => {
              val saveSubmissionForLater = playconfig.SaveForLater(validData.password)
              saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
                audit.sendSavedForLater(sum, exitPath)
                val email = sum.customerDetails.map(_.contactDetails.email)
                emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)

                Ok(savedForLater(sum, pw, expiryDate))

              }
            }
          )
        }
        case None =>
          InternalServerError(errorView(500))
      }
  }

  def login = refNumAction.async { implicit request =>
    Ok(saveForLaterLogin(saveForLaterForm))
  }

  def resume = refNumAction.async { implicit request =>
    saveForLaterForm.bindFromRequest().fold(
      formWithErrors => BadRequest(saveForLaterLogin(formWithErrors)),
      s4l => continue(hc)(s4l.password, request.refNum) flatMap {
        case PasswordsMatch(pageToResumeAt) => RedirectTo(pageToResumeAt, request.headers).flashing((s4lIndicator, s4lIndicator))
        case IncorrectPassword =>
          val formWithLoginErrors = saveForLaterForm.withError("password", Messages("saveForLater.invalidPassword"))
          BadRequest(saveForLaterLogin(formWithLoginErrors))
        case ErrorRetrievingSavedDocument => Redirect(controllers.routes.SaveForLaterController.login)
      }
    )
  }

  def immediateResume = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
      case Some(doc) =>
        val sum = SummaryBuilder.build(doc)
        Redirect(UrlFor(Journey.pageToResumeAt(sum), request.headers)).flashing((s4lIndicator, s4lIndicator))
      case None =>
        InternalServerError
    }
  }

  def timeout = refNumAction.async { implicit request =>
    repository.findById(playconfig.SessionId(hc), request.refNum).flatMap {
      case Some(doc) =>
        val saveSubmissionForLater = doc.saveForLaterPassword.fold(playconfig.SaveForLater())(playconfig.SaveForLater(_))
        saveSubmissionForLater(hc)(doc, hc).flatMap { pw =>
          val sum = SummaryBuilder.build(doc)
          val json = Json.obj(Audit.referenceNumber -> sum.referenceNumber) ++ Addresses.addressJson(sum)
          audit.sendExplicitAudit("UserTimeout", json)
          val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          val email = sum.customerDetails.map(_.contactDetails.email)

          emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate)
          Ok(savedForLater(sum, pw, expiryDate, hasTimedOut = true))
        }
      case None => Redirect(routes.LoginController.logout)
    }
  }

  lazy val saveForLaterForm = Form(mapping(
    "password" -> nonEmptyText
  )(SaveForLaterLogin.apply)(SaveForLaterLogin.unapply))

}

case class SaveForLaterLogin(password: String)
