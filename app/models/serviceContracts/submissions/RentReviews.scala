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

package models.serviceContracts.submissions

import models.RoughDate
import play.api.libs.json.{Json, OFormat}

case class RentReviews(leaseContainsRentReviews: Boolean, rentReviewDetails: Option[RentReviewDetails])

object RentReviews:
  implicit val format: OFormat[RentReviews] = Json.format

case class RentReviewDetails(
  reviewIntervalTypeSpecify: Option[MonthsYearDuration],
  lastReviewDate: Option[RoughDate],
  canRentReduced: Boolean,
  rentResultOfRentReview: Boolean,
  reviewDetails: Option[RentReviewResultDetails]
)

object RentReviewDetails:
  implicit val format: OFormat[RentReviewDetails] = Json.format

case class RentReviewResultDetails(whenWasRentReview: RoughDate, rentAgreedBetween: Boolean, rentFixedBy: Option[RentFixedType])

object RentReviewResultDetails:
  implicit val format: OFormat[RentReviewResultDetails] = Json.format
