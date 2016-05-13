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

import config.ForConfig
import connectors.{HODConnector, SubmissionConnector}
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Controller, Request}
import playconfig.Audit
import uk.gov.hmrc.play.http.{BadRequestException, HeaderCarrier, SessionKeys, Upstream4xxResponse}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object AgentAPI extends Controller {
  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, request.session)

  def getDocs = Action { implicit request =>
    Ok(views.html.api.apidoc())
  }

  def getSchema(name: String) = Action.async { implicit request =>
    HODConnector.getSchema(name) map { Ok(_) }
  }

  def submit(refNum: String, postcode: String): Action[AnyContent] = Action.async { implicit request =>
    if(ForConfig.agentApiEnabled) {
      request.body.asJson.map { js =>
        checkCredentialsAndSubmit(js, refNum, postcode)(request)
      }.getOrElse(BadRequest)
    } else {
      NotFound
    }
  }

  private def checkCredentialsAndSubmit(submission: JsValue, refNum: String, postcode: String)(implicit request: Request[_]) = {
    for {
      lr <- HODConnector.verifyCredentials(refNum.dropRight(3), refNum.takeRight(3), postcode)
      hc = withAuthToken(request, lr.forAuthToken)
      res <- SubmissionConnector.submit(refNum, submission)(hc)
      _ <- Audit("APISubmission", Map("referenceNumber" -> refNum, "submitted" -> DateTime.now.toString))
    } yield res
  } recover {
    case b: BadRequestException => BadRequest(b.message)
    case Upstream4xxResponse(body, 401, _, _) => Unauthorized(badCredentialsError(body, refNum, postcode))
    case Upstream4xxResponse(body, 409, _, _) => Conflict(body)
  }

  private def withAuthToken(request: Request[_], authToken: String): HeaderCarrier = {
    HeaderCarrier.fromHeadersAndSession(request.headers, request.session + (SessionKeys.authToken -> authToken))
  }

  private def badCredentialsError(body: String, refNum: String, postcode: String): String = {
    val js = Json.parse(body) match {
      case JsObject(s) if s.headOption.contains("numberOfRemainingTriesUntilIPLockout" -> JsNumber(BigDecimal(0))) =>
        JsObject(Seq("error" -> JsString(s"This IP address is locked out for 24 hours due to too many failed login attempts")))
      case JsObject(fields) => JsObject(Seq("error" -> JsString(s"invalid credentials: $refNum - $postcode")) ++ fields)
      case other => other
    }
    Json.prettyPrint(js)
  }
}
