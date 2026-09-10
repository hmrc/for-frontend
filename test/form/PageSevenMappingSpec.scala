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

import form.PageSevenForm.pageSevenForm
import models.pages.PageSeven
import models.serviceContracts.submissions.ReviewIntervalType
import org.scalatest.Assertion
import play.api.data.{Form, FormError}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.FormBindingTestAssertions.*
import utils.MappingSpecs.*

class PageSevenMappingSpec extends BaseSpec:

  private val leaseContainsRentReviews = "leaseContainsRentReviews"                                           -> "true"
  private val reviewIntervalType       = "rentReviewDetails.reviewIntervalType"                               -> ReviewIntervalType.other.toString
  private val reviewIntervalYears      = "rentReviewDetails.reviewIntervalTypeSpecify.years"                  -> "11"
  private val reviewIntervalMonths     = "rentReviewDetails.reviewIntervalTypeSpecify.months"                 -> "12"
  private val lastReviewDateMonth      = "rentReviewDetails.lastReviewDate.month"                             -> "3"
  private val lastReviewDateYear       = "rentReviewDetails.lastReviewDate.year"                              -> "2000"
  private val canRentReduced           = "rentReviewDetails.canRentReduced"                                   -> "true"
  private val rentResultOfRentReview   = "rentReviewDetails.rentResultOfRentReview"                           -> "true"
  private val whenWasRentReviewMonth   = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.month" -> "2"
  private val whenWasRentReviewYear    = "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year"  -> "2001"
  private val rentAgreedBetween        = "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween"       -> "false"
  private val rentFixedBy              = "rentReviewDetails.rentReviewResultsDetails.rentFixedBy"             -> "arbitrator"

  private val baseData: Map[String, String] = Map(
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

  private def bind(formData: Map[String, String]): Form[PageSeven] =
    pageSevenForm.bind(formData).convertGlobalToFieldErrors()

  private def containsError(errors: Seq[FormError], key: String, message: String): Assertion =
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists shouldBe true

  "PageSevenForm" should {
    "bind with the fields and not return issues" in {
      val res = bind(baseData)

      doesNotContainErrors(res)
    }

    "bind with the fields and return issues when review frequency is not selected" in {
      val data = baseData - "rentReviewDetails.reviewIntervalType"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      containsError(res.errors, "rentReviewDetails.reviewIntervalType", Errors.rentReviewFrequencyRequired)
    }

    "bind with the fields and return issues when the year field of the last review date is missing" in {
      val data = baseData - "rentReviewDetails.lastReviewDate.year"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      mustContainError("rentReviewDetails.lastReviewDate.year", "error.lastRentReviewDate.year.required", res)
    }

    "bind with the fields and return issues when boolean can rent be reduced due to rent review value is missing" in {
      val data = baseData - "rentReviewDetails.canRentReduced"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      containsError(res.errors, "rentReviewDetails.canRentReduced", Errors.rentCanBeReducedOnReviewRequired)
    }

    "bind with the fields and return issues when connection type selection missing" in {
      val data = baseData - "rentReviewDetails.rentResultOfRentReview"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      containsError(res.errors, "rentReviewDetails.rentResultOfRentReview", Errors.isRentResultOfReviewRequired)
    }

    "bind with the fields and return issues when the date for the last effective rent review is missing" in {
      val data = baseData - "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year"
      val res  = bind(data)

      res.errors.size    shouldBe 1
      res.errors.isEmpty shouldBe false
      mustContainError("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year", "error.rentResultOfReview.year.required", res)
    }

    "not bind with the fields and return an error when the person who agreed the rent input is not selected" in {
      val data = baseData - "rentReviewDetails.rentReviewResultsDetails.rentFixedBy"
      val res  = bind(data).convertGlobalToFieldErrors()

      mustContainError("rentReviewDetails.rentReviewResultsDetails.rentFixedBy", Errors.rentFixedByRequired, res)
    }

    "validate the Rent Interval duration" in
      validatesDuration("rentReviewDetails.reviewIntervalTypeSpecify", pageSevenForm, baseData, ".rentReviewIntervalOther")

    "validate the last review date" in
      validatePastDate("rentReviewDetails.lastReviewDate", pageSevenForm, baseData, ".lastRentReviewDate")

    "validate the rent review date" in
      validatePastDate("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview", pageSevenForm, baseData, ".rentResultOfReview")

    "not validate the rent interval duration when only leaseContainsRentReviews is specified" in {
      val data = Map(leaseContainsRentReviews)
      val res  = bind(data).convertGlobalToFieldErrors()

      mustNotContainErrorFor("rentReviewDetails.reviewIntervalTypeSpecify.months", res)
    }
  }
