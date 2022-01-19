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

import form.DateMappings._
import form.Errors.rentReviewDetailsRequired
import form.MappingSupport._
import models.pages._
import models.serviceContracts.submissions.{RentReviewResultDetails, ReviewIntervalTypeOther}
import play.api.data.Forms.{mapping, optional}
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings._


object PageSevenForm {

  val rentReviewResultsDetailsMapping = mapping(
    "whenWasRentReview" -> monthYearRoughDateMapping("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview"),
    "rentAgreedBetween" -> mandatoryBooleanWithError(Errors.rentWasAgreedBetweenRequired),
    "rentFixedBy" -> mandatoryIfFalse("rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween", rentFixedByTypeMapping)
  )(RentReviewResultDetails.apply)(RentReviewResultDetails.unapply)

  val rentReviewDetailsMapping = mapping(
    "reviewIntervalType" -> reviewIntervalTypeMapping,
    "reviewIntervalTypeSpecify" ->
      mandatoryIfEqual("rentReviewDetails.reviewIntervalType", ReviewIntervalTypeOther.name,
        monthsYearDurationMapping("rentReviewDetails.reviewIntervalTypeSpecify")),
    "lastReviewDate" -> optional(monthYearRoughDateMapping("rentReviewDetails.lastReviewDate")),
    "canRentReduced" -> mandatoryBooleanWithError(Errors.rentCanBeReducedOnReviewRequired),
    "rentResultOfRentReview" -> mandatoryBooleanWithError(Errors.isRentResultOfReviewRequired),
    "rentReviewResultsDetails" -> mandatoryIfTrue("rentReviewDetails.rentResultOfRentReview", rentReviewResultsDetailsMapping)
  )(PageSevenDetails.apply)(PageSevenDetails.unapply)

  val pageSevenForm = Form(mapping(
    "leaseContainsRentReviews" -> mandatoryBooleanWithError(rentReviewDetailsRequired),
    "rentReviewDetails" -> mandatoryIfTrue("leaseContainsRentReviews", rentReviewDetailsMapping)
  )(PageSeven.apply)(PageSeven.unapply))

}
