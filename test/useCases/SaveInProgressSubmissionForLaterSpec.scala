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
import controllers.toFut
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import util.DateUtil.nowInUK
import utils.BehaviourVerification

import java.security.SecureRandom
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaveInProgressSubmissionForLaterSpec extends BaseSpec with BehaviourVerification:

  private val random = SecureRandom()

  "SaveInProgressSubmissionForLater" when {
    val pas      = s"thisisapassorwd${random.nextDouble}"
    val ref      = "1111111222"
    val sid      = UUID.randomUUID.toString
    val hc       = HeaderCarrier(sessionId = Some(SessionId(sid)))
    val doc      = Document(ref, nowInUK)
    val savedDoc = doc.copy(saveForLaterPassword = Some(pas))

    "saving a document for a reference number that has not previously saved for later" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document)     = null
      val s: (Document, HeaderCarrier) => Future[ReferenceNumber] =
        SaveInProgressSubmissionForLater.apply(() => pas, expect(savedDoc), (a, b, c) => updated = (a, b, c))

      val r = s.apply(doc, hc).futureValue

      "generate a password using the password generator, and store the document with the generated password" in {
        r shouldBe pas
      }

      "update the document in the current session with the password" in {
        updated shouldBe (hc, ref, savedDoc)
      }
    }

    "saving a new document for a reference number that has already saved a document" should {
      val oldP = s"oldPassword${random.nextDouble}"
      val newP = s"newPassword${random.nextDouble}"
      val ref  = "77788899902"
      val doc  = Document(ref, nowInUK, saveForLaterPassword = Some(oldP))

      "use the existing password if a document already has a save for later password" in {
        val s: (Document, HeaderCarrier) => Future[ReferenceNumber] =
          SaveInProgressSubmissionForLater.apply(() => newP, set[Document, Unit](_ => ()), (_, _, _) => Future.unit)
        s.apply(doc, hc).futureValue shouldBe oldP
      }
    }
  }
