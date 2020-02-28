/*
 * Copyright 2020 HM Revenue & Customs
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

import actions.{RefNumAction, RefNumRequest}
import connectors.{Audit, EmailConnector}
import controllers.dataCapturePages.{RedirectTo, UrlFor}
import form.CustomUserPasswordForm
import form.persistence.FormDocumentRepository
import javax.inject.{Inject, Singleton}
import models.journeys._
import models.pages.{Summary, SummaryBuilder}
import org.joda.time.LocalDate
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import playconfig.{FormPersistence, SessionId}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases.{IncorrectPassword, PasswordsMatch, ReferenceNumber, SaveForLaterPassword}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaveForLater @Inject()(cc: MessagesControllerComponents, audit: Audit, refNumAction: RefNumAction,
                             emailConnector: EmailConnector, config: Configuration)(implicit ec: ExecutionContext) extends FrontendController(cc) {
  val expiryDateInDays = config.get[String]("savedForLaterExpiryDays").toInt

  lazy val s4l: SaveInProgressSubmissionForLater = playconfig.SaveForLater()

  def continue(implicit hc: HeaderCarrier): ContinueWithSavedSubmission = playconfig.ContinueWithSavedSubmission(hc)

  lazy val repository: FormDocumentRepository = FormPersistence.formDocumentRepository
  val s4lIndicator = "s4l"

  def saveForLater = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
      case Some(doc) => {
        val sum = SummaryBuilder.build(doc)
        val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          if (doc.saveForLaterPassword.isDefined) {
            playconfig.SaveForLater(doc.saveForLaterPassword.get)(hc)(doc, hc).flatMap { pw =>
              audit(sum)
              val email = sum.customerDetails.flatMap(_.contactDetails.email)
              emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate) map { _ =>
                Ok(views.html.savedForLater(sum, pw, expiryDate))
              }
            }
          } else {
            Ok(views.html.customPasswordSaveForLater(sum, expiryDate, CustomUserPasswordForm.customUserPassword))
          }
      }
      case None =>
        InternalServerError(views.html.error.error500())
    }
  }

  def customPasswordSaveForLater = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
        case Some(doc) => {
          val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          val sum = SummaryBuilder.build(doc)
          CustomUserPasswordForm.customUserPassword.bindFromRequest.fold(
            formErrors => {
              Ok(views.html.customPasswordSaveForLater(sum, expiryDate, formErrors))
            },
            validData => {
              playconfig.SaveForLater(validData.password)(hc)(doc, hc).flatMap { pw =>
                audit(sum)
                val email = sum.customerDetails.flatMap(_.contactDetails.email)
                emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate) map { _ =>
                  Ok(views.html.savedForLater(sum, pw, expiryDate))
                }
              }
            }
          )
        }
        case None =>
          InternalServerError(views.html.error.error500())
      }
  }

  def audit(sum: Summary)(implicit headerCarrier: HeaderCarrier) = audit(
    "SavedForLater", Map(
      Audit.referenceNumber -> sum.referenceNumber, "name" -> sum.submitter
    )
  )

  def resumeOptions = refNumAction.async { implicit request =>
    Ok(views.html.saveForLaterResumeOptions())
  }

  def login = refNumAction.async { implicit request =>
    Ok(views.html.saveForLaterLogin())
  }

  def resume = refNumAction.async { implicit request =>
    saveForLaterForm.bindFromRequest.fold(
      error => BadRequest(views.html.saveForLaterLoginFailed()),
      s4l => resumeSavedJourney(s4l.password, request.refNum)
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
      case Some(doc) => {
        val save4laterResponse = (if (doc.saveForLaterPassword.isDefined) {
          playconfig.SaveForLater(doc.saveForLaterPassword.get)(hc)(doc, hc)
        } else {
          playconfig.SaveForLater()(hc)(doc, hc)
        })
        save4laterResponse.flatMap { pw =>
          val sum = SummaryBuilder.build(doc)
          audit.sendExplicitAudit("UserTimeout", Json.obj(
            Audit.referenceNumber -> sum.referenceNumber))
          val expiryDate = LocalDate.now.plusDays(expiryDateInDays)
          val email = sum.customerDetails.flatMap(_.contactDetails.email)
          emailConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, email, expiryDate) map { _ =>
            Ok(views.html.savedForLater(sum, pw, expiryDate, hasTimedout = true))
          }
        }
      }
      case None =>
        InternalServerError(views.html.error.error500())
    }
  }

  private def resumeSavedJourney(p: SaveForLaterPassword, r: ReferenceNumber)(implicit re: RefNumRequest[AnyContent]): Future[Result] = {
    continue(hc)(p, r) flatMap {
      case PasswordsMatch(pageToResumeAt) => RedirectTo(pageToResumeAt, re.headers).flashing((s4lIndicator, s4lIndicator))
      case IncorrectPassword => BadRequest(views.html.saveForLaterLoginFailed())
    }
  }

  lazy val saveForLaterForm = Form(mapping(
    "password" -> nonEmptyText
  )(SaveForLaterLogin.apply)(SaveForLaterLogin.unapply))

  case class SaveForLaterLogin(password: String)

}
