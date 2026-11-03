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

case class IncentivesAndPayments(
  rentFreePeriod: Boolean,
  rentFreePeriodDetails: Option[FreePeriodDetails],
  payCapitalSum: Boolean,
  capitalPaidDetails: Option[CapitalDetails],
  receiveCapitalSum: Boolean,
  capitalReceivedDetails: Option[CapitalDetails]
)

object IncentivesAndPayments:
  implicit val format: OFormat[IncentivesAndPayments] = Json.format

case class FreePeriodDetails(lengthInMonths: Int, description: String)

object FreePeriodDetails:
  implicit val format: OFormat[FreePeriodDetails] = Json.format

case class CapitalDetails(capitalSum: BigDecimal, paymentDate: RoughDate)

object CapitalDetails:
  implicit val format: OFormat[CapitalDetails] = Json.format
