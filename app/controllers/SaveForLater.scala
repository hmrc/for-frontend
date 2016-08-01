/*
 * Copyright 2016 HM Revenue & Customs
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
import connectors.HODConnector
import controllers.dataCapturePages.{RedirectTo, UrlFor}
import form.persistence.FormDocumentRepository
import models.journeys._
import models.pages.{Summary, SummaryBuilder}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AnyContent, Result}
import playconfig.{Audit, FormPersistence, SessionId}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases.{IncorrectPassword, PasswordsMatch, ReferenceNumber, SaveForLaterPassword}

import scala.concurrent.Future

object SaveForLater extends FrontendController {
  lazy val s4l: SaveInProgressSubmissionForLater = playconfig.SaveForLater()

  def continue(implicit hc: HeaderCarrier): ContinueWithSavedSubmission = playconfig.ContinueWithSavedSubmission(hc)

  lazy val repository: FormDocumentRepository = FormPersistence.formDocumentRepository
  val s4lIndicator = "s4l"

  def saveForLater = RefNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
      case Some(doc) =>
        s4l(hc)(doc, hc).flatMap { pw =>
          val sum = SummaryBuilder.build(doc)
          audit(sum, pw)
          val expiryDate = LocalDate.now.plusDays(90)
          val email = sum.customerDetails.flatMap(_.contactDetails.email)
            HODConnector.sendEmail(sum.referenceNumber, sum.addressVOABelievesIsCorrect.postcode, sum.customerDetails.flatMap(_.contactDetails.email)) map {
              _ =>
              Ok(views.html.savedForLater(sum, pw, expiryDate))
            }
        }
      case None =>
        InternalServerError(views.html.error.error500())
    }
  }

  def audit(sum: Summary, pw: SaveForLaterPassword) = Audit(
    "SavedForLater", Map(
      "referenceNumber" -> sum.referenceNumber, "name" -> sum.submitter
    )
  )

  def resumeOptions = RefNumAction.async { implicit request =>
    Ok(views.html.saveForLaterResumeOptions())
  }

  def login = RefNumAction.async { implicit request =>
    Ok(views.html.saveForLaterLogin())
  }

  def resume = RefNumAction.async { implicit request =>
    saveForLaterForm.bindFromRequest.fold(
      error => BadRequest(views.html.saveForLaterLoginFailed()),
      s4l => resumeSavedJourney(s4l.password, request.refNum)
    )
  }

  def immediateResume = RefNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum).flatMap {
      case Some(doc) =>
        val sum = SummaryBuilder.build(doc)
        Redirect(UrlFor(Journey.pageToResumeAt(sum), request.headers)).flashing((s4lIndicator, s4lIndicator))
      case None =>
        InternalServerError
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
