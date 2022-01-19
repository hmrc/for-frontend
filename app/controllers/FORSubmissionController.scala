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

import actions.{RefNumAction, RefNumRequest}
import form.persistence.FormDocumentRepository
import helpers.AddressAuditing
import javax.inject.{Inject, Singleton}
import models.pages.SummaryBuilder
import play.api.mvc._
import playconfig.SessionId
import uk.gov.hmrc.http.Upstream4xxResponse
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import useCases.SubmitBusinessRentalInformation

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FORSubmissionController @Inject() (cc: MessagesControllerComponents,
                                         documentRepo: FormDocumentRepository,
                                         audit: connectors.Audit,
                                         refNumberAction: RefNumAction,
                                         submitBusinessRentalInformation: SubmitBusinessRentalInformation,
                                         auditAddresses: AddressAuditing,
                                        errorView: views.html.error.error
                                        )(implicit ec: ExecutionContext) extends FrontendController(cc) {

  lazy val confirmationUrl = controllers.feedback.routes.SurveyController.confirmation.url

  def submit: Action[AnyContent] = refNumberAction.async { implicit request:RefNumRequest[AnyContent] =>
    request.body.asFormUrlEncoded.flatMap { body =>
      body.get("declaration").map { agree =>
        if (agree.headOption.exists(_.toBoolean)) submit(request.refNum) else rejectSubmission
      }
    } getOrElse rejectSubmission
  }

  private def submit[T](refNum: String)(implicit request: RefNumRequest[T]): Future[Result] = {
    val hc = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))
    for {
      submission <- submitBusinessRentalInformation(refNum)(hc)
      _ <- audit("FormSubmission", submission)(hc)
      _ <- auditAddress(refNum, request)
    } yield {
      // Metrics.submissions.mark() //TODO - Solve metrics
      Found(confirmationUrl)
    }
  }recoverWith { case Upstream4xxResponse(_, 409, _, _) => Conflict(errorView(409)) }

  protected def auditAddress[T](refNum: String, request: RefNumRequest[_]): Future[Unit] = {
    val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    documentRepo.findById(SessionId(hc), refNum) flatMap {
      case Some(doc) =>
        val s = SummaryBuilder.build(doc)
        auditAddresses(s, request)
      case None => ()
    }
  }

  private def rejectSubmission = Future.successful {
    Found(routes.ApplicationController.declarationError.url)
  }

}
