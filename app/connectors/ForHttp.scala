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
import config.ForConfig
import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedForm
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HeaderNames.trueClientIp
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/*
with HttpDelete with WSDelete  with AppName with RunMode
 */
@ImplementedBy(classOf[ForHttpClient])
trait ForHttp:

  def postForm(
    url: String,
    body: Map[String, Seq[String]],
    headers: Seq[(String, String)] = Seq.empty
  )(implicit hc: HeaderCarrier
  ): Future[HttpResponse]

  def post[I](
    url: String,
    body: I,
    headers: Seq[(String, String)] = Seq.empty
  )(implicit
    wts: Writes[I],
    hc: HeaderCarrier
  ): Future[HttpResponse]

  def put[I](
    url: String,
    body: I,
    headers: Seq[(String, String)] = Seq.empty
  )(implicit
    wts: Writes[I],
    hc: HeaderCarrier
  ): Future[HttpResponse]

  def get[A](
    url: String,
    queryParams: Seq[(String, String)],
    headers: Seq[(String, String)] = Seq.empty
  )(implicit
    rds: Reads[A],
    hc: HeaderCarrier
  ): Future[A]

@Singleton
class ForHttpClient @Inject() (
  forConfig: ForConfig,
  httpClientV2: HttpClientV2
)(implicit ec: ExecutionContext
) extends ForHttp:

  private val useDummyIp = forConfig.useDummyIp

  private def useDummyIPInTrueClientIPHeader(headers: Seq[(String, String)]): Seq[(String, String)] =
    if useDummyIp then (trueClientIp, "") +: headers.filterNot(x => x._1.toLowerCase == trueClientIp.toLowerCase)
    else headers

  override def postForm(
    url: String,
    body: Map[String, Seq[String]],
    headers: Seq[(String, String)]
  )(implicit hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClientV2.post(url"$url")
      .withBody(body)
      .setHeader(useDummyIPInTrueClientIPHeader(headers)*)
      .execute[HttpResponse]

  override def post[I](
    url: String,
    body: I,
    headers: Seq[(String, String)]
  )(implicit
    wts: Writes[I],
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClientV2.post(url"$url")
      .withBody(Json.toJson(body))
      .setHeader(useDummyIPInTrueClientIPHeader(headers)*)
      .execute[HttpResponse]

  override def put[I](
    url: String,
    body: I,
    headers: Seq[(String, String)]
  )(implicit
    wts: Writes[I],
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClientV2.put(url"$url")
      .withBody(Json.toJson(body))
      .setHeader(useDummyIPInTrueClientIPHeader(headers)*)
      .execute[HttpResponse]
      .map(r => if r.status == 400 then throw new BadRequestException(r.body) else r)

  override def get[A](
    url: String,
    queryParams: Seq[(String, String)],
    headers: Seq[(String, String)]
  )(implicit
    rds: Reads[A],
    hc: HeaderCarrier
  ): Future[A] =
    httpClientV2.get(url"$url")
      .setHeader(useDummyIPInTrueClientIPHeader(headers)*)
      .execute[HttpResponse]
      .map { r =>
        if is2xx(r.status) then Json.parse(r.body).as[A]
        else throw UpstreamErrorResponse(r.body, r.status, r.status, r.headers)
      }
