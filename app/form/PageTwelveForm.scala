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

import models.pages._
import models.serviceContracts.submissions.ChargeDetails
import play.api.data.Form
import play.api.data.Forms.{default, mapping, text}
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._
import MappingSupport._
import play.api.data.validation.Constraints.{maxLength, nonEmpty}
import play.api.data.Mapping

object PageTwelveForm {

  val chargeDetailsMapping: String => Mapping[ChargeDetails] = (index: String) =>
    mapping(
      s"$index.chargeDescription" -> default(text, "").verifying(
        nonEmpty(errorMessage = "error.detailsOfService.required"),
        maxLength(50, "error.detailsOfService.maxLength")
      ),
      s"$index.chargeCost"        -> currencyMapping(".serviceChargesPerYear")
    )(ChargeDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageTwelveMapping: Mapping[PageTwelve] = mapping(
    "responsibleOutsideRepairs"    -> responsibleTypeMapping,
    "responsibleInsideRepairs"     -> responsibleTypeMapping,
    "responsibleBuildingInsurance" -> responsibleTypeMapping,
    "ndrCharges"                   -> mandatoryBooleanWithError(Errors.businessRatesRequired),
    "ndrDetails"                   -> mandatoryIfTrue("ndrCharges", currencyMapping(".businessRatesPerYear")),
    "waterCharges"                 -> mandatoryBooleanWithError(Errors.waterChargesIncludedRequired),
    "waterChargesCost"             -> mandatoryIfTrue("waterCharges", currencyMapping(".waterChargesPerYear")),
    "includedServices"             -> mandatoryBooleanWithError(Errors.serviceChargesIncludedRequired),
    "includedServicesDetails"      -> onlyIfTrue(
      "includedServices",
      IndexedMapping("includedServicesDetails", chargeDetailsMapping).verifying(Errors.tooManyServices, _.length <= 8)
    )
  )(PageTwelve.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageTwelveForm: Form[PageTwelve] = Form(pageTwelveMapping)
}
