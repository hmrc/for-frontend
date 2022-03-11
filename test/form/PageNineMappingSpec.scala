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

package form

import models._
import models.pages._
import models.serviceContracts.submissions._
import org.joda.time.LocalDate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.FormError


class PageNineMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageNineForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "A fully populated form" should "bind to a PageNineData" in {
    val expectedData = PageNine(
      totalRent = AnnualRent(123.45),
      rentBecomePayable = new LocalDate(2001, 5, 1),
      rentActuallyAgreed = new LocalDate(2001, 5, 1),
      negotiatingNewRent = false,
      rentBasis = RentBaseTypeOther,
      rentBasisOtherDetails = Some("oneTwoThree"))

    val res = bind(fullData)

    doesNotContainErrors(res)
    res.value.get should be(expectedData)
  }
  checkMissingField(keys.annualRentExcludingVat)

  checkMissingField(keys.rentActuallyAgreedDay)
  checkMissingField(keys.rentActuallyAgreedYear, "error.year.required")
  checkMissingField(keys.rentActuallyAgreedMonth, "error.month.required")

  checkMissingField(keys.negotiatingNewRent, Errors.negotiatingNewRentRequired)
  checkMissingField(keys.rentBasedOn, Errors.rentBasedOnRequired)
  checkMissingField(keys.rentBecomePayableDay)
  checkMissingField(keys.rentBecomePayableYear, "error.year.required")
  checkMissingField(keys.rentBecomePayableMonth, "error.month.required")

  RentBaseTypes.all.filter(x => (x != RentBaseTypeOpenMarket && x != RentBaseTypeIndexation) ).foreach { rentBasis =>
    "A form with 'rent basis' of '" + rentBasis.name + "' but a missing 'rent basis other' field" should "return required error for rent based on details" in {
      val testData = fullData.updated(keys.rentBasedOn, rentBasis.name) - keys.rentBasedOnDetails
      val res = bind(testData)
      
      mustContainRequiredErrorFor("rentBasedOnDetails", res)
    }
  }

  "A form with a rent basis of open market and no rent based on details" should "not error" in {
    val data = fullData.updated(keys.rentBasedOn, RentBaseTypeOpenMarket.name) - keys.rentBasedOnDetails
    val form = bind(data)

    doesNotContainErrors(form)
  }

  "Page Nine mapping" should "validate the rent start date" in {
    validateFullDateInPast("rentBecomePayable", pageNineForm, fullData)
  }

  it should "validate the rent agreed date" in {
    validateFullDateInPast("rentActuallyAgreed", pageNineForm, fullData)
  }

/*
  it should "validate the annual rent" in {
    validateAnnualRent(keys.totalRent, pageNineForm, fullData)
  }
*/

  it should "validate the rent based on ... details" in {
    validateLettersNumsSpecCharsUptoLength(keys.rentBasedOnDetails, 250, pageNineForm, fullData)
  }

  object TestData {
    val keys = new {
      val totalRent = "totalRent"
      val rentLengthType = "totalRent.rentLengthType"
      val annualRentExcludingVat = "totalRent.annualRentExcludingVat"
      val rentBecomePayableDay = "rentBecomePayable.day"
      val rentBecomePayableMonth = "rentBecomePayable.month"
      val rentBecomePayableYear = "rentBecomePayable.year"

      val rentActuallyAgreedDay = "rentActuallyAgreed.day"
      val rentActuallyAgreedMonth = "rentActuallyAgreed.month"
      val rentActuallyAgreedYear = "rentActuallyAgreed.year"

      val negotiatingNewRent = "negotiatingNewRent"
      val rentBasedOn = "rentBasedOn"
      val rentBasedOnDetails = "rentBasedOnDetails"
    }

    val fullData: Map[String, String] = Map(
      keys.rentLengthType -> RentLengthTypeQuarterly.name,
      keys.annualRentExcludingVat -> "123.45",
      keys.rentBecomePayableDay -> "1",
      keys.rentBecomePayableMonth -> "5",
      keys.rentBecomePayableYear -> "2001",

      keys.rentActuallyAgreedDay -> "1",
      keys.rentActuallyAgreedMonth -> "5",
      keys.rentActuallyAgreedYear -> "2001",

      keys.negotiatingNewRent -> "false",
      keys.rentBasedOn -> RentBasisTypeOther.name,
      keys.rentBasedOnDetails -> "oneTwoThree")

    def bind(dataMap: Map[String, String]) = {
      val bound = pageNineForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    def hasError(errors: Seq[FormError], key: String, message: String) = {
      val res = errors.exists { err => err.key == key && err.messages.contains(message) }
      res should be(true)
    }

    def checkMissingField(key: String, code: String = Errors.required) = {
      "a  form missing the " + key + " field" should "bind result in 1 validation error" in {
        val testData = fullData - key
        val res = bind(testData)
        mustContainError(key, code, res)
      }
    }
  }

}
