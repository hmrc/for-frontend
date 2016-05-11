package aat.agentApi

import aat.AcceptanceTest
import models.serviceContracts.submissions._
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

    "A 401 Unauthorised response is returned" in {
      assert(res.status === 401)
      assert(res.body === jsonBody(s"""{"error": "invalid credentials: ${invalid.refNum} - ${invalid.postcode}", "numberOfRemainingTriesUntilIPLockout":4}"""))
    }
  }

  "When PUTting a submission using valid credentials" - {
    http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)

    "When the submission json is invalid" - {
      http.stubSubmission(valid.refNum, invalidSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(
        responseStatus = 400,
        responseJson = Some(invalidSubmissionError)
      ))

      val res = AgentApi.submit(valid.refNum, valid.postcode, invalidSubmission)

      "A 400 Bad Request response explaining the error is returned" in {
        assert(res.status === 400)
        assert(res.body === Json.prettyPrint(invalidSubmissionError))
      }
    }

    "When the submission json is valid" - {
      http.stubSubmission(valid.refNum, validSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(200, responseJson = Some(Json.parse("{}"))))

      val res = AgentApi.submit(valid.refNum, valid.postcode, validSubmission)

      "A 200 Ok response is returned" in {
        assert(res.status === 200)
      }
    }
  }

  "When PUTting a submission using credentials that have already been used" - {
    http.stubConflictingCredentials(conflicting.ref1, conflicting.ref2, conflicting.postcode)

    val res = AgentApi.submit(conflicting.refNum, conflicting.postcode, validSubmission)

    "A 409 Conflict response explaining the error is returned" in {
      assert(res.status === 409)
      assert(res.body === jsonBody("{\"error\":\"Duplicate submission. 1234567890\"}"))
    }
  }

  "When PUTting a submission using a locked out IP" - {
    http.stubIPLockout(lockedOut.ref1, lockedOut.ref2, lockedOut.postcode)

    val res = AgentApi.submit(lockedOut.refNum, lockedOut.postcode, validSubmission)

    "A 401 Unauthorised response explaining that the IP is locked out is returned" in {
      assert(res.status === 401)
      assert(res.body === jsonBody("""{"error":"This IP address is locked out for 24 hours due to too many failed login attempts"}"""))
    }
  }

  private def jsonBody(body: String) = Json.prettyPrint(Json.parse(body))
}

private object AgentApi extends FutureAwaits with DefaultAwaitTimeout {
  import play.api.Play.current

  def submit(refNum: String, postcode: String, submission: JsValue) = {
    await(WS.url(s"http://localhost:9521/sending-rental-information/api/submit/$refNum/$postcode").put(submission))
  }
}

private object TestData {
  import models._

  val valid = Credentials("9999000", "123", "AA11+1AA")
  val invalid = Credentials("1234567", "890", "AA11+1AA")
  val conflicting = Credentials("0000999", "321", "AA11+1AA")
  val lockedOut = Credentials("9999999", "999", "AA11+1AA")

  val validSubmission: JsValue = Json.toJson(
    Submission(
      Some(PropertyAddress(true, None)),
      Some(CustomerDetails("bob", UserTypeOwnersAgent, ContactTypePhone, ContactDetails(Some("1"), None, None), None, None, None)),
      Some(TheProperty("shop", OccupierTypeNobody, None, None, true, None)),
      Some(Sublet(false, Nil)),
      None, None, None, None, None, None, None, None, None, None, Some("9999000123"))
  )
  val invalidSubmission: JsValue = Json.parse("{}")

  val invalidSubmissionError = Json.parse(
    """{ "errors":[ {"field":"", "error":"object has missing required properties ([ \"alterations\",\"customerDetails\",\"incentives\",\"landlord\",\"lease\", \"otherFactors\",\"propertyAddress\",\"referenceNumber\",\"rent\", \"rentAgreement\",\"rentIncludes\",\"rentReviews\",\"responsibilities\", \"sublet\",\"theProperty\"])", "schemaUsed":"defaultSchema.json"}] }""")
}

private case class Credentials(ref1: String, ref2: String, postcode: String) {
  def refNum = ref1 + ref2
}
