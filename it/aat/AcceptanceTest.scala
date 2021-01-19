/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import connectors.ForHttp
import models.FORLoginResponse
import models.serviceContracts.submissions.Address
import org.scalatest.{BeforeAndAfterAll, FreeSpec, FreeSpecLike, Matchers}
import org.scalatestplus.play.guice._
import play.api.Play
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HeaderCarrier, _}
import play.api.inject.bind
import javax.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

trait AcceptanceTest extends FreeSpec with Matchers with GuiceOneServerPerSuite {
  private lazy val testConfigs = Map("auditing.enabled" -> false, "agentApi.testAccountsOnly" -> true)

  def http: TestHttpClient = app.injector.instanceOf[ForHttp].asInstanceOf[TestHttpClient]

  override lazy val port = 9521

  override def fakeApplication() = new GuiceApplicationBuilder()
    .configure(testConfigs)
    .overrides(
      bind[ForHttp].to[TestHttpClient].in[Singleton]
    )
    .build()
}

class TestHttpClient extends ForHttp {
  import views.html.helper.urlEncode

  override protected def configuration: Option[Config] = None

  private val baseForUrl = "http://localhost:9522/for"
  type Headers = Seq[(String, String)]

  private var stubbedGets: Seq[(String, Headers, HttpResponse)] = Nil // scalastyle:ignore
  private var stubbedPuts: Seq[(String, Any, Headers, HttpResponse)] = Nil // scalastyle:ignore

  def stubGet(url: String, headers: Seq[(String, String)], response: HttpResponse) = {
    stubbedGets :+= (url, headers, response)
  }

  def stubPut[A](url: String, body: A, headers: Seq[(String, String)], response: HttpResponse) = {
    stubbedPuts :+= (url, body, headers, response)
  }

  def stubValidCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify", Nil, HttpResponse(
      responseStatus = 200,
      responseJson = Some(Json.toJson(FORLoginResponse("token", Address("1", None, None, "AA11 1AA"))))
    ))
  }

  def stubInvalidCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify", Nil, HttpResponse(
      responseStatus = 401,
      responseJson = Some(Json.parse("""{"numberOfRemainingTriesUntilIPLockout":4}"""))))
  }

  def stubConflictingCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify", Nil, HttpResponse(
      responseStatus = 409,
      responseJson = Some(Json.parse("{\"error\":\"Duplicate submission. 1234567890\"}"))))
  }

  def stubIPLockout(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify", Nil, HttpResponse(
      responseStatus = 401,
      responseJson = Some(Json.parse("""{"numberOfRemainingTriesUntilIPLockout":0}"""))))
  }

  def stubInternalServerError(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/${urlEncode(postcode)}/verify", Nil, HttpResponse(
      responseStatus = 500
    ))
  }

  def stubSubmission(refNum: String, submission: JsValue, headers: Seq[(String, String)], response: HttpResponse) = {
    stubPut(s"$baseForUrl/submissions/$refNum", submission, headers, response)
  }

  override def doGet(url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    stubbedGets.find(x => x._1 == url && x._2.forall(y => hc.headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, res)) => Future.successful(res)
      case _ => throw new HttpRequestNotStubbed(url, hc)
    }
  }

  override def doPut[A](url: String, body: A, headers: Seq[(String, String)])(implicit rds: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    stubbedPuts.find(x => x._1 == url && x._2 == body && x._3.forall(y => hc.headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, _, res)) => Future.successful(res)
      case _ => throw new HttpRequestNotStubbed(url, hc)
    }
  }

  override def doPutString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    Thread.sleep(100000000l)
    Future.failed(new RuntimeException("stupid error"))
  }

  override protected def actorSystem: ActorSystem = Play.current.actorSystem

  override val hooks: Seq[HttpHook] = Seq.empty[HttpHook]

  override def wsClient: WSClient = ???
}

class HttpRequestNotStubbed[A](url: String, hc: HeaderCarrier, data: Option[A] = None)
  extends Exception(s"Request not stubbed: $url - ${hc.headers} ${data.map { d => s"- $d" }.getOrElse("")}")