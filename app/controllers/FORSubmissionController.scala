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

package controllers

import actions.{RefNumAction, RefNumRequest}
import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import useCases.SubmitBusinessRentalInformation

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FORSubmissionController @Inject() (
  cc: MessagesControllerComponents,
  refNumberAction: RefNumAction,
  submitBusinessRentalInformation: SubmitBusinessRentalInformation,
  errorView: views.html.error.error
)(implicit ec: ExecutionContext
) extends FrontendController(cc)
  with Logging {

  lazy val confirmationUrl = controllers.feedback.routes.SurveyController.confirmation.url

  def submit: Action[AnyContent] = refNumberAction.async { implicit request: RefNumRequest[AnyContent] =>
    request.body.asFormUrlEncoded.flatMap { body =>
      body.get("declaration").map { agree =>
        if (agree.headOption.exists(_.toBoolean)) submit(request.refNum) else rejectSubmission
      }
    } getOrElse rejectSubmission
  }

  private def submit[T](refNum: String)(implicit request: RefNumRequest[T]): Future[Result] = {
    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    for {
      _ <- submitBusinessRentalInformation(refNum)(hc, request)
    } yield
    // Metrics.submissions.mark() //TODO - Solve metrics
    Found(confirmationUrl)
  } recoverWith { case UpstreamErrorResponse(_, 409, _, _) => Conflict(errorView(409)) }

  private def rejectSubmission = Future.successful {
    Found(routes.ApplicationController.declarationError.url)
  }

}
