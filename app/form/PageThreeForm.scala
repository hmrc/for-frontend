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
import models.pages.PageThree
import models.serviceContracts.submissions.{OccupierTypeCompany, OccupierTypeIndividuals}
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._
import MappingSupport._
import form.Errors.{propertyOwnedByYouRequired, propertyRentedByYouRequired,
}
import play.api.data.validation.Constraints.{maxLength, nonEmpty}

object PageThreeForm {

  val keys = new {
    val propertyType = "propertyType"
    val occupierType = "occupierType"
    val occupierCompanyName = "occupierCompanyName"
    val occupierCompanyContact = "occupierCompanyContact"
    val firstOccupationDate = "firstOccupationDate"
    val mainOccupierName = "mainOccupierName"
    val propertyOwnedByYou = "propertyOwnedByYou"
    val propertyRentedByYou = "propertyRentedByYou"
    val noRentDetails =  "noRentDetails"
  }

  private val basePageThreeMapping = mapping(
    "propertyType" -> default(text, "").verifying(
      nonEmpty(errorMessage = Errors.propertyTypeRequired),
      maxLength(100, "error.propertyType.maxLength")
    ),
    keys.occupierType -> occupierType,

    keys.occupierCompanyName -> mandatoryIfEqual(
      keys.occupierType,
      OccupierTypeCompany.name,
      default(text, "").verifying(
        nonEmpty(errorMessage = Errors.companyNameRequired),
        maxLength(50, "error.companyName.maxLength")
      )),

    keys.occupierCompanyContact -> onlyIf(isEqual(keys.occupierType, OccupierTypeCompany.name), optional(text(maxLength = 50))),

    keys.firstOccupationDate ->
      mandatoryIfEqualToAny(
        keys.occupierType,
        Seq(OccupierTypeCompany.name,OccupierTypeIndividuals.name),
        monthYearRoughDateMapping(keys.firstOccupationDate),
      ),

    keys.mainOccupierName -> mandatoryIfEqual(
      keys.occupierType,
      OccupierTypeIndividuals.name,
      default(text, "").verifying(
        nonEmpty(errorMessage = Errors.occupiersNameRequired),
        maxLength(50, "error.occupiersName.maxLength")
      )),

    keys.propertyOwnedByYou -> mandatoryBooleanWithError(propertyOwnedByYouRequired),

    keys.propertyRentedByYou -> mandatoryIfFalse(keys.propertyOwnedByYou, mandatoryBooleanWithError(propertyRentedByYouRequired)),

    keys.noRentDetails -> mandatoryIfFalse(
      keys.propertyRentedByYou,
      default(text, "").verifying(
        nonEmpty(errorMessage = Errors.noRentDetailsRequired),
        maxLength(249, "error.noRentDetails.maxLength")
      ))

  )(PageThree.apply)(PageThree.unapply)

  val pageThreeForm = Form(basePageThreeMapping)
}
