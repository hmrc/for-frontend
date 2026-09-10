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

import form.PageElevenForm.pageElevenForm
import models.serviceContracts.submissions.IncentivesAndPayments
import org.scalatest.Assertion
import play.api.data.{Form, FormError}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.FormBindingTestAssertions.*
import utils.MappingSpecs.*

class PageElevenMappingSpec extends BaseSpec:

  private val givenRentFreePeriod: (String, String)   = "rentFreePeriod"                              -> "true"
  private val rentFreePeriodLength: (String, String)  = "rentFreePeriodDetails.rentFreePeriodLength"  -> "36"
  private val rentFreePeriodDetails: (String, String) = "rentFreePeriodDetails.rentFreePeriodDetails" -> "alien abduction"

  private val capPaid: (String, String)             = "payCapitalSum"                        -> "true"
  private val capSumPaid: (String, String)          = "capitalPaidDetails.capitalSum"        -> "3.5"
  private val capSumPaidDateMonth: (String, String) = "capitalPaidDetails.paymentDate.month" -> "11"
  private val capSumPaidDateYear: (String, String)  = "capitalPaidDetails.paymentDate.year"  -> "2012"

  private val capReceived: (String, String)            = "receiveCapitalSum"                        -> "true"
  private val capSumReceived: (String, String)         = "capitalReceivedDetails.receivedSum"       -> "99.99"
  private val capSumReceiveDateMonth: (String, String) = "capitalReceivedDetails.paymentDate.month" -> "11"
  private val capSumReceiveDateYear: (String, String)  = "capitalReceivedDetails.paymentDate.year"  -> "2012"

  private val baseData: Map[String, String] = Map(
    givenRentFreePeriod,
    rentFreePeriodLength,
    rentFreePeriodDetails,
    capPaid,
    capSumPaid,
    capSumPaidDateMonth,
    capSumPaidDateYear,
    capReceived,
    capSumReceived,
    capSumReceiveDateMonth,
    capSumReceiveDateYear
  )

  private def bind(formData: Map[String, String]): Form[IncentivesAndPayments] =
    pageElevenForm.bind(formData).convertGlobalToFieldErrors()

  private def containsError(errors: Seq[FormError], key: String, message: String): Assertion =
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists shouldBe true

  "PageElevenForm" should {
    "bind with the fields and not return issues" in {
      val res = bind(baseData)

      res.errors.isEmpty shouldBe true
    }

    "bind with the fields and return no errors, when the rent free details are not present when the option for none was selected" in {
      val data = baseData.updated("rentFreePeriod", "false") - "rentFreePeriodDetails.rentFreePeriodLength"
      val res  = bind(data)

      res.errors.isEmpty shouldBe true
      res.errors.size    shouldBe 0
    }

    "bind with the fields and return errors, when the months section of the rent free details is not present when the option for one was selected" in {
      val data = baseData - "rentFreePeriodDetails.rentFreePeriodLength"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      containsError(res.errors, "rentFreePeriodDetails.rentFreePeriodLength", "error.empty.required")
    }

    "bind with the fields and return errors, when the rent free details are not present when the option for one was selected" in {
      val data = baseData - "rentFreePeriodDetails.rentFreePeriodLength" - "rentFreePeriodDetails.rentFreePeriodDetails"
      val res  = bind(data).convertGlobalToFieldErrors()

      mustContainError("rentFreePeriodDetails.rentFreePeriodLength", "error.empty.required", res)
      mustContainError("rentFreePeriodDetails.rentFreePeriodDetails", "error.rentFreePeriod.required", res)
    }

    "bind with the fields and return no error when there is no details for a capital sum payment when none is made" in {
      val data = baseData.updated(
        "payCapitalSum",
        "false"
      ) - "capitalPaidDetails.paymentDate.day" - "capitalPaidDetails.paymentDate.month" - "capitalPaidDetails.paymentDate.year" -
        "capitalPaidDetails.capitalSum"
      val res  = bind(data)

      res.errors.isEmpty shouldBe true
    }

    "bind with the fields and return errors, when payment date month field is not filled in when giving details about paying a capital sum" in {
      val data = baseData - "capitalPaidDetails.paymentDate.month"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      mustContainError("capitalPaidDetails.paymentDate.month", "error.made.month.required", res)
    }

    "bind with the fields and return errors, when payment date year fields is not filled in when giving details about paying a capital sum" in {
      val data = baseData - "capitalPaidDetails.paymentDate.year"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      mustContainError("capitalPaidDetails.paymentDate.year", "error.made.year.required", res)
    }

    "bind with the fields and return errors, when payment amount field is not filled in when giving details about paying a capital sum" in {
      val data = baseData - "capitalPaidDetails.capitalSum"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      mustContainError("capitalPaidDetails.capitalSum", "error.required.paid", res)
    }

    "validate the rent free period details" in
      validateLettersNumsSpecCharsUptoLength("rentFreePeriodDetails.rentFreePeriodDetails", 250, pageElevenForm, baseData, Some("error.rentFreePeriod.maxLength"))

    "not bind and return errors when rent free duration has 'a' entered" in {
      val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength", "a")
      val res      = bind(testData)

      containsError(res.errors, "rentFreePeriodDetails.rentFreePeriodLength", "error.maxValueRentFreeIsBlank.required")
    }

    "not bind and return errors when rent free duration has '0' entered" in {
      val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength", "0")
      val res      = bind(testData)

      containsError(res.errors, "rentFreePeriodDetails.rentFreePeriodLength", "error.empty.required")
    }

    "not bind and return errors when rent free duration has '-10' entered" in {
      val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength", "-10")
      val res      = bind(testData)

      containsError(res.errors, "rentFreePeriodDetails.rentFreePeriodLength", "error.maxValueRentFreeIsBlank.required")
    }

    "validate the capital sum paid" in
      validateCurrency("capitalPaidDetails.capitalSum", pageElevenForm, baseData, ".paid")

    "validate the capital sum payment date" in
      validatePastDate("capitalPaidDetails.paymentDate", pageElevenForm, baseData, ".made")

    "validate the capital sum received" in
      validateCurrency("capitalReceivedDetails.receivedSum", pageElevenForm, baseData, ".received")

    "validate the capital sum received date" in
      validatePastDate("capitalReceivedDetails.paymentDate", pageElevenForm, baseData, ".received")
  }
