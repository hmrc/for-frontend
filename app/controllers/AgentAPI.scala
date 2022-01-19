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

import config.ForConfig
import connectors.{Audit, HODConnector, HodSubmissionConnector}
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.{Request => _, _}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

class AgentAPI @Inject()(cc: MessagesControllerComponents,
                         hodConnector: HODConnector,
                         submissionConnector: HodSubmissionConnector,
                         audit: Audit,
                         forConfig: ForConfig)(implicit ec: ExecutionContext)
  extends FrontendController(cc)  with HeaderValidator {

  def getDocs = Action {
    Ok(views.html.api.apidoc())
  }

  def getSchema(name: String) = Action.async { implicit request =>
    hodConnector.getSchema(name) map {
      Ok(_)
    }
  }

  def submit(refNum: String, postcode: String): Action[AnyContent] = mustHaveValidAcceptHeader.async { implicit request =>
    (forConfig.agentApiEnabled, forConfig.apiTestAccountsOnly) match {
      case (false, _) => NotFound
      case (true, true) if !refNum.startsWith(forConfig.apiTestAccountPrefix) => mustUseTestCredentials(refNum, postcode)
      case (true, _) =>
        request.body.asJson.map {
          checkCredentialsAndSubmit(_, refNum, postcode)
        }.getOrElse(
          Future.successful(BadRequest(Json.parse("""{"code": "BAD_REQUEST"}""")))
        )
    }
  }

  private def checkCredentialsAndSubmit(submission: JsValue, refNum: String, postcode: String)(implicit request: Request[_]): Future[Result] = {
    for {
      lr <- hodConnector.verifyCredentials(refNum.dropRight(3), refNum.takeRight(3), postcode)
      hc = withAuthToken(request, lr.forAuthToken)
      res <- submissionConnector.submit(refNum, submission)(hc)
      _ <- audit("APISubmission", Map("referenceNumber" -> refNum,
        "submitted" -> DateTime.now.toString))(HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request)))
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
    case UpstreamErrorResponse.Upstream5xxResponse(_) => internalServerError
  }

  private def withAuthToken(request: Request[_], authToken: String): HeaderCarrier = {
    HeaderCarrierConverter.fromHeadersAndSession(request.headers, request.session + (SessionKeys.authToken -> authToken))
  }

  private def badCredentialsError(body: String, refNum: String, postcode: String) = {
    val jsonBody = Json.parse(body)

    jsonBody.validate[IpLockout] match {
      case JsSuccess(IpLockout(0), _) =>
        Json.parse("""{"code": "IP_LOCKOUT", "message": "This IP address is locked out for 24 hours due to too many failed login attempts"}""")
      case JsSuccess(IpLockout(count), _) =>
        Json.parse(s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: $refNum - $postcode; $count tries remaining until IP lockout"}""")
      case other => jsonBody
    }
  }

  private def mustUseTestCredentials(refNum: String, postcode: String): Result = {
    Unauthorized(
      Json.parse(s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: $refNum - $postcode"}""")
    )
  }

  private def internalServerError: Result = {
    InternalServerError(Json.parse("""{"code": "INTERNAL_SERVER_ERROR", "message": "Internal server error"}"""))
  }

  private def invalidSubmission(msg: String) = {
    val jsonBody = Json.parse(msg)

    jsonBody.validate[UpstreamError] match {
      case JsSuccess(UpstreamError(errors), _) =>
        Json.parse(s"""{"code": "INVALID_SUBMISSION", "message": ${Json.toJson(errors map buildJsonError)}}""")
      case other => jsonBody
    }
  }

  private def buildJsonError: Error => Map[String, String] = e =>
    Map("field" -> e.field, "error" -> e.error, "schemaUsed" -> e.schemaUsed)

  private def duplicateSubmission(refNum: String) =
    Json.parse(s"""{"code": "DUPLICATE_SUBMISSION", "message": "A submission already exists for $refNum"}""")

  private def validSubmission(refNum: String) =
    Json.parse(s"""{"code": "VALID_SUBMISSION", "message": "Accepted submission with reference $refNum"}""")
}

trait HeaderValidator {
  val validateVersion: String => Boolean = _ == "1.0"
  val matchHeader: String => Option[Match] = new Regex("""^application/vnd[.]hmrc[.](.*?)[+]json""", "version") findFirstMatchIn

  val acceptHeaderRules: Option[String] => Boolean =
    _ flatMap { a => matchHeader(a) map { res => validateVersion(res.group("version")) } } getOrElse false

  def controllerComponents: ControllerComponents

  def mustHaveValidAcceptHeader = new ActionBuilder[Request, AnyContent] {
    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      if (acceptHeaderRules(request.headers.get("Accept")))
        block(request)
      else
        Future.successful(
          NotAcceptable(
            Json.parse(
              """{"code": "ACCEPT_HEADER_INVALID", "message": "The header Accept is missing or invalid"}""")
          )
        )
    }

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.anyContent

    override protected def executionContext: ExecutionContext = controllerComponents.executionContext
  }
}
