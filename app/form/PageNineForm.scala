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

import form.mapping.ConditionalMappings.*
import models.pages.PageNine
import models.serviceContracts.submissions.RentBaseType
import play.api.data.Form
import play.api.data.Forms.{default, mapping, text}
import DateMappings.*
import MappingSupport.*
import play.api.data.validation.Constraints.{maxLength, nonEmpty}

object PageNineForm:

  val pageNineForm: Form[PageNine] =
    Form(
      mapping(
        "totalRent"          -> annualRent,
        "rentBecomePayable"  -> dateFieldsMapping("rentBecomePayable", fieldErrorPart = ".rentBecomePayable"),
        "rentActuallyAgreed" -> dateFieldsMapping("rentActuallyAgreed", fieldErrorPart = ".rentActuallyAgreed"),
        "negotiatingNewRent" -> mandatoryBooleanWithError(Errors.negotiatingNewRentRequired),
        "rentBasedOn"        -> rentBaseTypeMapping,
        "rentBasedOnDetails" -> mandatoryAndOnlyIfAnyOf(
          "rentBasedOn",
          Seq(RentBaseType.percentageOpenMarket.toString, RentBaseType.percentageTurnover.toString, RentBaseType.other.toString),
          default(text, "").verifying(
            nonEmpty(errorMessage = "error.rentBasedOnDetails.required"),
            maxLength(250, "error.rentBasedOnDetails.maxLength")
          )
        )
      )(PageNine.apply)(o => Some(Tuple.fromProductTyped(o)))
    )
