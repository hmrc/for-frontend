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

package connectors

import config.ForConfig
import models.serviceContracts.submissions.Submission
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import play.api.mvc.{ResponseHeader, Result}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object SubmissionConnector extends SubmissionConnector with ServicesConfig {
  lazy val serviceUrl = baseUrl("for-hod-adapter")
  val http = ForConfig.http

  def submit(refNum: String, submission: Submission)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"$serviceUrl/for/submissions/$refNum", submission).map(_ => ())
  }

  def submit(refNum: String, submission: JsValue)(implicit hc: HeaderCarrier): Future[Result] = {
    http.PUT(s"$serviceUrl/for/submissions/$refNum", submission) map { r =>
      Result(ResponseHeader(r.status), Enumerator(Option(r.body).getOrElse("").getBytes))
    }
  }
}

trait SubmissionConnector {
  def submit(refNum: String, submisson: Submission)(implicit hc: HeaderCarrier): Future[Unit]
}
