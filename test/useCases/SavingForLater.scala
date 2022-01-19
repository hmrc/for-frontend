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
import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import testutils._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class SaveInProgressSubmissionForLaterSpec extends UnitTest {

  "Save an in progress document for later" when {
    val pas = s"thisisapassorwd${Random.nextDouble}"
    val ref = "1111111222"
    val sid = java.util.UUID.randomUUID().toString
    val hc = HeaderCarrier(sessionId = Some(SessionId(sid)))
    val doc = Document(ref, DateTime.now)
    val savedDoc = doc.copy(saveForLaterPassword = Some(pas))

    "saving a document for a reference number that has not previously saved for later" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val s = SaveInProgressSubmissionForLater(() => pas, expect(savedDoc), (a, b, c) => updated = (a, b, c)) _
      val r = await(s(doc, hc))

      "generate a password using the password generator, and store the document with the generated password" in {
        assert(r === pas)
      }

      "update the document in the current session with the password" in {
        assert(updated === ((hc, ref, savedDoc)))
      }
    }

    "saving a new document for a reference number that has already saved a document" should {
      val oldP = s"oldPassword${Random.nextDouble}"
      val newP = s"newPassword${Random.nextDouble}"
      val ref = "77788899902"
      val doc = Document(ref, DateTime.now, saveForLaterPassword = Some(oldP))
      var savedDoc: Document = null

      "use the existing password if a document already has a save for later password" in {
        var updated: (HeaderCarrier, ReferenceNumber, Document) = null
        val s = SaveInProgressSubmissionForLater(() => newP, set(savedDoc = _), (a, b, c) => updated = (a, b, c)) _
        assert(await(s(doc, hc)) === oldP)
      }
    }
  }
}

class Generate7LengthLowercaseAlphaNumPasswordSpec extends AnyFlatSpec with should.Matchers {

  behavior of "Generate 7 length lowercase alpha numeric password spec"

  it should "Generate a password consisting of unambiguous lowercase chars and numbers with a length of 7" in {
    (1 to 100) foreach { n =>
      val pw = Generate7LengthLowercaseAlphaNumPassword()
      assert(pw.length === 7)
      pw.foreach { c => assert(isAllowed(c) === true, s"$c is not a valid character for passwords") }
    }
  }

  private def isAllowed(c: Char) =
    (c.isDigit || isLowercaseLetter(c)) && (isNonAmbiguousDigit(c) || isNonAmbiguousLowercaseLetter(c))

  private def isLowercaseLetter(c: Char) = c.toString.matches("[a-z]")
  private def isNonAmbiguousDigit(c: Char) = !Seq('0', '1').contains(c)
  private def isNonAmbiguousLowercaseLetter(c: Char) = !Seq('i', 'l', 'o').contains(c)
}
