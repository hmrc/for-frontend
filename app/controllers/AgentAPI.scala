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
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import playconfig.Audit
import uk.gov.hmrc.play.http._

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
    HODConnector.getSchema(name) map {
      Ok(_)
    }
  }

  def submit(refNum: String, postcode: String): Action[AnyContent] = mustHaveValidAcceptHeader.async { implicit request =>
    (ForConfig.agentApiEnabled, ForConfig.apiTestAccountsOnly) match {
      case (false, _) => NotFound
      case (true, true) if !refNum.startsWith(ForConfig.apiTestAccountPrefix) => mustUseTestCredentials(refNum, postcode)
      case (true, _) =>
        request.body.asJson.map {
          checkCredentialsAndSubmit(_, refNum, postcode)
        }.getOrElse(
          BadRequest(Json.parse("""{"code": "BAD_REQUEST"}"""))
        )
    }
  }

  private def checkCredentialsAndSubmit(submission: JsValue, refNum: String, postcode: String)(implicit request: Request[_]) = {
    for {
      lr <- HODConnector.verifyCredentials(refNum.dropRight(3), refNum.takeRight(3), postcode)
      hc = withAuthToken(request, lr.forAuthToken)
      res <- SubmissionConnector.submit(refNum, submission)(hc)
      _ <- Audit("APISubmission", Map("referenceNumber" -> refNum, "submitted" -> DateTime.now.toString))
    } yield {
      res.header.status match {
        case 200 => Ok(validSubmission(refNum))
        case _ => res
      }
    }
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

  private def mustUseTestCredentials(refNum: String, postcode: String): Result = {
    Unauthorized(
      Json.prettyPrint(
        Json.parse(s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: $refNum - $postcode"}""")
      )
    )
  }

  private def internalServerError: Result = {
    InternalServerError(Json.prettyPrint(Json.parse("""{"code": "INTERNAL_SERVER_ERROR", "message": "Internal server error"}""")))
  }

  private def invalidSubmission(msg: String): String = {
    val js = Json.parse(msg) match {
      case JsObject(Seq((k, v))) => JsObject(Seq("code" -> JsString("INVALID_SUBMISSION"), "message" -> v))
      case other => other
    }
    Json.prettyPrint(js)
  }

  private def duplicateSubmission(refNum: String): String = {
    Json.prettyPrint(
      Json.parse(s"""{"code": "DUPLICATE_SUBMISSION", "message": "A submission already exists for $refNum"}""")
    )
  }

  private def validSubmission(refNum: String) = {
    Json.prettyPrint(
      Json.parse(s"""{"code": "VALID_SUBMISSION", "message": "Accepted submission with reference $refNum"}""")
    )
  }
}

trait HeaderValidator {
  val validateVersion: String => Boolean = _ == "1.0"
  val matchHeader: String => Option[Match] = new Regex("""^application/vnd[.]hmrc[.](.*?)[+]json""", "version") findFirstMatchIn

  val acceptHeaderRules: Option[String] => Boolean =
    _ flatMap { a => matchHeader(a) map { res => validateVersion(res.group("version")) } } getOrElse false

  def mustHaveValidAcceptHeader = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      if (acceptHeaderRules(request.headers.get("Accept")))
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
