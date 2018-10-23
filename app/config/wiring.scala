/*
 * Copyright 2018 HM Revenue & Customs
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

package playconfig

import config.{AuditServiceConnector, ForConfig}
import connectors.HODConnector
import form.persistence.SessionScopedFormDocumentRepository
import models.journeys.Journey
import models.pages.SummaryBuilder
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Writes
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.http.cache.client.{ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import _root_.uk.gov.hmrc.http.HeaderNames._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases._
import security.LoginToHOD._

import scala.concurrent.Future
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig

object FORAuditConnector extends AuditConnector with AppName {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete  with AppName with RunMode

object WSHttp extends ForHttp

trait ForHttp extends WSHttp {
  override val hooks = Seq.empty
  lazy val useDummyIp = ForConfig.useDummyIp

  // By default HTTP Verbs does not provide access to the pure response body of a 4XX and we need it
  // An IP address needs to be injected because of the lockout mechanism
  override def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val hc2 = if (useDummyIp) hc.withExtraHeaders((trueClientIp, "")) else hc
    get(url)(hc2).map { res =>
      res.status match {
        case 401 => throw Upstream4xxResponse(res.body, 401, 401, res.allHeaders)
        case 409 => throw Upstream4xxResponse(res.body, 409, 409, res.allHeaders)
        case _ => res
      }
    }
  }

  override def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    put(url, body)(rds, hc) map { res =>
      if (res.status == 400) throw new BadRequestException(res.body) else res
    }
  }

  protected def get(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doGet(url)(hc)

  protected def put[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = super.doPut(url, body)(rds, hc)
}

object FormPartialProvider extends FormPartialRetriever {
  override val httpGet = WSHttp
  override lazy val crypto = SessionCookieCryptoFilter.encrypt _
}

object Audit extends Audit

trait Audit {
  val auditConnector = AuditServiceConnector

  def apply(event: String, detail: Map[String, String], tags: Map[String, String] = Map.empty): Future[AuditResult] = {
    val de = DataEvent(auditSource = "for-frontend", auditType = event, tags = tags, detail = detail)
    auditConnector.sendEvent(de)
  }
}

// We need save4Later and not keystore because keystore sessions expire after 60 minutes and ours last longer
object ShortLivedCacher extends ShortLivedHttpCaching with AppName with ServicesConfig  {
  override def defaultSource: String = appName
  override def baseUri: String = baseUrl("cachable.short-lived-cache")
  override def domain: String = getConfString("cachable.short-lived-cache.domain", throw new Exception("No config setting for cache domain"))
  override def http: HttpGet with HttpPut with HttpDelete = WSHttp
}

object S4L extends ShortLivedCache with AppName with ServicesConfig {
  override implicit lazy val crypto: CompositeSymmetricCrypto = ApplicationCrypto.JsonCrypto
  override def shortLiveCache: ShortLivedHttpCaching = ShortLivedCacher
  def expiryDateInDays: Int = appNameConfiguration.getInt("savedForLaterExpiryDays")
    .getOrElse(throw new Exception("No config setting for expiry days"))
}

object FormPersistence {
  lazy val formDocumentRepository = new SessionScopedFormDocumentRepository(S4L)
}

object SessionId {
  def apply(implicit hc: HeaderCarrier): String = hc.sessionId.map(_.value).getOrElse(throw SessionIdMissing())
}
case class SessionIdMissing() extends Exception

object SaveForLater {
  def apply(): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    Generate7LengthLowercaseAlphaNumPassword.apply, StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
  def apply(pwd: String): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    UseUserAlphaNumPassword(pwd), StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
}

object ContinueWithSavedSubmission {
  def apply(implicit hc: HeaderCarrier): ContinueWithSavedSubmission = useCases.ContinueWithSavedSubmission(
    LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply,
    SummaryBuilder.build, Journey.pageToResumeAt, () => DateTime.now
  )
}

object LoginToHOD {
  def apply(implicit hc: HeaderCarrier): LoginToHOD = security.LoginToHOD(
    HODConnector.verifyCredentials, LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply
  )
}

object Environment extends uk.gov.hmrc.play.config.RunMode {
  def isDev = env == "Dev"
  val analytics = ForConfig.analytics
}
