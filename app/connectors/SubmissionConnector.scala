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

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import models.serviceContracts.submissions.{NotConnectedSubmission, Submission}
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import play.api.mvc.{ResponseHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpReads, HttpResponse, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class HodSubmissionConnector @Inject() (config: ServicesConfig,
                                        http: ForHttp)(implicit ec: ExecutionContext) extends SubmissionConnector  {
  lazy val serviceUrl = config.baseUrl("for-hod-adapter")

  implicit def httpReads: HttpReads[HttpResponse] = (method: String, url: String, response: HttpResponse) => {
    response.status match {
      case 400 => throw new BadRequestException(response.body)
      case 401 => throw new Upstream4xxResponse(response.body, 401, 401, response.headers)
      case 409 => throw new Upstream4xxResponse(response.body, 409, 409, response.headers)
      case _ => HttpReads.Implicits.readRaw.read(method, url, response)
    }
  }

  def submit(refNum: String, submission: Submission)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"$serviceUrl/for/submissions/$refNum", submission).map(_ => ())
  }

  def submit(refNum: String, submission: JsValue)(implicit hc: HeaderCarrier): Future[Result] = {
    http.PUT(s"$serviceUrl/for/submissions/$refNum", submission) map { r =>
      Result(ResponseHeader(r.status), HttpEntity.Streamed(Source.single(ByteString(Option(r.body).getOrElse(""))), None, None))
    }
  }

  override def submitNotConnected(refNumber: String, submission: NotConnectedSubmission)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"$serviceUrl/for/submissions/notConnected/${submission.id}", submission).map(_ => ())
  }

}

@ImplementedBy(classOf[HodSubmissionConnector])
trait SubmissionConnector {
  def submit(refNum: String, submisson: Submission)(implicit hc: HeaderCarrier): Future[Unit]
  def submitNotConnected(refNumber: String, submission: NotConnectedSubmission)(implicit hc: HeaderCarrier): Future[Unit]
}
