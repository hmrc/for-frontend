package aat.agentApi

import aat.AcceptanceTest
import play.api.libs.json.{JsNull, JsValue}
import play.api.libs.ws.WS
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

class PUTtingSubmissionJson extends AcceptanceTest {
  import TestData._

  startApp()

  "When PUTting a submission using invalid credentials" - {
    http.stubInvalidCredentials(invalid.ref1, invalid.ref2, invalid.postcode)
    val res = AgentApi.submit(invalid.refNum, invalid.postcode, submissionJson)

    "A 401 Unauthorised response explaining that the credentials are invalid is returned" in {
      assert(res.status === 401)
      assert(res.body === "")
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

  val submissionJson: JsValue = JsNull
}

private case class Credentials(ref1: String, ref2: String, postcode: String) {
  def refNum = ref1 + ref2
}
