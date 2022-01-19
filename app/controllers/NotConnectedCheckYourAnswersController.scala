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
import connectors.{Audit, SubmissionConnector}
import controllers.feedback.Survey
import form.persistence.{FormDocumentRepository, MongoSessionRepository}
import models.NotConnectedJourney
import models.pages.{NotConnectedSummary, Summary, SummaryBuilder}
import models.serviceContracts.submissions.{NotConnected, NotConnectedSubmission, PreviouslyConnected}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.Logger
import playconfig.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class NotConnectedCheckYourAnswersController @Inject()
( repository: FormDocumentRepository,
  submissionConnector: SubmissionConnector,
  refNumAction: RefNumAction,
  cache: MongoSessionRepository,
  audit: Audit,
  cc: MessagesControllerComponents,
  notConnectedCheckYourAnswers: views.html.notConnectedCheckYourAnswers,
  confirmNotConnectedView: views.html.confirmNotConnected,
  errorView: views.html.error.error
)(implicit ec: ExecutionContext)
  extends FrontendController(cc) {

  val logger = Logger(classOf[NotConnectedCheckYourAnswersController])

  def findSummary(implicit request: RefNumRequest[_]) = {
    repository.findById(SessionId(hc), request.refNum) flatMap {
      case Some(doc) => Option(SummaryBuilder.build(doc))
      case None => None
    }
  }

  def findNotConnected(sum: Summary)(implicit hc: HeaderCarrier) = {
    getNotConnectedFromCache.flatMap { notConnected =>
      getPreviouslyConnectedFromCache().flatMap { previouslyConnected =>
        Option(NotConnectedSummary(sum, previouslyConnected, notConnected))
      }
    }
  }

  def removeSession(implicit request: RefNumRequest[_]) = {
    repository.remove(SessionId(hc))
  }

  def onPageView = refNumAction.async { implicit request =>
    findSummary.flatMap { summary =>
      findNotConnected(summary.get).map {
        case Some(notConnectedSummary) => Ok(notConnectedCheckYourAnswers(notConnectedSummary))
        case None => {
          logger.error(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
          InternalServerError(errorView(500))
        }
      }
    }
  }

  def onPageSubmit = refNumAction.async { implicit request =>
    findSummary.flatMap {
      case Some(summary) => {
        audit.sendExplicitAudit("NotConnectedSubmission", Json.obj(Audit.referenceNumber -> summary.referenceNumber))
        submitToHod(summary).map { _ =>
          Redirect(routes.NotConnectedCheckYourAnswersController.onConfirmationView)
        }.recover {
          case e: Exception => {
            logger.error(s"Could not send data to HOD - ${request.refNum} - ${hc.sessionId}")
            InternalServerError(errorView(500))
          }
        }
      }
    }
  }

  def onConfirmationView() = refNumAction.async { implicit request =>
    val feedbackForm = Survey.completedFeedbackForm.bind(
      Map("journey" -> NotConnectedJourney.name)
    ).discardingErrors

    findSummary.flatMap { summary =>
      findNotConnected(summary.get).flatMap {
        case Some(notConnectedSummary) => {
          removeSession.map(_ => Ok(confirmNotConnectedView(feedbackForm, Some(notConnectedSummary))))
        }
        case None => Future.successful(Ok(confirmNotConnectedView(feedbackForm, None)))
      }
    }
  }

  def getPreviouslyConnectedFromCache()(implicit hc: HeaderCarrier)  = {
    cache.fetchAndGetEntry[PreviouslyConnected](SessionId(hc), PreviouslyConnectedController.cacheKey).flatMap {
      case Some(x) => Future.successful(x)
      case None => Future.failed(new RuntimeException("Unable to find record in cache for previously connected"))
    }
  }

  def getNotConnectedFromCache()(implicit hc: HeaderCarrier)  = {
    cache.fetchAndGetEntry[NotConnected](SessionId(hc), NotConnectedController.cacheKey).flatMap {
      case Some(x) => Future.successful(x)
      case None => Future.failed(new RuntimeException("Unable to find record in cache for not connected"))
    }
  }

  private def submitToHod(summary: Summary)(implicit hc: HeaderCarrier) = {
    getNotConnectedFromCache.flatMap { notConnected =>
      getPreviouslyConnectedFromCache().flatMap { previouslyConnected =>
        val submission = NotConnectedSubmission(
          summary.referenceNumber,
          summary.address.get,
          notConnected.fullName,
          notConnected.emailAddress,
          notConnected.phoneNumber,
          notConnected.additionalInformation,
          Instant.now(),
          previouslyConnected.previouslyConnected
        )

        submissionConnector.submitNotConnected(summary.referenceNumber, submission)
      }
    }
  }

}