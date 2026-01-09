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

import _root_.utils.UnitTest
import connectors.Document
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import util.DateUtil.nowInUK
import utils.toFut

import java.security.SecureRandom
import scala.collection.StringOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaveInProgressSubmissionForLaterSpec extends UnitTest {

  private val random = new SecureRandom()

  "Save an in progress document for later" when {
    val pas      = s"thisisapassorwd${random.nextDouble}"
    val ref      = "1111111222"
    val sid      = java.util.UUID.randomUUID().toString
    val hc       = HeaderCarrier(sessionId = Some(SessionId(sid)))
    val doc      = Document(ref, nowInUK)
    val savedDoc = doc.copy(saveForLaterPassword = Some(pas))

    "saving a document for a reference number that has not previously saved for later" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document)     = null
      val s: (Document, HeaderCarrier) => Future[ReferenceNumber] =
        SaveInProgressSubmissionForLater.apply(() => pas, expect(savedDoc), (a, b, c) => updated = (a, b, c))
      val r                                                       = await(s.apply(doc, hc))

      "generate a password using the password generator, and store the document with the generated password" in
        assert(r === pas)

      "update the document in the current session with the password" in
        assert(updated === (hc, ref, savedDoc))
    }

    "saving a new document for a reference number that has already saved a document" should {
      val oldP = s"oldPassword${random.nextDouble}"
      val newP = s"newPassword${random.nextDouble}"
      val ref  = "77788899902"
      val doc  = Document(ref, nowInUK, saveForLaterPassword = Some(oldP))

      "use the existing password if a document already has a save for later password" in {
        val s: (Document, HeaderCarrier) => Future[ReferenceNumber] =
          SaveInProgressSubmissionForLater.apply(() => newP, set[Document, Unit](_ => ()), (_, _, _) => Future.unit)
        assert(await(s.apply(doc, hc)) === oldP)
      }
    }
  }
}

class Generate7LengthLowercaseAlphaNumPasswordSpec extends AnyFlatSpec with should.Matchers {

  behavior of "Generate 7 length lowercase alpha numeric password spec"

  it should "Generate a password consisting of unambiguous lowercase chars and numbers with a length of 7" in {
    (1 to 100) foreach { _ =>
      val pw = Generate7LengthLowercaseAlphaNumPassword()
      assert(pw.length === 7)
      for (c <- StringOps(pw))
        assert(isAllowed(c) === true, s"$c is not a valid character for passwords")
    }
  }

  private def isAllowed(c: Char) =
    (c.isDigit || isLowercaseLetter(c)) && (isNonAmbiguousDigit(c) || isNonAmbiguousLowercaseLetter(c))

  private def isLowercaseLetter(c: Char)             = c.toString.matches("[a-z]")
  private def isNonAmbiguousDigit(c: Char)           = !Seq('0', '1').contains(c)
  private def isNonAmbiguousLowercaseLetter(c: Char) = !Seq('i', 'l', 'o').contains(c)
}
