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

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import play.api.mvc.Results._
import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi,
                              errorView:views.html.error.error) extends FrontendErrorHandler {

  override def onServerError(header: RequestHeader, exception: Throwable): Future[Result] = {

    implicit val request: Request[_] = Request( header,  "")
    exception.getCause match {
      case e: BadRequestException => BadRequest(errorView(500))
      case Upstream4xxResponse(_, 404, _, _) => NotFound(errorView(404))
      case Upstream4xxResponse(_, 408, _, _) => RequestTimeout(errorView(408))
      case Upstream4xxResponse(_, 409, _, _) => Conflict(errorView(409))
      case Upstream4xxResponse(_, 410, _, _) => Gone(errorView(410))
      case e: NotFoundException => NotFound(errorView(404))
      case _ => super.resolveError(header, exception)
    }
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    errorView(500)
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    errorView(404)
  }
}
