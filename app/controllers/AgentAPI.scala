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
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import playconfig.Audit
import uk.gov.hmrc.play.http._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.math.BigDecimal
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object AgentAPI extends Controller with HeaderValidator {
  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, request.session)

  def getDocs = Action { implicit request =>
    Ok(views.html.api.apidoc())
  }

  def getSchema(name: String) = Action.async { implicit request =>
    HODConnector.getSchema(name) map { Ok(_) }
  }

  def submit(refNum: String, postcode: String): Action[AnyContent] = mustHaveValidAcceptHeader.async { implicit request =>
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
    case b: BadRequestException => BadRequest(invalidSubmission(b.message))
    case Upstream4xxResponse(body, 401, _, _) => Unauthorized(badCredentialsError(body, refNum, postcode))
    case Upstream4xxResponse(_, 409, _, _) => Conflict(duplicateSubmission(refNum))
    case Upstream5xxResponse(_, 500, _) => internalServerError
  }

  private def withAuthToken(request: Request[_], authToken: String): HeaderCarrier = {
    HeaderCarrier.fromHeadersAndSession(request.headers, request.session + (SessionKeys.authToken -> authToken))
  }

  private def badCredentialsError(body: String, refNum: String, postcode: String): String = {
    val js = Json.parse(body) match {
      case JsObject(s) if s.headOption.contains("numberOfRemainingTriesUntilIPLockout" -> JsNumber(BigDecimal(0))) =>
        JsObject(
          Seq(
            "code" -> JsString("IP_LOCKOUT"),
            "message" -> JsString(s"This IP address is locked out for 24 hours due to too many failed login attempts")
          )
        )
      case JsObject(Seq(("numberOfRemainingTriesUntilIPLockout", n))) =>
        JsObject(
          Seq(
            "code" -> JsString("INVALID_CREDENTIALS"),
            "message" -> JsString(s"Invalid credentials: $refNum - $postcode; $n tries remaining until IP lockout")
          )
        )
      case other => other
    }
    Json.prettyPrint(js)
  }

  private def internalServerError: Result = {
    InternalServerError(Json.prettyPrint(Json.parse("""{"code": "INTERNAL_SERVER_ERROR", "message": "Internal server error"}""")))
  }

  private def invalidSubmission(msg: String): String = {
    val js = Json.parse(msg) match {
      case JsObject(s) => JsObject(Seq("code" -> JsString("INVALID_SUBMISSION")) ++ s)
      case other => other
    }
    Json.prettyPrint(js)
  }

  private def duplicateSubmission(refNum: String): String = {
    Json.prettyPrint(
      Json.parse(s"""{"code": "DUPLICATE_SUBMISSION", "message": "A submission already exists for $refNum"}""")
    )
  }
}

trait HeaderValidator {
  val validateVersion: String => Boolean = _ == "1.0"
  val matchHeader: String => Option[Match] = new Regex("""^application/vnd[.]hmrc[.](.*?)[+]json""", "version") findFirstMatchIn

  val acceptHeaderRules: Option[String] => Boolean =
    _ flatMap { a => matchHeader(a) map { res => validateVersion(res.group("version"))} } getOrElse false

  def mustHaveValidAcceptHeader = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      Logger.info(request.headers.get("Accept").toString)
      if(acceptHeaderRules(request.headers.get("Accept")))
        block(request)
      else
        Future.successful(
          NotAcceptable(
            Json.prettyPrint(Json.parse(
              """{"code": "ACCEPT_HEADER_INVALID", "message": "The header Accept is missing or invalid"}""")
            )
          )
        )
    }
  }
}
