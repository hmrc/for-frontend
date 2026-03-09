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

object PageThirteenForm:

  private val propertyAlterations        = "propertyAlterations"
  private val propertyAlterationsDetails = "propertyAlterationsDetails"
  private val alterationsRequired        = "requiredAnyWorks"

  private val propertyAlterationsDetailsMapping: String => Mapping[PropertyAlterationsDetails] = (indexed: String) =>
    mapping(
      s"$indexed.date"           -> monthYearRoughDateMapping(s"$indexed.date", ".alternationCost"),
      s"$indexed.alterationType" -> alterationSetByTypeMapping,
      s"$indexed.cost"           -> currencyMapping(".alternationCost")
    )(PropertyAlterationsDetails.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageThirteenForm: Form[PropertyAlterations] =
    Form(
      mapping(
        propertyAlterations        -> mandatoryBooleanWithError(Errors.hasTenantDonePropertyAlterationsRequired),
        propertyAlterationsDetails -> onlyIfTrue(
          propertyAlterations,
          IndexedMapping("propertyAlterationsDetails", propertyAlterationsDetailsMapping).verifying(Errors.tooManyAlterations, _.length <= 10)
        ),
        alterationsRequired        -> mandatoryBooleanIfTrue(
          propertyAlterations,
          mandatoryBooleanWithError(Errors.tenantWasRequiredToMakeAlterationsRequired)
        )
      )(PropertyAlterations.apply)(o => Some(Tuple.fromProductTyped(o)))
    )
