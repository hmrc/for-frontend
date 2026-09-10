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

package useCases

import connectors.Document
import crypto.MongoHasher
import models.journeys.SummaryPage
import models.pages.Summary
import play.api.Configuration
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import util.DateUtil.nowInUK
import utils.BehaviourVerification

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.Future

class ContinueWithSavedSubmissionSpec extends BaseSpec with BehaviourVerification:

  implicit private val mongoHasher: MongoHasher =
    MongoHasher(Configuration("oneway.hash.key" -> "UkFMRCBTYXZlRm9yTGF0ZXIgcGFzd29yZCB2ZXJ5IGNvb2wgYW5kIHNlY3JldCBvbmUgd2F5IGhhc2gga2V5"))

  "Continue with saved submission" when {
    val pwd = "anicepassword"
    val ref = "11122233344"
    val now = ZonedDateTime.of(2015, 3, 5, 12, 25, 0, 0, ZoneOffset.UTC)
    val doc = Document(ref, nowInUK, saveForLaterPassword = Some(mongoHasher.hash(pwd)), journeyResumptions = Seq(now.minusDays(1)))
    val tok = "BASIC abcdefg=="
    val sum = Summary(ref, nowInUK, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)

    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(tok)))

    "a document has been saved and the passwords match" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document)                           = null
      val c: (SaveForLaterPassword, ReferenceNumber) => Future[SaveForLaterLoginResult] =
        ContinueWithSavedSubmission.apply(
          respondWith(tok, ref)(Some(doc)),
          set(updated = _),
          _ => sum,
          _ => SummaryPage,
          () => now
        )

      val r = c.apply(pwd, ref).futureValue

      "return the next page to go to" in {
        r shouldBe PasswordsMatch(SummaryPage)
      }

      "load the saved document into the current session updating the journey resumptions with the current date and time" in {
        val docWithNowResumption = doc.copy(saveForLaterPassword = None, journeyResumptions = doc.journeyResumptions :+ now)
        updated shouldBe (hc, ref, docWithNowResumption)
      }
    }

    "a document has been saved but the passwords do no match" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document)                           = null
      val c: (SaveForLaterPassword, ReferenceNumber) => Future[SaveForLaterLoginResult] =
        ContinueWithSavedSubmission.apply(
          respondWith(tok, ref)(Some(doc)),
          set(updated = _),
          _ => sum,
          _ => SummaryPage,
          () => now
        )

      val r = c.apply("invalidPassword", ref).futureValue

      "return a failed login" in {
        r shouldBe IncorrectPassword
      }

      "not update the document in the current session" in {
        updated shouldBe null
      }
    }

    "there is no matching document" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document)                           = null
      val c: (SaveForLaterPassword, ReferenceNumber) => Future[SaveForLaterLoginResult] =
        ContinueWithSavedSubmission(none, set(updated = _), _ => sum, _ => SummaryPage, () => now)

      val r = c.apply(pwd, ref).futureValue

      "a retrieval error is returned" in {
        r shouldBe ErrorRetrievingSavedDocument
      }

      "the current session is not modified" in {
        updated shouldBe null
      }
    }
  }
