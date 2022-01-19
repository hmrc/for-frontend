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

package playconfig

import com.google.inject.ImplementedBy
import connectors.HODConnector
import form.persistence.FormDocumentRepository

import javax.inject.{Inject, Singleton}
import models.journeys.Journey
import models.pages.SummaryBuilder
import org.joda.time.DateTime
import security.LoginToHOD._
import uk.gov.hmrc.http._
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases._

import scala.concurrent.ExecutionContext


//object FORAuditConnector extends AuditConnector with AppName with AppNameHelper {
//  override lazy val auditingConfig = LoadAuditingConfig("auditing")
//}

//trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete  with AppName with RunMode

//object WSHttp extends ForHttp with AppNameHelper with RunModeHelper {
//  override protected def configuration: Option[Config] = Option(runModeConfiguration.underlying)
//
//  override protected def actorSystem: ActorSystem = Play.current.actorSystem
//}
/*
trait ForHttp extends WSHttp {
  override val hooks = Seq.empty
  lazy val useDummyIp = ForConfig.useDummyIp

  // By default HTTP Verbs does not provide access to the pure response body of a 4XX and we need it
  // An IP address needs to be injected because of the lockout mechanism
  override def doGet(url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val hc2 = if (useDummyIp) hc.withExtraHeaders((trueClientIp, "")) else hc
    super.doGet(url, headers)(hc2, ec).map { res =>
      res.status match {
        case 401 => throw Upstream4xxResponse(res.body, 401, 401, res.allHeaders)
        case 409 => throw Upstream4xxResponse(res.body, 409, 409, res.allHeaders)
        case _ => res
      }
    }(ec)
  }

  override def doPut[A](url: String,
                        body: A,
                        headers: Seq[(String, String)])(implicit rds: Writes[A],
                                                        hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    super.doPut(url, body, headers)(rds, hc, ec).map { res =>
      if (res.status == 400) throw new BadRequestException(res.body) else res
    }(ec)
  }

}

object FormPartialProvider extends FormPartialRetriever {

  override val httpGet = Play.current.injector.instanceOf[ForHttp]
  override def crypto = encrypt

  private def encrypt(value: String): String = {
    val sessionCrypto = Play.current.injector.instanceOf[SessionCookieCrypto]
    sessionCrypto.crypto.encrypt(PlainText(value)).value
  }

}


object SessionCrypto {
  val applicationCrypto = new ApplicationCrypto(Play.current.configuration.underlying)
  val crypto = new SessionCookieCryptoFilter(applicationCrypto)
}
*/


/*
trait Audit {
  val referenceNumber = "referenceNumber"

  val auditConnector = AuditServiceConnector

  def apply(event: String, detail: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val tags = hc.toAuditTags()
    val de = DataEvent(auditSource = "for-frontend", auditType = event, tags = tags, detail = detail)
    auditConnector.sendEvent(de)
  }

  def sendExplicitAudit[T](auditType: String, detail: T)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[T]) =
    auditConnector.sendExplicitAudit(auditType, detail)(hc, ec, writes)

  def sendExplicitAudit(auditType: String, detail: JsObject)(implicit hc: HeaderCarrier, ec: ExecutionContext) =
    auditConnector.sendExplicitAudit(auditType, detail)(hc, ec)

}
*/

//object S4L extends AppName with AppNameHelper{
//  def expiryDateInDays: Int = appNameConfiguration.getInt("savedForLaterExpiryDays")
//    .getOrElse(throw new Exception("No config setting for expiry days"))
//}

object SessionId {
  def apply(implicit hc: HeaderCarrier): String = hc.sessionId.map(_.value).getOrElse(throw SessionIdMissing())
}
case class SessionIdMissing() extends Exception

object SaveForLater {
  def apply()(implicit ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    Generate7LengthLowercaseAlphaNumPassword(), StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
  def apply(pwd: String)(implicit ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    UseUserAlphaNumPassword(pwd), StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
}

object ContinueWithSavedSubmission {
  def apply()(implicit hc: HeaderCarrier, ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): ContinueWithSavedSubmission = useCases.ContinueWithSavedSubmission(
    LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply,
    SummaryBuilder.build, Journey.pageToResumeAt, () => DateTime.now
  )
}


/**
 * Temporal solution before we move all login logic to separate service class.
 * This allow us to test login controller without starting google guice.
 */
@ImplementedBy(classOf[DefaultLoginToHodAction])
trait LoginToHODAction {
  def apply(implicit hc: HeaderCarrier, ec: ExecutionContext): LoginToHOD
}

@Singleton
class DefaultLoginToHodAction @Inject() (implicit hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository) extends LoginToHODAction {

  override def apply(implicit hc: HeaderCarrier, ec: ExecutionContext): LoginToHOD = security.LoginToHOD(
    hodConnector.verifyCredentials, LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply
  )
}
