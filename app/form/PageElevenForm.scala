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

import models.serviceContracts.submissions.{CapitalDetails, FreePeriodDetails, IncentivesAndPayments}
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._
import MappingSupport._
import DateMappings._

object PageElevenForm {

  val freePeriodDetailsMapping = mapping(
    "rentFreePeriodLength" -> number(min = 1),
    "rentFreePeriodDetails" -> nonEmptyText(maxLength = 250))(FreePeriodDetails.apply)(FreePeriodDetails.unapply)

  private def capitalDetailsMapping(prefix: String) = mapping(
    "capitalSum" -> currency,
    "paymentDate" -> monthYearRoughDateMapping(s"$prefix.paymentDate"))(CapitalDetails.apply)(CapitalDetails.unapply)


  val pageElevenMapping = mapping(
    "rentFreePeriod" -> mandatoryBooleanWithError(Errors.rentFreePeriodRequired),
    "rentFreePeriodDetails" -> mandatoryIfTrue("rentFreePeriod", freePeriodDetailsMapping),
    "payCapitalSum" -> mandatoryBooleanWithError(Errors.paidCapitalSumRequired),
    "capitalPaidDetails" -> mandatoryIfTrue("payCapitalSum", capitalDetailsMapping("capitalPaidDetails")),
    "receiveCapitalSum" -> mandatoryBooleanWithError(Errors.receivedCapitalSumRequired),
    "capitalReceivedDetails" -> mandatoryIfTrue("receiveCapitalSum", capitalDetailsMapping("capitalReceivedDetails"))
    )(IncentivesAndPayments.apply)(IncentivesAndPayments.unapply)

  val pageElevenForm = Form(pageElevenMapping)
}
