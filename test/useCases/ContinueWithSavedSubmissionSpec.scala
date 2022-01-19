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

package useCases

import _root_.utils.UnitTest
import connectors.Document
import models.journeys.SummaryPage
import models.pages.Summary
import org.joda.time.DateTime
import uk.gov.hmrc.http.{HeaderCarrier,Authorization}

import scala.concurrent.ExecutionContext.Implicits.global

class ContinueWithSavedSubmissionSpec extends UnitTest {

   "Continue with saved submission" when {
     val pwd = "anicepassword"
     val ref = "11122233344"
     val now = new DateTime(2015, 3, 5, 12, 25)
     val doc = Document(ref, DateTime.now, saveForLaterPassword = Some(pwd), journeyResumptions = Seq(now.minusDays(1)))
     val tok = "BASIC abcdefg=="
     implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(tok)))
     val sum = Summary(ref, DateTime.now, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)

    "a document has been saved and the passwords match" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val c = ContinueWithSavedSubmission(
        respondWith(tok, ref)(Some(doc)), set(updated = _), doc => sum, sum => SummaryPage, () => now
      ) _
      val r = await(c(pwd, ref))

      "return the next page to go to" in {
        assert(r === PasswordsMatch(SummaryPage))
      }

      "load the saved document into the current session updating the journey resumptions with the current date and time" in {
        val docWithNowResumption = doc.copy(journeyResumptions = doc.journeyResumptions :+ now)
        assert(updated === ((hc, ref, docWithNowResumption)))
      }
    }

    "a document has been saved but the passwords do no match" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val c = ContinueWithSavedSubmission(
        respondWith(tok, ref)(Some(doc)), set(updated = _), doc => sum, sum => SummaryPage, () => now
      ) _

      val r = await(c("invalidPassword", ref))

      "return a failed login" in {
        assert(r === IncorrectPassword)
      }

      "not update the document in the current session" in {
        assert(updated === null)
      }
    }

    "there is no matching document" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val c = ContinueWithSavedSubmission(none, set(updated = _), doc => sum, sum => SummaryPage, () => now) _
      val r = await(c(pwd, ref))

      "a retrieval error is returned" in {
        assert(r === ErrorRetrievingSavedDocument)
      }

      "the current session is not modified" in {
        assert(updated === null)
      }
    }
  }
}
