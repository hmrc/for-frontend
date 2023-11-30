/*
 * Copyright 2023 HM Revenue & Customs
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

package security

import connectors.Document
import models.FORLoginResponse
import models.serviceContracts.submissions.Address
import uk.gov.hmrc.http.HeaderCarrier
import useCases.ReferenceNumber
import utils.UnitTest

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class LoginToHODSpec extends UnitTest {
  import TestData._

  "Login to HOD with valid credentials" when {
    implicit val hc = HeaderCarrier()

    "a user has previously saved a document for later" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val l = LoginToHOD(
        respondWith(refNum, postcode)(loginResponse),
        respondWith(auth, refNum)(Some(savedDoc)),
        set[HeaderCarrier, ReferenceNumber, Document, Unit](updated = _)
      ) _
      val r = await(l(ref1, ref2, postcode, now))

      "return the saved document" in {
        assert(r.leftSideValue === DocumentPreviouslySaved(savedDoc, loginResponse.forAuthToken, loginResponse.address))
      }

      "loads an empty document with the retrieved credentials and the current time as the journey start time into the session in case the user cannot login or wants to start again" in {
        assert(updated === ((hc, refNum, Document(refNum, now, address = Some(loginResponse.address)))))
      }
    }

    "there is no previously stored document" should {
      var updated: (HeaderCarrier, ReferenceNumber, Document) = null
      val l = LoginToHOD(
        respondWith(refNum, postcode)(loginResponse),
        none,
        set[HeaderCarrier, ReferenceNumber, Document, Unit](updated = _)
      ) _
      val r = await(l(ref1, ref2, postcode, now))

      "indicate there is no saved document" in {
         assert(r.leftSideValue === NoExistingDocument(loginResponse.forAuthToken, loginResponse.address))
       }

       "loads an empty document with the retrieved credentials into the session" in {
         assert(updated === ((hc, refNum, Document(refNum, now, address = Some(loginResponse.address)))))
       }
     }
  }

  object TestData {
    val ref1 = "1111111"
    val ref2 = "899"
    val refNum = s"$ref1$ref2"
    val password = "aljsljdf"
    val postcode = "CV24 5RR"
    val testAddress = Address("123", None, None, postcode)
    val auth = "YouAreLoggedInNow"
    val loginResponse = FORLoginResponse(auth, testAddress)
    val now = ZonedDateTime.of(2015, 3, 2, 13, 20, 0, 0, ZoneOffset.UTC)
    val savedDoc = Document("savedDocument", now)
  }
}

case class ArgumentsDidNotMatch(es: Seq[Any], as: Seq[Any]) extends Exception(s"Expected: $es but got: $as")
