/*
 * Copyright 2019 HM Revenue & Customs
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

package connectors

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.OneServerPerSuite


/**
  * Test compatibility after rentLengthType removal.
  *
  * This test was created to test code which remove page9 from saved documents.
  * We removed one question from page 9 and this question changes meaning of
  * anualRent field. To prevent submission of wrong data, we removing whole page9.
  * Probably after 6 months this code can be removed.
  */
class HODConnectorCompatibilitySpec extends FlatSpec with Matchers with OneServerPerSuite{


  val oldPage9 = Page(9, Map(
    "totalRent.annualRentExcludingVat" -> Seq("10000"),
    "totalRent.rentLengthType" -> Seq("quarterly"),
    "totalRent.SomethingForTest" -> Seq("testing value"),
    "rentBecomePayable.day" -> Seq("20"),
    "rentBecomePayable.month" -> Seq("12"),
    "rentBecomePayable.year" -> Seq("2018"),
    "rentActuallyAgreed.day" -> Seq("20"),
    "rentActuallyAgreed.month" -> Seq("12"),
    "rentActuallyAgreed.year" -> Seq("2018"),
    "negotiatingNewRent" -> Seq("false"),
    "rentBasedOn" -> Seq("other"),
    "rentBasedOnDetails" -> Seq("vvsdfsd sdf")
  ))

  val newPage9 = Page(9, Map(
    "totalRent.annualRentExcludingVat" -> Seq("10000"),
    "totalRent.SomethingForTest" -> Seq("testing value"),
    "rentBecomePayable.day" -> Seq("20"),
    "rentBecomePayable.month" -> Seq("12"),
    "rentBecomePayable.year" -> Seq("2018"),
    "rentActuallyAgreed.day" -> Seq("20"),
    "rentActuallyAgreed.month" -> Seq("12"),
    "rentActuallyAgreed.year" -> Seq("2018"),
    "negotiatingNewRent" -> Seq("false"),
    "rentBasedOn" -> Seq("other"),
    "rentBasedOnDetails" -> Seq("vvsdfsd sdf")
  ))

  val page3 = Page(3, Map(
    "propertyType" -> Seq("hotel"),
    "occupierType" -> Seq("individuals"),
    "firstOccupationDate.month" -> Seq("02"),
    "firstOccupationDate.year" -> Seq("2018"),
    "mainOccupierName" -> Seq("John John"),
    "propertyOwnedByYou" -> Seq("true")
  ))

  val journeyStarted = DateTime.now()

  val oldDocument = Some(Document(
    "referenceNumber", journeyStarted, Seq(page3, oldPage9), None, None, Seq.empty
  ))

  val expectedOldDocumentAfterModification = Some(Document(
    "referenceNumber", journeyStarted, Seq(page3), None, None, Seq.empty
  ))

  val newDocument = Some(Document(
    "referenceNumber", journeyStarted, Seq(page3, newPage9), None, None, Seq.empty
  ))



  "HODConnecter" should "remove page 9 from document when rentLengthType exist in page 9" in {
    val connector = HODConnector

    val testDocument = connector.removeRentLengthType(oldDocument)

    testDocument shouldBe expectedOldDocumentAfterModification

  }


  "HODConnecter" should "keep page9 in docuemnt when rentLengthType not exist " in {
    val connector = HODConnector

    val testDocument = connector.removeRentLengthType(newDocument)

    testDocument shouldBe newDocument

    testDocument should be theSameInstanceAs newDocument

  }


}