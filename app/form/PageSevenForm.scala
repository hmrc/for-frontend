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

import form.DateMappings._
import form.Errors.{lastReviewDateIsBeforeStartDate, rentReviewDateIsBeforeStartDate, rentReviewDetailsRequired}
import form.MappingSupport._
import models.pages._
import models.serviceContracts.submissions.{RentReviewResultDetails, ReviewIntervalTypeOther}
import play.api.data.Forms.{localDate, mapping, optional}
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings._

object PageSevenForm {

  val rentReviewResultsDetailsMapping: Mapping[RentReviewResultDetails] = mapping(
    "whenWasRentReview" -> dateIsBeforeAnotherDate(
      monthYearRoughDateMapping("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview", ".rentResultOfReview"),
      "agreementStartDate",
      rentReviewDateIsBeforeStartDate
    ),
    "rentAgreedBetween" -> mandatoryBooleanWithError(Errors.rentWasAgreedBetweenRequired),
    "rentFixedBy"       -> mandatoryIfFalse("rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween", rentFixedByTypeMapping)
  )(RentReviewResultDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  val rentReviewDetailsMapping: Mapping[PageSevenDetails] = mapping(
    "reviewIntervalType"        -> reviewIntervalTypeMapping,
    "reviewIntervalTypeSpecify" ->
      mandatoryIfEqual(
        "rentReviewDetails.reviewIntervalType",
        ReviewIntervalTypeOther.name,
        monthsYearDurationMapping("rentReviewDetails.reviewIntervalTypeSpecify", ".rentReviewIntervalOther")
      ),
    "lastReviewDate"            -> optional(
      dateIsBeforeAnotherDate(
        monthYearRoughDateMapping("rentReviewDetails.lastReviewDate", ".lastRentReviewDate"),
        "agreementStartDate",
        lastReviewDateIsBeforeStartDate
      )
    ),
    "canRentReduced"            -> mandatoryBooleanWithError(Errors.rentCanBeReducedOnReviewRequired),
    "rentResultOfRentReview"    -> mandatoryBooleanWithError(Errors.isRentResultOfReviewRequired),
    "rentReviewResultsDetails"  -> mandatoryIfTrue("rentReviewDetails.rentResultOfRentReview", rentReviewResultsDetailsMapping)
  )(PageSevenDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageSevenForm: Form[PageSeven] = Form(
    mapping(
      "leaseContainsRentReviews" -> mandatoryBooleanWithError(rentReviewDetailsRequired),
      "rentReviewDetails"        -> mandatoryIfTrue("leaseContainsRentReviews", rentReviewDetailsMapping),
      "agreementStartDate"       -> optional(localDate)
    )(PageSeven.apply)(o => Some(Tuple.fromProductTyped(o)))
  )

}
