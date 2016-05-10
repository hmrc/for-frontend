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

package config

import controllers.toFut
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Results._
import play.api.mvc._
import play.twirl.api.Html
import playconfig.{ForHttp, WSHttp}
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter
import uk.gov.hmrc.play.language.LanguageUtils
import useCases.Now

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object ForGlobal extends ForGlobal

trait ForGlobal extends DefaultFrontendGlobal {
  lazy val forHttp: ForHttp = WSHttp

  def auditConnector: uk.gov.hmrc.play.audit.http.connector.AuditConnector = AuditServiceConnector

  def frontendAuditFilter: uk.gov.hmrc.play.audit.filters.FrontendAuditFilter = AuditFilter

  def loggingFilter: uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter = LoggingFilter

  override def frontendFilters: Seq[EssentialFilter] = defaultFrontendFilters ++ Seq(SessionTimeoutFilter)

  def microserviceMetricsConfig(implicit app: play.api.Application): Option[Configuration] = ForConfig.metricsConfig

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: play.api.mvc.Request[_]): play.twirl.api.Html = {
    views.html.error.error500()
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    views.html.error.error404()(request, LanguageUtils.getCurrentLang)
  }

  override def badRequestTemplate(implicit request: Request[_]): Html = {
    views.html.error.error500()
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = {

    implicit val requestHeader: RequestHeader = rh
    ex.getCause match {
      case e: BadRequestException => BadRequest(views.html.error.error500())
      case Upstream4xxResponse(_, 408, _, _) => RequestTimeout(views.html.error.error408())
      case Upstream4xxResponse(_, 409, _, _) => Conflict(views.html.error.error409())
      case Upstream4xxResponse(_, 410, _, _) => Gone(views.html.error.error410())
      case e: NotFoundException => NotFound(views.html.error.error404())
      case _ => super.resolveError(rh, ex)
    }
  }
}

object AuditServiceConnector extends AuditConnector with AppName {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

object AuditFilter extends FrontendAuditFilter with AppName {
  override lazy val maskedFormFields = Seq.empty
  override lazy val applicationPort = None
  override lazy val auditConnector = AuditServiceConnector

  override def controllerNeedsAuditing(controllerName: String): Boolean = false
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = ForConfig.controllerConfigs
}

object LoggingFilter extends FrontendLoggingFilter {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object SessionTimeoutFilter extends SessionTimeoutFilter {
  def now = () => DateTime.now() //scalastyle:ignore

  lazy val timeoutDuration = ForConfig.sessionTimeoutDuration
}

trait SessionTimeoutFilter extends Filter {
  val timestampKey = "lastrequesttimestamp"

  def now: Now

  val timeoutDuration: Duration

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] =
    timeOfLastRequest(rh) match {
      case Some(dt: DateTime) =>
        if (dt.isBefore(now().minusMinutes(timeoutDuration.toMinutes.toInt)))
          Redirect(controllers.routes.Application.sessionTimeout()).withNewSession
        else
          f(rh)
      case None => f(rh).map(_.addingToSession((timestampKey, now().toString))(rh))
    }

  def timeOfLastRequest(rh: RequestHeader): Option[DateTime] = rh.session.get(timestampKey) flatMap { ts =>
    Some(DateTime.parse(ts))
  }
}
