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

package aat

import connectors.ForHttp
import models.FORLoginResponse
import models.serviceContracts.submissions.Address
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.play.guice.*
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx

import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object AcceptanceTest {
  val noHeaders: Map[String, Seq[String]] = Map.empty
}

trait AcceptanceTest extends AnyFlatSpec with should.Matchers with GuiceOneServerPerSuite {
  private lazy val testConfigs = Map("auditing.enabled" -> false, "agentApi.testAccountsOnly" -> true)

  val noHeaders: Map[String, Seq[String]] = AcceptanceTest.noHeaders

  def http: TestHttpClient = app.injector.instanceOf[ForHttp].asInstanceOf[TestHttpClient]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(testConfigs)
    .overrides(
      bind[ForHttp].to[TestHttpClient].in[Singleton]
    )
    .build()
}

class TestHttpClient extends ForHttp:
  import AcceptanceTest.noHeaders
  import views.html.helper.urlEncode

  private val baseForUrl = "http://localhost:9522/for"
  type Headers = Seq[(String, String)]

  private var stubbedGets: Seq[(String, Headers, HttpResponse)]      = Nil
  private var stubbedPuts: Seq[(String, Any, Headers, HttpResponse)] = Nil

  def stubGet(url: String, headers: Seq[(String, String)], response: HttpResponse): Unit =
    stubbedGets :+= (url, headers, response)

  def stubPut[A](url: String, body: A, headers: Seq[(String, String)], response: HttpResponse): Unit =
    stubbedPuts :+= (url, body, headers, response)

  def stubValidCredentials(ref1: String, ref2: String, postcode: String): Unit =
    stubGet(
      s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify",
      Nil,
      HttpResponse(
        OK,
        Json.toJson(FORLoginResponse("token", Address("1", None, None, "AA11 1AA"))),
        noHeaders
      )
    )

  def stubInvalidCredentials(ref1: String, ref2: String, postcode: String): Unit =
    stubGet(
      s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify",
      Nil,
      HttpResponse(
        NOT_FOUND,
        Json.parse("""{"numberOfRemainingTriesUntilIPLockout":4}"""),
        noHeaders
      )
    )

  def stubConflictingCredentials(ref1: String, ref2: String, postcode: String): Unit =
    stubGet(
      s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify",
      Nil,
      HttpResponse(
        409,
        Json.parse("{\"error\":\"Duplicate submission. 1234567890\"}"),
        noHeaders
      )
    )

  def stubIPLockout(ref1: String, ref2: String, postcode: String): Unit =
    stubGet(
      s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify",
      Nil,
      HttpResponse(
        401,
        Json.parse("""{"numberOfRemainingTriesUntilIPLockout":0}"""),
        noHeaders
      )
    )

  def stubInternalServerError(ref1: String, ref2: String, postcode: String): Unit =
    stubGet(
      s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify",
      Nil,
      HttpResponse(
        500,
        ""
      )
    )

  def stubSubmission(refNum: String, submission: JsValue, headers: Seq[(String, String)], response: HttpResponse): Unit =
    stubPut(s"$baseForUrl/submissions/$refNum", submission, headers, response)

  override def get[A](
    url: String,
    queryParams: Seq[(String, String)],
    headers: Seq[(String, String)] = Seq.empty
  )(implicit
    rds: Reads[A],
    hc: HeaderCarrier
  ): Future[A] =
    stubbedGets.find(x => x._1 == url && x._2.forall(y => headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, response)) =>
        Future.successful(response).map { r =>
          if is2xx(r.status) then Json.parse(r.body).as[A]
          else throw UpstreamErrorResponse(r.body, r.status, r.status, r.headers)
        }
      case _                      => throw new HttpRequestNotStubbed(url, headers)
    }

  def put[I](
    url: String,
    body: I,
    headers: Seq[(String, String)] = Seq.empty
  )(implicit
    wts: Writes[I],
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    stubbedPuts.find(x => x._1 == url && x._2 == body && x._3.forall(y => headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, _, response)) => Future.successful(response)
      case _                         => throw new HttpRequestNotStubbed(url, headers)
    }

  override def postForm(url: String, body: Map[String, Seq[String]], headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def post[I](url: String, body: I, headers: Seq[(String, String)])(implicit wts: Writes[I], hc: HeaderCarrier): Future[HttpResponse] = ???

class HttpRequestNotStubbed[A](url: String, headers: Seq[(String, String)], data: Option[A] = None)
  extends Exception(s"Request not stubbed: $url - $headers ${data.map(d => s"- $d").getOrElse("")}")
