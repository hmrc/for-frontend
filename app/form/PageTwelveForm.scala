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

import models.pages._
import models.serviceContracts.submissions.ChargeDetails
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._
import MappingSupport._

object PageTwelveForm {
  val chargeDetailsMapping = (index: String) => mapping(
    s"$index.chargeDescription" -> nonEmptyText(maxLength = 50),
    s"$index.chargeCost" -> currency)(ChargeDetails.apply)(ChargeDetails.unapply)

  val pageTwelveMapping = mapping(
    "responsibleOutsideRepairs" -> responsibleTypeMapping,
    "responsibleInsideRepairs" -> responsibleTypeMapping,
    "responsibleBuildingInsurance" -> responsibleTypeMapping,
    "ndrCharges" -> mandatoryBooleanWithError(Errors.businessRatesRequired),
    "ndrDetails" -> mandatoryIfTrue("ndrCharges", currency),
    "waterCharges" -> mandatoryBooleanWithError(Errors.waterChargesIncludedRequired),
    "waterChargesCost" -> mandatoryIfTrue("waterCharges", currency),
    "includedServices" -> mandatoryBooleanWithError(Errors.serviceChargesIncludedRequired),
    "includedServicesDetails" -> onlyIfTrue(
      "includedServices", IndexedMapping("includedServicesDetails" , chargeDetailsMapping).verifying(Errors.tooManyServices, _.length <= 8)
    )
    )(PageTwelve.apply)(PageTwelve.unapply)

  val pageTwelveForm = Form(pageTwelveMapping)
}
