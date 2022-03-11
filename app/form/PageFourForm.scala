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
import models.pages.{PageFour, _}
import play.api.data.Form
import play.api.data.Forms.{default, mapping, text}
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._
import MappingSupport._
import models.serviceContracts.submissions.SubletPart
import play.api.data.validation.Constraints.{maxLength, nonEmpty}

object PageFourForm {

  val nonMandatoryFields = Seq("sublet.annualRentExcludingVat")

  val subletMapping = (index: String) => mapping(
    s"$index.tenantFullName" -> default(text, "").verifying(
      nonEmpty(errorMessage = "error.sublet.tenantFullName.required"),
      maxLength(50, "error.sublet.tenantFullName.maxLength")
    ),
    s"$index.tenantAddress" ->  addressMapping(s"$index.tenantAddress"),
    s"$index.subletType" ->  subletTypeMapping,
    s"$index.subletPropertyPartDescription" -> mandatoryIf(
      isEqual(s"$index.subletType", SubletPart.name), default(text, "").verifying(
        nonEmpty(errorMessage = "error.subletPropertyPartDescription.required"),
        maxLength(100, "error.subletPropertyPartDescription.maxLength")
      )),
    s"$index.subletPropertyReasonDescription" -> default(text, "").verifying(
      nonEmpty(errorMessage = "error.subletPropertyReasonDescription.required"),
      maxLength(100, "error.subletPropertyReasonDescription.maxLength")
    ),
    s"$index.annualRent" -> currencyMapping(".sublet.annualRent"),
    s"$index.rentFixedDate" -> monthYearRoughDateMapping(s"$index.rentFixedDate", ".sublet.rentFixedDate")
  )(SubletDetails.apply)(SubletDetails.unapply)

  val pageFourMapping  = mapping(
    "propertyIsSublet" -> mandatoryBooleanWithError(Errors.propertyIsSublet),
    "sublet" -> onlyIfTrue("propertyIsSublet",
      IndexedMapping("sublet", subletMapping, allowEmpty = false, alwaysValidateFirstIndex = true).verifying(Errors.tooManySublets, _.length <= 5)
    )
  )(PageFour.apply)(PageFour.unapply)

  val pageFourForm =Form(pageFourMapping)

}
