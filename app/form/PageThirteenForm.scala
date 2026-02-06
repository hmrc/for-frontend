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

import models.serviceContracts.submissions.{PropertyAlterations, PropertyAlterationsDetails}
import play.api.data.Form
import play.api.data.Forms.*
import uk.gov.voa.play.form.ConditionalMappings.*
import uk.gov.voa.play.form.*
import DateMappings.*
import MappingSupport.*
import play.api.data.Mapping

object PageThirteenForm {

  val keys: Keys = new Keys

  class Keys {
    val propertyAlterations          = "propertyAlterations"
    val propertyAlterationsDetails   = "propertyAlterationsDetails"
    val alterationDetailsDescription = "propertyAlterationsDetails.description"
    val alterationDetailsCost        = "propertyAlterationsDetails.cost"
    val alterationDetailsDateMonth   = "propertyAlterationsDetails.date.month"
    val alterationDetailsDateYear    = "propertyAlterationsDetails.date.year"
    val alterationsRequired          = "requiredAnyWorks"
  }

  val pageThirteenForm: Form[PropertyAlterations] = Form(mapping(
    keys.propertyAlterations        -> mandatoryBooleanWithError(Errors.hasTenantDonePropertyAlterationsRequired),
    keys.propertyAlterationsDetails -> onlyIfTrue(
      keys.propertyAlterations,
      IndexedMapping("propertyAlterationsDetails", propertyAlterationsDetailsMapping).verifying(Errors.tooManyAlterations, _.length <= 10)
    ),
    keys.alterationsRequired        -> mandatoryBooleanIfTrue(
      keys.propertyAlterations,
      mandatoryBooleanWithError(Errors.tenantWasRequiredToMakeAlterationsRequired)
    )
  )(PropertyAlterations.apply)(o => Some(Tuple.fromProductTyped(o))))

  lazy val propertyAlterationsDetailsMapping: String => Mapping[PropertyAlterationsDetails] = (indexed: String) =>
    mapping(
      s"$indexed.date"           -> monthYearRoughDateMapping(s"$indexed.date", ".alternationCost"),
      s"$indexed.alterationType" -> alterationSetByTypeMapping,
      s"$indexed.cost"           -> currencyMapping(".alternationCost")
    )(PropertyAlterationsDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

}
