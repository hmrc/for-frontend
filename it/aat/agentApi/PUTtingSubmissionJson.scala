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

  "When PUTting a submission without a valid accept header" - {
    http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)
    val res = AgentApi.submitWithoutAcceptHeader(valid.refNum, valid.postcode, validSubmission)

    "A formatted 406 Not Acceptable response is returned" in {
      assert(res.status === 406)
      assert(res.body === jsonBody("""{"code": "ACCEPT_HEADER_INVALID", "message": "The header Accept is missing or invalid"}"""))
    }
  }

  "When PUTting a submission using invalid credentials" - {
    http.stubInvalidCredentials(invalid.ref1, invalid.ref2, invalid.postcode)
    val res = AgentApi.submit(invalid.refNum, invalid.postcode, validSubmission)

    "A formatted 401 Unauthorised response is returned" in {
      assert(res.status === 401)
      assert(res.body === jsonBody(
        s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: ${invalid.refNum} - ${invalid.postcode}; 4 tries remaining until IP lockout"}""")
      )
    }
  }

  "When PUTting a submission using valid credentials" - {
    http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)

    "When the submission json is invalid" - {
      http.stubSubmission(valid.refNum, invalidSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(
        responseStatus = 400,
        responseJson = Some(Json.parse(s"{$invalidSubmissionError}"))
      ))

      val res = AgentApi.submit(valid.refNum, valid.postcode, invalidSubmission)

      "A formatted 400 Bad Request response explaining the error is returned" in {
        assert(res.status === 400)
        assert(res.body === jsonBody(s"""{"code": "INVALID_SUBMISSION", $invalidSubmissionError}"""))
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

  "When PUTting a submission and there is an internal server error" - {
    http.stubInternalServerError(internalServerError.ref1, internalServerError.ref2, internalServerError.postcode)

    val res = AgentApi.submit(internalServerError.refNum, internalServerError.postcode, validSubmission)

    "A formatted 500 Internal Server Error response is returned" in {
      assert(res.status === 500)
      assert(res.body === jsonBody("""{"code": "INTERNAL_SERVER_ERROR", "message": "Internal server error"}"""))
    }
  }

  "When PUTting a submission using credentials that have already been used" - {
    http.stubConflictingCredentials(conflicting.ref1, conflicting.ref2, conflicting.postcode)

    val res = AgentApi.submit(conflicting.refNum, conflicting.postcode, validSubmission)

    "A formatted 409 Conflict response explaining the error is returned" in {
      assert(res.status === 409)
      assert(res.body === jsonBody(s"""{"code": "DUPLICATE_SUBMISSION", "message": "A submission already exists for ${conflicting.refNum}"}"""))
    }
  }

  "When PUTting a submission using a locked out IP" - {
    http.stubIPLockout(lockedOut.ref1, lockedOut.ref2, lockedOut.postcode)

    val res = AgentApi.submit(lockedOut.refNum, lockedOut.postcode, validSubmission)

    "A formatted 401 Unauthorised response explaining that the IP is locked out is returned" in {
      assert(res.status === 401)
      assert(res.body === jsonBody("""{"code": "IP_LOCKOUT", "message":"This IP address is locked out for 24 hours due to too many failed login attempts"}"""))
    }
  }

  private def jsonBody(body: String) = Json.prettyPrint(Json.parse(body))
}

private object AgentApi extends FutureAwaits with DefaultAwaitTimeout {
  import play.api.Play.current

  def submit(refNum: String, postcode: String, submission: JsValue) = {
    await(WS
      .url(s"http://localhost:9521/sending-rental-information/api/submit/$refNum/$postcode")
      .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
      .put(submission)
    )
  }

  def submitWithoutAcceptHeader(refNum: String, postcode: String, submission: JsValue) = {
    await(WS
      .url(s"http://localhost:9521/sending-rental-information/api/submit/$refNum/$postcode")
      .put(submission)
    )
  }
}

private object TestData {
  import models._

  val valid = Credentials("9999000", "123", "AA11+1AA")
  val invalid = Credentials("1234567", "890", "AA11+1AA")
  val conflicting = Credentials("0000999", "321", "AA11+1AA")
  val lockedOut = Credentials("9999999", "999", "AA11+1AA")
  val internalServerError = Credentials("7654321", "098", "AA11+1AA")
  val missingAcceptHeader = Credentials("1112223", "334", "AA11+1AA")

  val validSubmission: JsValue = Json.toJson(
    Submission(
      Some(PropertyAddress(true, None)),
      Some(CustomerDetails("bob", UserTypeOwnersAgent, ContactTypePhone, ContactDetails(Some("1"), None, None), None, None, None)),
      Some(TheProperty("shop", OccupierTypeNobody, None, None, true, None)),
      Some(Sublet(false, Nil)),
      None, None, None, None, None, None, None, None, None, None, Some("9999000123"))
  )
  val invalidSubmission: JsValue = Json.parse("{}")

  val invalidSubmissionError =
    """"errors":[
      |{"field":"",
      |"error":"object has missing required properties
      |([ \"alterations\",\"customerDetails\",\"incentives\",\"landlord\",\"lease\", \"otherFactors\",
      |\"propertyAddress\",\"referenceNumber\",\"rent\", \"rentAgreement\",\"rentIncludes\",\"rentReviews\",
      |\"responsibilities\", \"sublet\",\"theProperty\"])", "schemaUsed":"defaultSchema.json"}
      |]""".stripMargin.replaceAll("\n", "")
}

private case class Credentials(ref1: String, ref2: String, postcode: String) {
  def refNum = ref1 + ref2
}
