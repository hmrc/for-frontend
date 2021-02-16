/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.feedback

import actions.{RefNumAction, RefNumRequest}
import connectors.Audit
import form.Formats._
import form.persistence.FormDocumentRepository

import javax.inject.{Inject, Singleton}
import models.pages.SummaryBuilder
import models.{Journey, NormalJourney, Satisfaction, contactDetailsFormat}
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.mvc.{AnyContent, MessagesControllerComponents, MessagesRequestHeader, Request, RequestHeader}
import playconfig.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

object Survey {
  val completedFeedbackForm = Form(mapping(
    "satisfaction" -> Forms.of[Satisfaction],
    "details" -> text(maxLength = 1200),
    "journey" -> Forms.of[Journey]
  )(SurveyFeedback.apply)(SurveyFeedback.unapply))

}


@Singleton
class SurveyController @Inject() (
                                   cc: MessagesControllerComponents,
                                   repository: FormDocumentRepository,
                                   refNumAction: RefNumAction,
                                   audit: Audit,
                                   confirmationView: views.html.confirm,
                                   confirmationViewB: views.html.confirmB,
                                   confirmationViewC: views.html.confirmC,
                                   errorView: views.html.error.error,
                                   feedbackThxView: views.html.feedbackThx,
                                   surveyView: views.html.survey
                                 )(implicit ec: ExecutionContext) extends FrontendController(cc) {
  import Survey._

  val completedFeedbackFormNormalJourney = completedFeedbackForm.bind(Map("journey" -> NormalJourney.name)).discardingErrors

  def onPageView(journey: String) = refNumAction { implicit request =>
    val form = completedFeedbackForm.copy(data = Map("journey" -> journey))
    Ok(surveyView(form))
  }

  def confirmation = refNumAction.async { implicit request =>
    viewConfirmationPage(request.refNum)
  }

  def confirmationB = refNumAction.async { implicit request =>

    repository.findById(SessionId(hc), request.refNum) map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)
        Ok(confirmationViewB(
          completedFeedbackFormNormalJourney, request.refNum,
          summary.customerDetails.flatMap(_.contactDetails.email),
          summary))
    }

  }

  def confirmationC = refNumAction.async { implicit request =>

    repository.findById(SessionId(hc), request.refNum) map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)
        Ok(confirmationViewC(
          completedFeedbackFormNormalJourney, request.refNum,
          summary.customerDetails.flatMap(_.contactDetails.email),
          summary))
    }

  }

  def formCompleteFeedback = refNumAction.async { implicit request =>
    completedFeedbackForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(surveyView(formWithErrors))),
      success => {
        sendFeedback(success, request.refNum) map { _ => Redirect(routes.FeedbackController.feedbackThankyou()) }
      }
    )
  }

  private def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  private def viewConfirmationPage(refNum: String, form: Option[Form[SurveyFeedback]] = None)(implicit request:RefNumRequest[AnyContent] ) =
    repository.findById(SessionId(hc), refNum) map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)
        Ok(confirmationView(
          form.getOrElse(completedFeedbackFormNormalJourney), refNum,
          summary.customerDetails.flatMap(_.contactDetails.email),
          summary))
      case None => InternalServerError(errorView(500))
    }

  private def sendFeedback(f: SurveyFeedback, refNum: String)(implicit request: Request[_]) = {
    audit("SurveySatisfaction", Map("satisfaction" -> f.satisfaction.rating.toString, "referenceNumber" -> refNum, "journey" -> f.journey.name)).flatMap { _ =>
      audit("SurveyFeedback", Map("feedback" -> f.details, "referenceNumber" -> refNum, "journey" -> f.journey.name))
    }
  }

  def surveyThankyou = Action { implicit request =>
    Ok(feedbackThxView()).withNewSession
  }
}

case class SurveyFeedback(satisfaction: Satisfaction, details: String, journey: Journey)
