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
import controllers.NotConnectedController.cacheKey
import form.persistence.{FormDocumentRepository, MongoSessionRepository}
import form.NotConnectedPropertyForm.form

import javax.inject.{Inject, Singleton}
import models.pages.{NotConnectedSummary, SummaryBuilder}
import models.serviceContracts.submissions.NotConnected
import play.api.mvc.MessagesControllerComponents
import play.api.Logger
import config.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import models.pages.Summary
import play.api.mvc
import play.api.data.Form
import play.api.mvc.AnyContent

@Singleton
class NotConnectedController @Inject() (
  repository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cache: MongoSessionRepository,
  cc: MessagesControllerComponents,
  notConnectedView: views.html.notConnected,
  errorView: views.html.error.error
)(implicit ec: ExecutionContext
) extends FrontendController(cc) {

  val logger: Logger = Logger(classOf[NotConnectedController])

  def findSummary(implicit request: RefNumRequest[?]): Future[Option[Summary]] =
    repository.findById(SessionId(using hc), request.refNum) flatMap {
      case Some(doc) => Option(SummaryBuilder.build(doc))
      case None      => None
    }

  def getNotConnectedFromCache()(implicit hc: HeaderCarrier): Future[Option[NotConnected]] =
    cache.fetchAndGetEntry[NotConnected](SessionId(using hc), NotConnectedController.cacheKey).flatMap {
      case Some(x) => Some(x)
      case None    => None
    }

  def findNotConnectedSummary(implicit request: RefNumRequest[?], hc: HeaderCarrier): Future[Option[NotConnectedSummary]] =
    findSummary.flatMap { summary =>
      getNotConnectedFromCache().flatMap { notConnected =>
        Option(NotConnectedSummary(summary.get, None, notConnected))
      }
    }

  def getForm(notConnectedSummary: NotConnectedSummary): Form[NotConnected] =
    notConnectedSummary.notConnected match {
      case Some(x) => form.fill(x)
      case None    => form
    }

  def onPageView: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    findNotConnectedSummary.map {
      case Some(notConnectedSummary) => Ok(notConnectedView(getForm(notConnectedSummary), notConnectedSummary.summary))
      case None                      =>
        logger.error(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
        InternalServerError(errorView(500))
    }
  }

  def onPageSubmit: mvc.Action[AnyContent] = refNumAction.async { implicit request =>
    findSummary.flatMap {
      case Some(summary) =>
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(Ok(notConnectedView(formWithErrors, summary))),
          formWithData =>
            cache.cache(SessionId(using hc), cacheKey, formWithData).map { _ =>
              Redirect(routes.NotConnectedCheckYourAnswersController.onPageView)
            }
        )
      case None          =>
        logger.warn(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
        Future.successful(InternalServerError(errorView(500)))
    }
  }
}

object NotConnectedController {
  val cacheKey = "NotConnected"
}
