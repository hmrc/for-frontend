package aat

import config.ForGlobal
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}
import play.api.libs.json.{JsValue, Writes}
import play.api.test.{FakeApplication, TestServer}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.{HeaderCarrier, _}

import scala.concurrent.Future

trait AcceptanceTest extends FreeSpecLike with Matchers with BeforeAndAfterAll {

  val http = new TestHttpClient()

  val port = 9521

  private var server: TestServer = null

  protected def startApp() = {
    val global = new ForGlobal {
      override lazy val forHttp = http
    }
    val app = FakeApplication(withGlobal = Some(global))
    server = TestServer(port, app)
    server.start()
  }

  override def afterAll() = {
    server.stop()
  }
}

class TestHttpClient extends HttpGet with HttpPut with HttpPost with HttpDelete {
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
    stubGet(s"$baseForUrl/$ref1/$ref2/$postcode/verify", Nil, HttpResponse(200))
  }

  def stubInvalidCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"$baseForUrl/$ref1/$ref2/$postcode/verify", Nil, HttpResponse(401))
  }

  def stubSubmission(refNum: String, submission: JsValue, headers: Seq[(String, String)], response: HttpResponse) = {
    stubPut(s"$baseForUrl/submissions/$refNum", submission, headers, response)
  }

  override protected def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    stubbedGets.find(x => x._1 == url && x._2.forall(y => hc.headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, res)) => Future.successful(res)
      case _ => throw new HttpRequestNotStubbed(url, hc)
    }
  }

  override protected def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    stubbedPuts.find(x => x._1 == url && x._2 == body && x._3.forall(y => hc.headers.exists(h => h._1 == y._1 && h._2 == y._2))) match {
      case Some((_, _, _, res)) => Future.successful(res)
      case _ => throw new HttpRequestNotStubbed(url, hc)
    }
  }

  override protected def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override protected def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override protected def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???

  override protected def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override protected def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override val hooks: Seq[HttpHook] = Nil
}

class HttpRequestNotStubbed(url: String, hc: HeaderCarrier) extends Exception(s"Request not stubbed: $url - ${hc.headers}")