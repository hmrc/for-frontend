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

import models.serviceContracts.submissions.{CapitalDetails, FreePeriodDetails, IncentivesAndPayments}
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._
import MappingSupport._
import DateMappings._
import play.api.data.validation.Constraints.{maxLength, nonEmpty}
import play.api.data.Mapping

object PageElevenForm {

  val freePeriodDetailsMapping: Mapping[FreePeriodDetails] = mapping(
    "rentFreePeriodLength"  -> intMapping(),
    "rentFreePeriodDetails" -> default(text, "").verifying(
      nonEmpty(errorMessage = "error.rentFreePeriod.required"),
      maxLength(250, "error.rentFreePeriod.maxLength")
    )
  )(FreePeriodDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  private def capitalDetailsMapping(prefix: String) = mapping(
    "capitalSum"  -> currencyMapping(".paid"),
    "paymentDate" -> monthYearRoughDateMapping(s"$prefix.paymentDate", ".made")
  )(CapitalDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  private def capitalDetailsReceivedMapping(prefix: String) = mapping(
    "receivedSum" -> currencyMapping(".received"),
    "paymentDate" -> monthYearRoughDateMapping(s"$prefix.paymentDate", ".received")
  )(CapitalDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageElevenMapping: Mapping[IncentivesAndPayments] = mapping(
    "rentFreePeriod"         -> mandatoryBooleanWithError(Errors.rentFreePeriodRequired),
    "rentFreePeriodDetails"  -> mandatoryIfTrue("rentFreePeriod", freePeriodDetailsMapping),
    "payCapitalSum"          -> mandatoryBooleanWithError(Errors.paidCapitalSumRequired),
    "capitalPaidDetails"     -> mandatoryIfTrue("payCapitalSum", capitalDetailsMapping("capitalPaidDetails")),
    "receiveCapitalSum"      -> mandatoryBooleanWithError(Errors.receivedCapitalSumRequired),
    "capitalReceivedDetails" -> mandatoryIfTrue("receiveCapitalSum", capitalDetailsReceivedMapping("capitalReceivedDetails"))
  )(IncentivesAndPayments.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageElevenForm: Form[IncentivesAndPayments] = Form(pageElevenMapping)
}
