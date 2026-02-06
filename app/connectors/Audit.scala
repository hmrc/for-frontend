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

package connectors

import com.google.inject.ImplementedBy
import controllers.feedback.Survey.SurveyFeedback
import models.*
import models.pages.Summary
import models.serviceContracts.submissions.Submission
import play.api.Configuration
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.*
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, AuditResult, DatastreamMetrics}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[ForAuditConnector])
trait Audit extends AuditConnector {

  implicit def ec: ExecutionContext

  def configuration: Configuration

  def servicesConfig: ServicesConfig

  private val AUDIT_SOURCE = "for-frontend"

  def apply(event: String, detail: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val tags = hc.toAuditTags()
    val de   = DataEvent(auditSource = AUDIT_SOURCE, auditType = event, tags = tags, detail = detail)
    sendEvent(de)
  }

  /**
    * Don't use this in the rest of application(unless you know what are you doing).
    * Summary doesn't have defined formatter,
    * it is constructed manually when is deserialized from session or DB.
    */
  private val summaryWriter = {
    import play.api.libs.json.*
    Json.writes[Summary]
  }

  def sendSavedForLater(summary: Summary, exitPath: String)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val json = Json.toJson(summary)(using summaryWriter).as[JsObject] ++ Addresses.addressJson(summary)

    val tags = hc.toAuditTags().updated("exitPath", exitPath)

    val dataEvent = ExtendedDataEvent(auditSource = AUDIT_SOURCE, auditType = "SavedForLater", tags = tags, detail = json)
    sendExtendedEvent(dataEvent)
  }

  def apply(event: String, submission: Submission)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val sub = implicitly[OWrites[Submission]].writes(submission)
    val de  = ExtendedDataEvent(auditSource = AUDIT_SOURCE, auditType = event, detail = sub)
    sendExtendedEvent(de)
  }

  def sendSurveyFeedback(f: SurveyFeedback, refNum: String)(implicit hc: HeaderCarrier, request: RequestHeader): Future[AuditResult] =
    apply(
      "SurveySatisfaction",
      Map("satisfaction" -> f.satisfaction.rating.toString, "referenceNumber" -> refNum, "journey" -> f.journey.name, "surveyUrl" -> toAbsoluteUrl(f.surveyUrl))
    ).flatMap { _ =>
      apply("SurveyFeedback", Map("feedback" -> f.details, "referenceNumber" -> refNum, "journey" -> f.journey.name))
    }

  def sendFeedback(f: Feedback, refNumOpt: Option[String])(implicit hc: HeaderCarrier, request: RequestHeader): Future[AuditResult] = {
    val refNum         = refNumOpt.getOrElse("")
    val rating: Int    = f.rating.flatMap(r => Try(r.toInt).toOption).getOrElse(0)
    val satisfaction   = SatisfactionTypes.all.find(_.rating == rating).getOrElse(Satisfied)
    val surveyFeedback = SurveyFeedback(satisfaction, f.comments.getOrElse(""), FeedbackPageJourney, getReferrerUrl)
    sendSurveyFeedback(surveyFeedback, refNum)
  }

  private def platformFrontendHost(implicit request: RequestHeader): String = {
    val protocol = servicesConfig.getConfString("for-hod-adapter.protocol", "http")

    configuration.getOptional[String]("platform.frontend.host")
      .getOrElse(s"$protocol://${request.host}")
  }

  private def toAbsoluteUrl(urlOrPath: String)(implicit request: RequestHeader): String =
    if urlOrPath.contains("http") then urlOrPath else s"$platformFrontendHost$urlOrPath"

  private def getReferrerUrl(implicit request: RequestHeader): String =
    toAbsoluteUrl(request.uri)

}

object Audit {
  val referenceNumber = "referenceNumber"
  val address         = "address"
  val updatedAddress  = "updatedAddress"
  val language        = "language"

  def languageJson(implicit messages: Messages): JsObject =
    Json.obj(Audit.language -> messages.lang.language)

}

@Singleton
class ForAuditConnector @Inject() (
  val configuration: Configuration,
  val servicesConfig: ServicesConfig,
  val auditingConfig: AuditingConfig,
  val auditChannel: AuditChannel,
  val datastreamMetrics: DatastreamMetrics
)(implicit val ec: ExecutionContext
) extends Audit {}
