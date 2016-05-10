/*
 * Copyright 2016 HM Revenue & Customs
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

import connectors.{HODConnector, SubmissionConnector}
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{BadRequestException, HeaderCarrier, SessionKeys, Upstream4xxResponse}

object AgentAPI extends FrontendController {

  def getDocs = Action { implicit request =>
    Ok(views.html.api.apidoc())
  }

  def getSchema(name: String) = Action.async { implicit request =>
    HODConnector.getSchema(name) map { Ok(_) }
  }

  def submit(refNum: String, postcode: String): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map { js =>
      HODConnector.verifyCredentials(refNum.dropRight(3), refNum.takeRight(3), postcode).flatMap { lr =>
        val hc = withAuthToken(request, lr.forAuthToken)
        SubmissionConnector.submit(refNum, js)(hc)
      }.recover {
        case b: BadRequestException => BadRequest(b.message)
        case Upstream4xxResponse(body, 401, _, _) => Unauthorized(body)
        case Upstream4xxResponse(body, 409, _, _) => Conflict(body)
      }
    }.getOrElse(BadRequest)
  }

  private def withAuthToken(request: Request[_], authToken: String): HeaderCarrier = {
    HeaderCarrier.fromHeadersAndSession(request.headers, request.session + (SessionKeys.authToken -> authToken))
  }
}
