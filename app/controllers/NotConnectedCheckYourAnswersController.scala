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

import actions.{RefNumAction, RefNumRequest}
import connectors.{Audit, SubmissionConnector}
import controllers.feedback.Survey
import form.persistence.{FormDocumentRepository, MongoSessionRepository}
import models.pages.{NotConnectedSummary, Summary, SummaryBuilder}
import models.serviceContracts.submissions.{NotConnected, NotConnectedSubmission, PreviouslyConnected}
import models.{Addresses, NotConnectedJourney}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import config.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotConnectedCheckYourAnswersController @Inject() (
  repository: FormDocumentRepository,
  submissionConnector: SubmissionConnector,
  refNumAction: RefNumAction,
  cache: MongoSessionRepository,
  audit: Audit,
  cc: MessagesControllerComponents,
  notConnectedCheckYourAnswers: views.html.notConnectedCheckYourAnswers,
  confirmNotConnectedView: views.html.confirmNotConnected,
  errorView: views.html.error.error
)(using ec: ExecutionContext
) extends FrontendController(cc)
  with Logging:

  def findSummary(using request: RefNumRequest[?]): Future[Option[Summary]] =
    repository.findById(SessionId(using hc), request.refNum) flatMap {
      case Some(doc) => Option(SummaryBuilder.build(doc))
      case None      => None
    }

  private def findNotConnected(sum: Summary)(using hc: HeaderCarrier): Future[Option[NotConnectedSummary]] =
    getNotConnectedFromCache().flatMap { notConnected =>
      getPreviouslyConnectedFromCache().flatMap { previouslyConnected =>
        Option(NotConnectedSummary(sum, previouslyConnected, notConnected))
      }
    }

  private def removeSession(using request: RefNumRequest[?]): Future[Unit] =
    repository.remove(SessionId(using hc))

  def onPageView: Action[AnyContent] = refNumAction.async { implicit request =>
    findSummary.flatMap { summary =>
      findNotConnected(summary.get).map {
        case Some(notConnectedSummary) => Ok(notConnectedCheckYourAnswers(notConnectedSummary))
        case None                      =>
          logger.error(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
          NotFound(errorView(404))
      }
    }
  }

  def onPageSubmit: Action[AnyContent] = refNumAction.async { implicit request =>
    findSummary.flatMap {
      case Some(summary) =>
        val json = Json.obj(Audit.referenceNumber -> summary.referenceNumber) ++
          Addresses.addressJson(summary) ++
          Audit.languageJson
        submitToHod(summary).map { _ =>
          audit.sendExplicitAudit("NotConnectedSubmission", json)
          Redirect(routes.NotConnectedCheckYourAnswersController.onConfirmationView)
        }.recover {
          case _: Exception =>
            logger.error(s"Could not send data to HOD - ${request.refNum} - ${hc.sessionId}")
            audit.sendExplicitAudit("NotConnectedSubmissionFailed", json)
            NotFound(errorView(404))
        }
      case None          => NotFound(errorView(404))
    }
  }

  def onConfirmationView: Action[AnyContent] = refNumAction.async { implicit request =>
    val feedbackForm = Survey.completedFeedbackForm.bind(
      Map("journey" -> NotConnectedJourney.name, "surveyUrl" -> request.uri)
    ).discardingErrors

    findSummary.flatMap { summary =>
      findNotConnected(summary.get).flatMap {
        case Some(notConnectedSummary) =>
          removeSession.map(_ => Ok(confirmNotConnectedView(feedbackForm, Some(notConnectedSummary))))
        case None                      => Future.successful(Ok(confirmNotConnectedView(feedbackForm, None)))
      }
    }
  }

  def getPreviouslyConnectedFromCache()(using hc: HeaderCarrier): Future[PreviouslyConnected] =
    cache.fetchAndGetEntry[PreviouslyConnected](SessionId(using hc), PreviouslyConnectedController.cacheKey).flatMap {
      case Some(x) => Future.successful(x)
      case None    => Future.failed(RuntimeException("Unable to find record in cache for previously connected"))
    }

  def getNotConnectedFromCache()(using hc: HeaderCarrier): Future[NotConnected] =
    cache.fetchAndGetEntry[NotConnected](SessionId(using hc), NotConnectedController.cacheKey).flatMap {
      case Some(x) => Future.successful(x)
      case None    => Future.failed(RuntimeException("Unable to find record in cache for not connected"))
    }

  private def submitToHod(summary: Summary)(using hc: HeaderCarrier, messages: Messages) =
    getNotConnectedFromCache().flatMap { notConnected =>
      getPreviouslyConnectedFromCache().flatMap { previouslyConnected =>
        val submission = NotConnectedSubmission(
          summary.referenceNumber,
          summary.address.get,
          notConnected.fullName,
          notConnected.emailAddress,
          notConnected.phoneNumber,
          notConnected.additionalInformation,
          Instant.now(),
          previouslyConnected.previouslyConnected,
          messages.lang.language
        )

        submissionConnector.submitNotConnected(summary.referenceNumber, submission)
      }
    }
