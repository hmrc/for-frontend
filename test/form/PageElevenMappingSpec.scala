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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.FormError
import utils.FormBindingTestAssertions._
import utils.MappingSpecs._

class PageElevenMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageElevenForm._

  val givenRentFreePeriod = "rentFreePeriod" -> "true"
  val rentFreePeriodLength = "rentFreePeriodDetails.rentFreePeriodLength" -> "36"
  val rentFreePeriodDetails = "rentFreePeriodDetails.rentFreePeriodDetails" -> "alien abduction"

  val capPaid = "payCapitalSum" -> "true"
  val capSumPaid = "capitalPaidDetails.capitalSum" -> "3.5"
  val capSumPaidDateMonth = "capitalPaidDetails.paymentDate.month" -> "11"
  val capSumPaidDateYear = "capitalPaidDetails.paymentDate.year" -> "2012"

  val capReceived = "receiveCapitalSum" -> "true"
  val capSumReceived = "capitalReceivedDetails.capitalSum" -> "99.99"
  val capSumReceiveDateMonth = "capitalReceivedDetails.paymentDate.month" -> "11"
  val capSumReceiveDateYear = "capitalReceivedDetails.paymentDate.year" -> "2012"


  val baseData = Map(givenRentFreePeriod,
    rentFreePeriodLength,
    rentFreePeriodDetails,
    capPaid,
    capSumPaid,
    capSumPaidDateMonth,
    capSumPaidDateYear,
    capReceived,
    capSumReceived,
    capSumReceiveDateMonth,
    capSumReceiveDateYear)

  def bind(formData: Map[String, String]) = {
    pageElevenForm.bind(formData).convertGlobalToFieldErrors()
  }

  def containsError(errors: Seq[FormError], key: String, message: String): Boolean = {
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists should be(true)
    exists
  }

  behavior of "Page eleven form"

  it should "bind with the fields and not return issues" in {
    val res = bind(baseData)
    res.errors.isEmpty should be(true)
  }

  it should "bind with the fields and return no errors, when the rent free details are not present when the option for none was selected" in {
    val data = baseData.updated("rentFreePeriod", "false") - "rentFreePeriodDetails.rentFreePeriodLength"
    val res = bind(data)
    res.errors.isEmpty should be(true)
    res.errors.size should be(0)
  }

  it should "bind with the fields and return errors, when the months section of the rent free details is not present when the option for one was selected" in {
    val data = baseData - "rentFreePeriodDetails.rentFreePeriodLength"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "rentFreePeriodDetails.rentFreePeriodLength", "error.required")
  }

  it should "bind with the fields and return errors, when the rent free details are not present when the option for one was selected" in {
    val data = baseData - "rentFreePeriodDetails.rentFreePeriodLength" - "rentFreePeriodDetails.rentFreePeriodDetails"
    val res = bind(data).convertGlobalToFieldErrors()
    mustContainRequiredErrorFor("rentFreePeriodDetails.rentFreePeriodLength", res)
    mustContainRequiredErrorFor("rentFreePeriodDetails.rentFreePeriodDetails", res)
  }

  it should "bind with the fields and return no error when there is no details for a capital sum payment when none is made" in {
    val data = baseData.updated("payCapitalSum", "false") - "capitalPaidDetails.paymentDate.day" - "capitalPaidDetails.paymentDate.month" - "capitalPaidDetails.paymentDate.year" - "capitalPaidDetails.capitalSum"
    val res = bind(data)
    res.errors.isEmpty should be (true)
  }

  it should "bind with the fields and return errors, when payment date month field is not filled in when giving details about paying a capital sum" in {
    val data = baseData - "capitalPaidDetails.paymentDate.month"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    mustContainError("capitalPaidDetails.paymentDate.month", "error.month.required", res)
  }

  it should "bind with the fields and return errors, when payment date year fields is not filled in when giving details about paying a capital sum" in {
    val data = baseData - "capitalPaidDetails.paymentDate.year"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    mustContainError("capitalPaidDetails.paymentDate.year", "error.year.required", res)
  }

  it should "bind with the fields and return errors, when payment amount field is not filled in when giving details about paying a capital sum" in {
    val data = baseData - "capitalPaidDetails.capitalSum"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "capitalPaidDetails.capitalSum", "error.required")
  }

  it should "validate the rent free period details" in {
    validateLettersNumsSpecCharsUptoLength("rentFreePeriodDetails.rentFreePeriodDetails", 250, pageElevenForm, baseData)
  }

  it should "not bind and return errors when rent free duration has 'a' entered" in {
    val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength","a")
    val res = bind(testData)
    containsError(res.errors,"rentFreePeriodDetails.rentFreePeriodLength", "error.number")
  }

  it should "not bind and return errors when rent free duration has '0' entered" in {
    val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength","0")
    val res = bind(testData)
    containsError(res.errors,"rentFreePeriodDetails.rentFreePeriodLength", "error.min")
  }

  it should "not bind and return errors when rent free duration has '-10' entered" in {
    val testData = baseData.updated("rentFreePeriodDetails.rentFreePeriodLength","-10")
    val res = bind(testData)
    containsError(res.errors,"rentFreePeriodDetails.rentFreePeriodLength", "error.min")
  }

  it should "validate the capital sum paid" in {
    validateCurrency("capitalPaidDetails.capitalSum", pageElevenForm, baseData)
  }

  it should "validate the capital sum payment date" in {
    validatePastDate("capitalPaidDetails.paymentDate", pageElevenForm, baseData)
  }

  it should "validate the capital sum received" in {
    validateCurrency("capitalReceivedDetails.capitalSum", pageElevenForm, baseData)
  }

  it should "validate the capital sum received date" in {
    validatePastDate("capitalReceivedDetails.paymentDate", pageElevenForm, baseData)
  }
}
