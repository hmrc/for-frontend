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

package form

import models.*
import models.pages.*
import models.serviceContracts.submissions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.{Form, FormError}

import java.time.LocalDate
import org.scalatest.Assertion

class PageNineMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageNineForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "A fully populated form" should "bind to a PageNineData" in {
    val expectedData = PageNine(
      totalRent = AnnualRent(123.45),
      rentBecomePayable = LocalDate.of(2001, 5, 1),
      rentActuallyAgreed = LocalDate.of(2001, 5, 1),
      negotiatingNewRent = false,
      rentBasis = RentBaseTypeOther,
      rentBasisOtherDetails = Some("oneTwoThree")
    )

    val res = bind(fullData)

    doesNotContainErrors(res)
    res.value.get should be(expectedData)
  }
  checkMissingField(keys.annualRentExcludingVat, "error.required.annualRentExcludingVat")

  checkMissingField(keys.rentActuallyAgreedDay, "error.rentActuallyAgreed.day.required")
  checkMissingField(keys.rentActuallyAgreedYear, "error.rentActuallyAgreed.year.required")
  checkMissingField(keys.rentActuallyAgreedMonth, "error.rentActuallyAgreed.month.required")

  checkMissingField(keys.negotiatingNewRent, Errors.negotiatingNewRentRequired)
  checkMissingField(keys.rentBasedOn, Errors.rentBasedOnRequired)
  checkMissingField(keys.rentBecomePayableDay, "error.rentBecomePayable.day.required")
  checkMissingField(keys.rentBecomePayableYear, "error.rentBecomePayable.year.required")
  checkMissingField(keys.rentBecomePayableMonth, "error.rentBecomePayable.month.required")

  RentBaseTypes.all.filter(x => x != RentBaseTypeOpenMarket && x != RentBaseTypeIndexation).foreach { rentBasis =>
    "A form with 'rent basis' of '" + rentBasis.name + "' but a missing 'rent basis other' field" should "return required error for rent based on details" in {
      val testData = fullData.updated(keys.rentBasedOn, rentBasis.name) - keys.rentBasedOnDetails
      val res      = bind(testData)

      mustContainError("rentBasedOnDetails", "error.rentBasedOnDetails.required", res)
    }
  }

  "A form with a rent basis of open market and no rent based on details" should "not error" in {
    val data = fullData.updated(keys.rentBasedOn, RentBaseTypeOpenMarket.name) - keys.rentBasedOnDetails
    val form = bind(data)

    doesNotContainErrors(form)
  }

  "Page Nine mapping" should "validate the rent start date" in
    validateFullDateInPast("rentBecomePayable", pageNineForm, fullData, ".rentBecomePayable")

  it should "validate the rent agreed date" in
    validateFullDateInPast("rentActuallyAgreed", pageNineForm, fullData, ".rentActuallyAgreed")

  it should "validate the annual rent" in
    validateAnnualRent(keys.totalRent, pageNineForm, fullData, ".annualRentExcludingVat")

  it should "validate the rent based on ... details" in
    validateLettersNumsSpecCharsUptoLength(keys.rentBasedOnDetails, 250, pageNineForm, fullData, Some("error.rentBasedOnDetails.maxLength"))

  object TestData {

    val keys: Keys = new Keys

    class Keys {
      val totalRent              = "totalRent"
      val rentLengthType         = "totalRent.rentLengthType"
      val annualRentExcludingVat = "totalRent.annualRentExcludingVat"
      val rentBecomePayableDay   = "rentBecomePayable.day"
      val rentBecomePayableMonth = "rentBecomePayable.month"
      val rentBecomePayableYear  = "rentBecomePayable.year"

      val rentActuallyAgreedDay   = "rentActuallyAgreed.day"
      val rentActuallyAgreedMonth = "rentActuallyAgreed.month"
      val rentActuallyAgreedYear  = "rentActuallyAgreed.year"

      val negotiatingNewRent = "negotiatingNewRent"
      val rentBasedOn        = "rentBasedOn"
      val rentBasedOnDetails = "rentBasedOnDetails"
    }

    val fullData: Map[String, String] = Map(
      keys.rentLengthType          -> RentLengthTypeQuarterly.name,
      keys.annualRentExcludingVat  -> "123.45",
      keys.rentBecomePayableDay    -> "1",
      keys.rentBecomePayableMonth  -> "5",
      keys.rentBecomePayableYear   -> "2001",
      keys.rentActuallyAgreedDay   -> "1",
      keys.rentActuallyAgreedMonth -> "5",
      keys.rentActuallyAgreedYear  -> "2001",
      keys.negotiatingNewRent      -> "false",
      keys.rentBasedOn             -> RentBasisTypeOther.name,
      keys.rentBasedOnDetails      -> "oneTwoThree"
    )

    def bind(dataMap: Map[String, String]): Form[PageNine] = {
      val bound = pageNineForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    def hasError(errors: Seq[FormError], key: String, message: String): Assertion = {
      val res = errors.exists(err => err.key == key && err.messages.contains(message))
      res should be(true)
    }

    def checkMissingField(key: String, code: String = Errors.required): Unit =
      "a  form missing the " + key + " field" should "bind result in 1 validation error" in {
        val testData = fullData - key
        val res      = bind(testData)
        mustContainError(key, code, res)
      }
  }

}
