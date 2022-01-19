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

import _root_.form.persistence.{FormDocumentRepository, MongoSessionRepository}
import actions.{RefNumAction, RefNumRequest}
import controllers.PreviouslyConnectedController.cacheKey
import controllers.dataCapturePages.ForDataCapturePage
import form.PreviouslyConnectedForm.formMapping

import javax.inject.{Inject, Singleton}
import models.pages.{NotConnectedSummary, SummaryBuilder}
import models.serviceContracts.submissions.PreviouslyConnected
import models.serviceContracts.submissions.PreviouslyConnected.format
import play.api.Logger
import play.api.mvc.MessagesControllerComponents
import playconfig.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreviouslyConnectedController @Inject()
(
  val cc: MessagesControllerComponents,
  val cache: MongoSessionRepository,
  val repository: FormDocumentRepository,
  val refNumberAction: RefNumAction,
  previouslyConnected: views.html.previouslyConnected,
  errorView: views.html.error.error
)
(implicit val ec: ExecutionContext) extends FrontendController(cc)  {
  val logger = Logger(this.getClass)

  def findSummary(implicit request: RefNumRequest[_]) = {
    repository.findById(SessionId(hc), request.refNum) flatMap {
      case Some(doc) => Option(SummaryBuilder.build(doc))
      case None => None
    }
  }

  def getPreviouslyConnectedFromCache()(implicit hc: HeaderCarrier)  = {
    cache.fetchAndGetEntry[PreviouslyConnected](SessionId(hc), PreviouslyConnectedController.cacheKey).flatMap {
      case Some(x) => Some(x)
      case None => None
    }
  }

  def findNotConnectedSummary(implicit request: RefNumRequest[_], hc: HeaderCarrier) = {
    findSummary.flatMap { summary =>
      getPreviouslyConnectedFromCache().flatMap { previouslyConnected =>
        Option(NotConnectedSummary(summary.get, previouslyConnected, None))
      }
    }
  }

  def getForm(notConnectedSummary: NotConnectedSummary) = {
    notConnectedSummary.previouslyConnected match {
      case Some(x) => formMapping.fill(x)
      case None => formMapping
    }
  }

  def onPageView = refNumberAction.async { implicit request =>
    findNotConnectedSummary.map {
      case Some(notConnectedSummary) => Ok(previouslyConnected(getForm(notConnectedSummary), notConnectedSummary.summary))
      case None => {
        logger.warn(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
        InternalServerError(errorView(500))
      }
    }
  }

  def onPageSubmit = refNumberAction.async { implicit request =>
    findSummary.flatMap {
      case Some(summary) => {
        formMapping.bindFromRequest().fold( formWithErrors => {
          Future.successful(Ok(previouslyConnected(formWithErrors, summary)))
        }, {formWithData =>
          cache.cache(SessionId(hc), cacheKey, formWithData).map { cacheWriteResult =>
            ForDataCapturePage.extractAction(request.body.asFormUrlEncoded) match {
              case ForDataCapturePage.Update => Redirect(routes.NotConnectedCheckYourAnswersController.onPageView)
              case _ => Redirect(routes.NotConnectedController.onPageView)
            }
          }
        })
      }
      case None => {
        logger.warn(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
        Future.successful(InternalServerError(errorView(500)))
      }
    }
  }
}

object PreviouslyConnectedController {
  val cacheKey = "PreviouslyConnected"
}