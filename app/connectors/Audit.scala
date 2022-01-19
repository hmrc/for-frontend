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

package connectors

import com.google.inject.ImplementedBy
import models.pages.Summary

import javax.inject.{Inject, Singleton}
import models.serviceContracts.submissions.Submission
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, AuditResult, DatastreamMetrics}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import uk.gov.hmrc.play.audit.AuditExtensions._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ForAuditConnector])
trait Audit extends AuditConnector {

  implicit def ec: ExecutionContext

  private val AUDIT_SOURCE = "for-frontend"

  def apply(event: String, detail: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val tags = hc.toAuditTags()
    val de = DataEvent(auditSource = AUDIT_SOURCE, auditType = event, tags = tags, detail = detail)
    sendEvent(de)
  }

  /**
   * Don't use this in the rest of application(unless you know what are you doing).
   * Summary doesn't have defined formatter,
   * it is constructed manually when is deserialized from session or DB.
   */
  private val summaryWriter = {
    import play.api.libs.json._
    import play.api.libs.json.JodaWrites._
    Json.writes[Summary]
  }

  def apply(even: String, sum: Summary, exitPath: String)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val details = Json.toJson(sum)(summaryWriter)

    val tags = hc.toAuditTags().+("exitPath" -> exitPath)

    val dataEvent = ExtendedDataEvent(auditSource = AUDIT_SOURCE, auditType = even, tags = tags, detail = details)
    sendExtendedEvent(dataEvent)
  }

  def apply(event: String, submission: Submission)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val sub = (implicitly[OWrites[Submission]]).writes(submission)
    val de = ExtendedDataEvent(auditSource = AUDIT_SOURCE, auditType = event, detail = sub)
    sendExtendedEvent(de)
  }

}

object Audit {
  val referenceNumber = "referenceNumber"
}

@Singleton
class ForAuditConnector @Inject() (val auditingConfig: AuditingConfig,
                                   val auditChannel: AuditChannel,
                                   val datastreamMetrics: DatastreamMetrics
                                  )(implicit val ec: ExecutionContext) extends Audit {
}
