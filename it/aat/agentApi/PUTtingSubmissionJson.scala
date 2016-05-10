package aat.agentApi

import aat.AcceptanceTest
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.libs.ws.WS
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.play.http.{HeaderNames, HttpResponse}

class PUTtingSubmissionJson extends AcceptanceTest {
  import TestData._

  startApp()

  "When PUTting a submission using invalid credentials" - {
    http.stubInvalidCredentials(invalid.ref1, invalid.ref2, invalid.postcode)
    val res = AgentApi.submit(invalid.refNum, invalid.postcode, validSubmission)

    "A 401 Unauthorised response" in {
      assert(res.status === 401)
    }
  }

  "When PUTting a submission using valid credentials" - {
    http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)

    "When the submission json is invalid" - {
      http.stubSubmission(valid.refNum, invalidSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(
        responseStatus = 400
      ))

      val res = AgentApi.submit(valid.refNum, valid.postcode, invalidSubmission)

      "A 400 Bad Request response is returned" in {
        assert(res.status === 400)
      }
    }

    "When the submission json is valid" - {
      http.stubSubmission(valid.refNum, validSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(200))

      val res = AgentApi.submit(valid.refNum, valid.postcode, validSubmission)

      "A 200 Ok response is returned" in {
        assert(res.status === 200)
      }
    }

    
  }
}

private object AgentApi extends FutureAwaits with DefaultAwaitTimeout {
  import play.api.Play.current

  def submit(refNum: String, postcode: String, submission: JsValue) = {
    await(WS.url(s"http://localhost:9521/sending-rental-information/api/submit/$refNum/$postcode").put(submission))
  }
}

private object TestData {
  val valid = Credentials("9999000", "123", "AA11+1AA")
  val invalid = Credentials("1234567", "890", "AA11+1AA")

  val validSubmission: JsValue = JsNull
  val invalidSubmission: JsValue = Json.parse("{}")
}

private case class Credentials(ref1: String, ref2: String, postcode: String) {
  def refNum = ref1 + ref2
}
