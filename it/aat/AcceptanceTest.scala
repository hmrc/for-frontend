package aat

import config.ForGlobal
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FreeSpecLike, Matchers}
import play.api.libs.json.{JsValue, Writes}
import play.api.test.{FakeApplication, TestServer}
import uk.gov.hmrc.play.http.{HeaderCarrier, _}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

import scala.concurrent.Future

trait AcceptanceTest extends FreeSpecLike with Matchers with BeforeAndAfterAll {

  val http: HttpGet with HttpPut with HttpPost with HttpDelete

  val port = 9521
  private var server: TestServer = null // scalastyle:ignore

  override def beforeAll() = {
    server = createServer()
    server.start()
  }

  private def createServer() = {
    val global = new ForGlobal {
      override val forHttp: WSGet with WSPut with WSPost with WSDelete = http
    }
    val app = FakeApplication(withGlobal = Some(global))
    TestServer(port, app)
  }

  override def afterAll() = {
    server.stop()
  }
}

class TestHttpClient extends HttpGet with HttpPut with HttpPost with HttpDelete {
  type Headers = Seq[(String, String)]

  private var stubbedGets: Seq[(String, Headers, HttpResponse)] = Nil // scalastyle:ignore
  private var stubbedPuts: Seq[(String, Object, Headers, HttpResponse)] = Nil // scalastyle:ignore

  def stubGet(url: String, headers: Seq[(String, String)], response: HttpResponse) = {
    stubbedGets :+= (url, headers, response)
  }

  def stubPut[A](url: String, body: A, headers: Seq[(String, String)], response: HttpResponse) = {
    stubbedPuts :+= (url, body, headers, response)
  }

  def stubValidCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"http://localhost:9522/$ref1/$ref2/$postcode/verify", Nil, HttpResponse(200))
  }

  def stubInvalidCredentials(ref1: String, ref2: String, postcode: String) = {
    stubGet(s"http://localhost:9522/$ref1/$ref2/$postcode/verify", Nil, HttpResponse(401))
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

  override val hooks: Seq[HttpHook] = _
}

class HttpRequestNotStubbed(url: String, hc: HeaderCarrier) extends Exception(s"Request not stubbed: $url - ${hc.headers}")