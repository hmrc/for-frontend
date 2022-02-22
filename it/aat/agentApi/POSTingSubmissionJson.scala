/*
 * Copyright 2022 HM Revenue & Customs
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

package aat.agentApi

import aat.AcceptanceTest
import models.serviceContracts.submissions._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{HeaderNames, HttpResponse}

class POSTingSubmissionJson extends AcceptanceTest {
  import TestData._

  "When POSTting a submission without a valid accept header" should
    "A formatted 406 Not Acceptable response is returned" in {
      http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)
      val res = AgentApi.submitWithoutAcceptHeader(valid.refNum, valid.postcode, validSubmission)

      assert(res.status === 406)
      assert(res.body === Json.parse("""{"code": "ACCEPT_HEADER_INVALID", "message": "The header Accept is missing or invalid"}""").toString)
  }

  "When POSTting a submission using invalid credentials" should
    "A formatted 401 Unauthorised response is returned" in {
      http.stubInvalidCredentials(invalid.ref1, invalid.ref2, invalid.postcode)
      val res = AgentApi.submit(invalid.refNum, invalid.postcode, validSubmission)

      assert(res.status === 401)
      assert(res.body === Json.parse(
        s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: ${invalid.refNum} - ${invalid.postcode}; 4 tries remaining until IP lockout"}""").toString
      )
  }

  "When POSTting a submission using valid credentials" should
    "When the submission json is invalid, a formatted 400 Bad Request response explaining the error is returned" in {
      http.stubValidCredentials(valid.ref1, valid.ref2, valid.postcode)

      http.stubSubmission(valid.refNum, invalidSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(
        400, Json.parse(submissionErrorFromHodAdapter), noHeaders
      ))

      val res = AgentApi.submit(valid.refNum, valid.postcode, invalidSubmission)

      assert(res.status === 400)
      assert(res.body === Json.parse(s"""{"code": "INVALID_SUBMISSION", "message": $invalidSubmissionError}""").toString)
    }

  "When the submission json is valid" should
      "A 200 Ok response is returned" in {
        http.stubSubmission(valid.refNum, validSubmission, Seq(HeaderNames.authorisation -> "token"), HttpResponse(
          200, Json.parse("{}"), noHeaders
        ))

        val res = AgentApi.submit(valid.refNum, valid.postcode, validSubmission)

        assert(res.status === 200)
        assert(res.body === Json.parse(s"""{"code": "VALID_SUBMISSION", "message": "Accepted submission with reference ${valid.refNum}"}""").toString)
  }

  "When POSTting a submission and there is an internal server error" should
    "A formatted 500 Internal Server Error response is returned" in {
      http.stubInternalServerError(internalServerError.ref1, internalServerError.ref2, internalServerError.postcode)

      val res = AgentApi.submit(internalServerError.refNum, internalServerError.postcode, validSubmission)

      assert(res.status === 500)
      assert(res.body === Json.parse("""{"code": "INTERNAL_SERVER_ERROR", "message": "Internal server error"}""").toString)
  }

  "When POSTting a submission using credentials that have already been used" should
    "A formatted 409 Conflict response explaining the error is returned" in {
      http.stubConflictingCredentials(conflicting.ref1, conflicting.ref2, conflicting.postcode)

      val res = AgentApi.submit(conflicting.refNum, conflicting.postcode, validSubmission)

      assert(res.status === 409)
      assert(res.body === Json.parse(s"""{"code": "DUPLICATE_SUBMISSION", "message": "A submission already exists for ${conflicting.refNum}"}""").toString)
  }

  "When POSTting a submission using a locked out IP" should
    "A formatted 401 Unauthorised response explaining that the IP is locked out is returned" in {
      http.stubIPLockout(lockedOut.ref1, lockedOut.ref2, lockedOut.postcode)

      val res = AgentApi.submit(lockedOut.refNum, lockedOut.postcode, validSubmission)

      assert(res.status === 401)
      assert(res.body === Json.parse("""{"code": "IP_LOCKOUT", "message":"This IP address is locked out for 24 hours due to too many failed login attempts"}""").toString)
  }

  "When POSTting a submission using a non-test account when only test accounts are allowed" should
    "A formatted 401 Unauthorised response explaining that the credentials are invalid is returned" in {
      http.stubValidCredentials(nonTestAccount.ref1, nonTestAccount.ref2, nonTestAccount.postcode)

      val res = AgentApi.submit(nonTestAccount.refNum, nonTestAccount.postcode, validSubmission)

      assert(res.status === 401)
      assert(res.body === Json.parse(
        s"""{"code": "INVALID_CREDENTIALS", "message": "Invalid credentials: ${nonTestAccount.refNum} - ${nonTestAccount.postcode}"}""").toString
      )
  }

  private object AgentApi extends FutureAwaits with DefaultAwaitTimeout {
    val WS = app.injector.instanceOf(classOf[WSClient])

    def submit(refNum: String, postcode: String, submission: JsValue) =
      await(WS.url(s"http://localhost:$port/sending-rental-information/api/submit/$refNum/$postcode")
        .addHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
        .addHttpHeaders("X-Requested-With" -> "IT-Test")
        .post(submission))

    def submitWithoutAcceptHeader(refNum: String, postcode: String, submission: JsValue) =
      await(WS.url(s"http://localhost:$port/sending-rental-information/api/submit/$refNum/$postcode")
        .addHttpHeaders("X-Requested-With" -> "IT-Test")
        .post(submission))
  }
}

private object TestData {
  import models._

  val valid = Credentials("9999000", "001", "AA11+1AA")
  val invalid = Credentials("9999000", "002", "AA11+1AA")
  val conflicting = Credentials("9999000", "003", "AA11+1AA")
  val lockedOut = Credentials("9999000", "004", "AA11+1AA")
  val internalServerError = Credentials("9999000", "005", "AA11+1AA")
  val nonTestAccount = Credentials("1234567", "890", "AA11+1AA")

  val validSubmission: JsValue = Json.toJson(
    Submission(
      None,
      Some(CustomerDetails("bob", UserTypeOwnersAgent, ContactDetails("01234567890", "abc@mailinator.com"))),
      Some(TheProperty("shop", OccupierTypeNobody, None, None, true, None,None)),
      Some(Sublet(false, Nil)),
      None, None, None, None, None, None, None, None, None, None, Some("9999000123"))
  )
  val invalidSubmission: JsValue = Json.parse("{}")

  val invalidSubmissionError =
    """[
      |{"field":"",
      |"error":"object has missing required properties
      |([ \"alterations\",\"customerDetails\",\"incentives\",\"landlord\",\"lease\", \"otherFactors\",
      |\"propertyAddress\",\"referenceNumber\",\"rent\", \"rentAgreement\",\"rentIncludes\",\"rentReviews\",
      |\"responsibilities\", \"sublet\",\"theProperty\"])", "schemaUsed":"defaultSchema.json"}
      |]""".stripMargin.replaceAll("\n", "")

  val submissionErrorFromHodAdapter = s"""{"errors": $invalidSubmissionError}"""
}

private case class Credentials(ref1: String, ref2: String, postcode: String) {
  def refNum = ref1 + ref2
}
