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

package controllers.feedback

import actions.{RefNumAction, RefNumRequest}
import connectors.Audit
import form.Formats.*
import form.persistence.FormDocumentRepository
import models.pages.SummaryBuilder
import models.{Journey, NormalJourney, Satisfaction}
import play.api.data.Forms.*
import play.api.data.{Form, Forms}
import play.api.mvc
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import config.SessionId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object Survey {

  case class SurveyFeedback(satisfaction: Satisfaction, details: String, journey: Journey, surveyUrl: String)

  val completedFeedbackForm: Form[SurveyFeedback] = Form(mapping(
    "satisfaction" -> Forms.of[Satisfaction],
    "details"      -> text(maxLength = 1200),
    "journey"      -> Forms.of[Journey],
    "surveyUrl"    -> text(maxLength = 2000)
  )(SurveyFeedback.apply)(o => Some(Tuple.fromProductTyped(o))))

}

@Singleton
class SurveyController @Inject() (
  cc: MessagesControllerComponents,
  repository: FormDocumentRepository,
  refNumAction: RefNumAction,
  audit: Audit,
  confirmationView: views.html.confirm,
  errorView: views.html.error.error,
  feedbackThxView: views.html.feedbackThx,
  surveyView: views.html.survey
)(implicit ec: ExecutionContext
) extends FrontendController(cc) {
  import Survey.*

  val completedFeedbackFormNormalJourney: Form[SurveyFeedback] = completedFeedbackForm.bind(Map("journey" -> NormalJourney.name)).discardingErrors

  def onPageView(journey: String): mvc.Action[AnyContent] = refNumAction { implicit request =>
    val form = completedFeedbackForm.copy(data = Map("journey" -> journey, "surveyUrl" -> request.uri))
    Ok(surveyView(form))
  }

  def confirmation: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    viewConfirmationPage(request.refNum)
  }

  def formCompleteFeedback: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    completedFeedbackForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(surveyView(formWithErrors))),
      success =>
        audit.sendSurveyFeedback(success, request.refNum).map {
          _ => Redirect(routes.FeedbackController.feedbackThankyou)
        }
    )
  }

  private def viewConfirmationPage(refNum: String, form: Option[Form[SurveyFeedback]] = None)(implicit request: RefNumRequest[AnyContent]) =
    repository.findById(SessionId(using hc), refNum) map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)
        Ok(confirmationView(
          form.getOrElse(completedFeedbackFormNormalJourney.bind(Map("surveyUrl" -> request.uri)).discardingErrors),
          refNum,
          summary.customerDetails.map(_.contactDetails.email),
          summary
        ))
      case None      => InternalServerError(errorView(500))
    }

  def surveyThankyou: mvc.Action[AnyContent] = Action { implicit request =>
    Ok(feedbackThxView()).withNewSession
  }
}
