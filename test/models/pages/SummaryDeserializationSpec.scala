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

package models.pages

import connectors.{Document, Page}
import models.serviceContracts.submissions.OccupierTypeIndividuals
import org.joda.time.DateTime
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class SummaryDeserializationSpec extends AnyFlatSpec with should.Matchers with OptionValues {

  val pages: Seq[Page] = Seq(
    Page(3, Map(
      "propertyType" -> Seq("hotel"),
      "occupierType" -> Seq("individuals"),
      "firstOccupationDate.month" -> Seq("02"),
      "firstOccupationDate.year" -> Seq("2018"),
      "mainOccupierName" -> Seq("John John"),
      "propertyOwnedByYou" -> Seq("true")
    )),

    Page(9, Map(
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
  )

  val doc: Document = Document(
    referenceNumber = "1111",
    journeyStarted = DateTime.now(),
    pages = pages,
    address = None,
    saveForLaterPassword = None,
    journeyResumptions = Seq()
  )

  "Summary builder" should "deserialize document with redundant fields " in {
     val summaryBuilder: SummaryBuilder = SummaryBuilder
     val summary = summaryBuilder.build(doc)

    summary.theProperty shouldBe defined
    summary.theProperty.value.propertyType shouldBe "hotel"
    summary.theProperty.value.occupierType shouldBe OccupierTypeIndividuals


    summary.rent shouldBe defined
    summary.rent.value.totalRent.amount shouldBe BigDecimal("10000")

   }





}
