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

import play.api.i18n.MessagesApi
import play.api.mvc.Results.*
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  errorView: views.html.error.error
)(implicit val ec: ExecutionContext
) extends FrontendErrorHandler {

  override def onServerError(header: RequestHeader, exception: Throwable): Future[Result] = {

    implicit val request: Request[?] = Request(header, "")

    exception.getCause match {
      case _: BadRequestException              => BadRequest(errorView(500))
      case UpstreamErrorResponse(_, 404, _, _) => NotFound(errorView(404))
      case UpstreamErrorResponse(_, 408, _, _) => RequestTimeout(errorView(408))
      case UpstreamErrorResponse(_, 409, _, _) => Conflict(errorView(409))
      case UpstreamErrorResponse(_, 410, _, _) => Gone(errorView(410))
      case _: NotFoundException                => NotFound(errorView(404))
      case _                                   => super.resolveError(header, exception)
    }
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] =
    render { implicit request =>
      errorView(500)
    }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] =
    render { implicit request =>
      errorView(404)
    }

  private def render(template: Request[?] => Html)(implicit rh: RequestHeader): Future[Html] =
    Future.successful(template(Request(rh, "")))

}
