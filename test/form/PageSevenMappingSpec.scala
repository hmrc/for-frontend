/*
 * Copyright 2018 HM Revenue & Customs
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
import models.serviceContracts.submissions.ReviewIntervalTypeOther
import org.scalatest.{FlatSpec, Matchers}
import play.api.data.{Form, FormError}
import utils.FormBindingTestAssertions._

class PageSevenMappingSpec extends FlatSpec with Matchers {

  import PageSevenForm._
  import utils.MappingSpecs._

  val leaseContainsRentReviews = "leaseContainsRentReviews" -> "true"
  val reviewIntervalType = "rentReviewDetails.reviewIntervalType" -> ReviewIntervalTypeOther.name
  val reviewIntervalYears = "rentReviewDetails.reviewIntervalTypeSpecify.years" -> "11"
  val reviewIntervalMonths = "rentReviewDetails.reviewIntervalTypeSpecify.months" -> "12"
  val lastReviewDateMonth = "rentReviewDetails.lastReviewDate.month" -> "3"
  val lastReviewDateYear = "rentReviewDetails.lastReviewDate.year" -> "2000"
  val canRentReduced = "rentReviewDetails.canRentReduced" -> "true"
  val rentResultOfRentReview = "rentReviewDetails.rentResultOfRentReview" -> "true"
  val whenWasRentReviewMonth = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.month" -> "2"
  val whenWasRentReviewYear = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year" -> "2001"
  val rentAgreedBetween = "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween" -> "false"
  val rentFixedBy = "rentReviewDetails.rentReviewResultsDetails.rentFixedBy" -> "arbitrator"

  val baseData = Map(leaseContainsRentReviews,
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
    rentFixedBy)

  def bind(formData: Map[String, String]) = {
    pageSevenForm.bind(formData).convertGlobalToFieldErrors()
  }

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
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "rentReviewDetails.reviewIntervalType", "error.no_value_selected")
  }

  "PageSevenData" should "bind with the fields and return issues when the year field of the last review date is missing" in {
    val data = baseData - "rentReviewDetails.lastReviewDate.year"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "rentReviewDetails.lastReviewDate.year", "error.required")
  }
  "PageSevenData" should "bind with the fields and return issues when boolean can rent be reduced due to rent review value is missing" in {
    val data = baseData - "rentReviewDetails.canRentReduced"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "rentReviewDetails.canRentReduced", "error.boolean_missing")
  }

  "PageSevenData" should "bind with the fields and return issues when connection type selection missing" in {
    val data = baseData - "rentReviewDetails.rentResultOfRentReview"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    containsError(res.errors, "rentReviewDetails.rentResultOfRentReview", "error.boolean_missing")
  }

  "PageSevenData" should "bind with the fields and return issues when the date for the last effective rent review is missing" in {
    val data = baseData - "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year"
    val res = bind(data)
    res.errors.size should be(1)
    res.errors.isEmpty should be(false)
    containsError(res.errors, "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year", "error.required")
  }

  "PageSevenData" should "not bind with the fields and return an error when the person who agreed the rent input is not selected" in {
    val data = baseData - "rentReviewDetails.rentReviewResultsDetails.rentFixedBy"
    val res = bind(data).convertGlobalToFieldErrors()
    mustContainError("rentReviewDetails.rentReviewResultsDetails.rentFixedBy", Errors.noValueSelected, res)
  }

  "Page Seven Mapping" should "validate the Rent Interval duration" in {
    validatesDuration("rentReviewDetails.reviewIntervalTypeSpecify", pageSevenForm, baseData)
  }

  it should "validate the last review date" in {
    validatePastDate("rentReviewDetails.lastReviewDate", pageSevenForm, baseData)
  }

  it should "validate the rent review date" in {
    validatePastDate("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview", pageSevenForm, baseData)
  }

  it should "not validate the rent interval duration when only leaseContainsRentReviews is specified" in {
    val data = Map(leaseContainsRentReviews)

    val res = bind(data).convertGlobalToFieldErrors()

    mustNotContainErrorFor("rentReviewDetails.reviewIntervalTypeSpecify.months", res)
  }
}
