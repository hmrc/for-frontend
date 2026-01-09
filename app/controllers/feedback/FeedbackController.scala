/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.{Audit, ForHttp}
import controllers.*
import form.Formats.*
import models.{Feedback, Journey, NormalJourney, NotConnectedJourney}
import play.api.Logging
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Forms}
import play.api.mvc.*
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{feedbackForm, feedbackThx}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FeedbackController @Inject() (
  audit: Audit,
  cc: MessagesControllerComponents,
  http: ForHttp,
  val servicesConfig: ServicesConfig,
  feedbackThankyouView: feedbackThx,
  feedbackFormView: feedbackForm
)(implicit ec: ExecutionContext
) extends FrontendController(cc)
  with Logging:

  private val contactFrontendPartialBaseUrl: String  = servicesConfig.baseUrl("contact-frontend")
  private val contactFrontendFeedbackPostUrl: String = s"$contactFrontendPartialBaseUrl/contact/beta-feedback/submit-unauthenticated"

  object FeedbackFormMapper {

    val feedbackForm: Form[Feedback] = Form(
      mapping(
        "feedback-rating"   -> optional(text).verifying("feedback.rating.required", _.isDefined),
        "feedback-name"     -> text,
        "feedback-email"    -> text,
        "service"           -> text,
        "referrer"          -> text,
        "journey"           -> Forms.of[Journey],
        "feedback-comments" -> optional(
          text.verifying("feedback.commments.maxLength", _.length <= 1200)
        )
      )(Feedback.apply)(o => Some(Tuple.fromProductTyped(o)))
    )
  }

  import FeedbackFormMapper.feedbackForm

  def handleFeedbackSubmit: Action[AnyContent] = Action.async { implicit request =>
    feedbackForm.bindFromRequest().fold(
      formWithErrors => BadRequest(feedbackFormView(formWithErrors)),
      feedback => {
        val urlEncodedForm = request.body.asFormUrlEncoded.get

        implicit val headerCarrier: HeaderCarrier = hc.copy(authorization = None).withExtraHeaders("Csrf-Token" -> "nocheck")

        http.postForm(contactFrontendFeedbackPostUrl, urlEncodedForm).map { r =>
          if is2xx(r.status) then logger.info(s"Feedback successful: ${r.status} response from $contactFrontendFeedbackPostUrl")
          else logger.error(s"Feedback FAILED: ${r.status} response from $contactFrontendFeedbackPostUrl, \nheaderCarrier: $headerCarrier")
        }

        audit.sendFeedback(feedback, request.session.get("refNum")).map {
          _ => Redirect(controllers.feedback.routes.FeedbackController.feedbackThankyou)
        }
      }
    )
  }

  def feedback: Action[AnyContent] = Action { implicit request =>
    Ok(feedbackFormView(feedbackForm.bind(Map("journey" -> NormalJourney.name)).discardingErrors))
  }

  def notConnectedFeedback: Action[AnyContent] = Action { implicit request =>
    Ok(feedbackFormView(feedbackForm.bind(Map("journey" -> NotConnectedJourney.name)).discardingErrors))
  }

  def feedbackThankyou: Action[AnyContent] = Action { implicit request =>
    Ok(feedbackThankyouView())
  }
