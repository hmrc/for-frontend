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

import models.pages.PageSeven
import models.serviceContracts.submissions.ReviewIntervalTypeOther
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.{Form, FormError}
import utils.FormBindingTestAssertions.*

class PageSevenMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageSevenForm._
  import utils.MappingSpecs._

  val leaseContainsRentReviews: (String, String) = "leaseContainsRentReviews"                                           -> "true"
  val reviewIntervalType: (String, String)       = "rentReviewDetails.reviewIntervalType"                               -> ReviewIntervalTypeOther.name
  val reviewIntervalYears: (String, String)      = "rentReviewDetails.reviewIntervalTypeSpecify.years"                  -> "11"
  val reviewIntervalMonths: (String, String)     = "rentReviewDetails.reviewIntervalTypeSpecify.months"                 -> "12"
  val lastReviewDateMonth: (String, String)      = "rentReviewDetails.lastReviewDate.month"                             -> "3"
  val lastReviewDateYear: (String, String)       = "rentReviewDetails.lastReviewDate.year"                              -> "2000"
  val canRentReduced: (String, String)           = "rentReviewDetails.canRentReduced"                                   -> "true"
  val rentResultOfRentReview: (String, String)   = "rentReviewDetails.rentResultOfRentReview"                           -> "true"
  val whenWasRentReviewMonth: (String, String)   = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.month" -> "2"
  val whenWasRentReviewYear: (String, String)    = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year"  -> "2001"
  val rentAgreedBetween: (String, String)        = "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween"       -> "false"
  val rentFixedBy: (String, String)              = "rentReviewDetails.rentReviewResultsDetails.rentFixedBy"             -> "arbitrator"

  val baseData: Map[String, String] = Map(
    leaseContainsRentReviews,
    reviewIntervalType,
    reviewIntervalYears,
    reviewIntervalMonths,
    lastReviewDateMonth,
    lastReviewDateYear,
    canRentReduced,
    rentResultOfRentReview,
    whenWasRentReviewMonth,
    whenWasRentReviewYear,
    rentAgreedBetween,
    rentFixedBy
  )

  def bind(formData: Map[String, String]): Form[PageSeven] =
    pageSevenForm.bind(formData).convertGlobalToFieldErrors()

  def containsError(errors: Seq[FormError], key: String, message: String): Boolean = {
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists should be(true)
    exists
  }

  "PageSevenData" should "bind with the fields and not return issues" in {
    val res = bind(baseData)
    doesNotContainErrors(res)
  }

  "PageSevenData" should "bind with the fields and return issues when review frequency is not selected" in {
    val data = baseData - "rentReviewDetails.reviewIntervalType"
    val res  = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size    should be(1)
    containsError(res.errors, "rentReviewDetails.reviewIntervalType", Errors.rentReviewFrequencyRequired)
  }

  "PageSevenData" should "bind with the fields and return issues when the year field of the last review date is missing" in {
    val data = baseData - "rentReviewDetails.lastReviewDate.year"
    val res  = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size    should be(1)
    mustContainError("rentReviewDetails.lastReviewDate.year", "error.lastRentReviewDate.year.required", res)
  }
  "PageSevenData" should "bind with the fields and return issues when boolean can rent be reduced due to rent review value is missing" in {
    val data = baseData - "rentReviewDetails.canRentReduced"
    val res  = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size    should be(1)
    containsError(res.errors, "rentReviewDetails.canRentReduced", Errors.rentCanBeReducedOnReviewRequired)
  }

  "PageSevenData" should "bind with the fields and return issues when connection type selection missing" in {
    val data = baseData - "rentReviewDetails.rentResultOfRentReview"
    val res  = bind(data)
    res.errors.isEmpty should be(false)
    containsError(res.errors, "rentReviewDetails.rentResultOfRentReview", Errors.isRentResultOfReviewRequired)
  }

  "PageSevenData" should "bind with the fields and return issues when the date for the last effective rent review is missing" in {
    val data = baseData - "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year"
    val res  = bind(data)
    res.errors.size    should be(1)
    res.errors.isEmpty should be(false)
    mustContainError("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year", "error.rentResultOfReview.year.required", res)
  }

  "PageSevenData" should "not bind with the fields and return an error when the person who agreed the rent input is not selected" in {
    val data = baseData - "rentReviewDetails.rentReviewResultsDetails.rentFixedBy"
    val res  = bind(data).convertGlobalToFieldErrors()
    mustContainError("rentReviewDetails.rentReviewResultsDetails.rentFixedBy", Errors.rentFixedByRequired, res)
  }

  "Page Seven Mapping" should "validate the Rent Interval duration" in
    validatesDuration("rentReviewDetails.reviewIntervalTypeSpecify", pageSevenForm, baseData, ".rentReviewIntervalOther")

  it should "validate the last review date" in
    validatePastDate("rentReviewDetails.lastReviewDate", pageSevenForm, baseData, ".lastRentReviewDate")

  it should "validate the rent review date" in
    validatePastDate("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview", pageSevenForm, baseData, ".rentResultOfReview")

  it should "not validate the rent interval duration when only leaseContainsRentReviews is specified" in {
    val data = Map(leaseContainsRentReviews)

    val res = bind(data).convertGlobalToFieldErrors()

    mustNotContainErrorFor("rentReviewDetails.reviewIntervalTypeSpecify.months", res)
  }
}
