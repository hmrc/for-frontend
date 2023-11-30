/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.pekko.actor.ActorSystem
import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import config.ForConfig

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Writes
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderNames.trueClientIp
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{ExecutionContext, Future}

/*
with HttpDelete with WSDelete  with AppName with RunMode
 */
@ImplementedBy(classOf[ForHttpClient])
trait ForHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost {

}

@Singleton
class ForHttpClient @Inject() (val config: Configuration,
                               forConfig: ForConfig,
                               override protected val actorSystem: ActorSystem,
                               override val wsClient: WSClient)  extends ForHttp {

  private val useDummyIp = forConfig.useDummyIp
  override val hooks: Seq[HttpHook] = Seq.empty

  private def useDummyIPInTrueClientIPHeader(headers: Seq[(String, String)]): Seq[(String, String)] =
    if (useDummyIp) {
      (trueClientIp, "") +: headers.filterNot(x => x._1.toLowerCase == trueClientIp.toLowerCase)
    } else {
      headers
    }

  override def doPost[A](
                          url: String,
                          body: A,
                          headers: Seq[(String, String)]
                        )(
                          implicit rds: Writes[A],
                          ec: ExecutionContext
                        ): Future[HttpResponse] = {
    super.doPost(url, body, useDummyIPInTrueClientIPHeader(headers))(rds, ec)
  }

  // By default HTTP Verbs does not provide access to the pure response body of a 4XX and we need it
  // An IP address needs to be injected because of the lockout mechanism
  override def doGet(url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] =
    super.doGet(url, useDummyIPInTrueClientIPHeader(headers))(ec).map { res =>
      res.status match {
        case 401 => throw Upstream4xxResponse(res.body, 401, 401, res.headers)
        case 409 => throw Upstream4xxResponse(res.body, 409, 409, res.headers)
        case _ => res
      }
    }(ec)

  override def doPut[A](url: String,
                        body: A,
                        headers: Seq[(String, String)])(implicit rds: Writes[A],
                                                        ec: ExecutionContext): Future[HttpResponse] = {
    super.doPut(url, body, headers)(rds, ec).map { res =>
      if (res.status == 400) throw new BadRequestException(res.body) else res
    }(ec)
  }

  override protected def configuration: Config = config.underlying
}
