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

import form.DateMappings._
import form.MappingSupport._
import models.serviceContracts.submissions.{Parking, WhatRentIncludes}
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{default, mapping, text}
import uk.gov.voa.play.form.ConditionalMappings._
import PageTenForm._
import play.api.data.validation.Constraints.{maxLength, nonEmpty}

object PageTenForm {

  object Keys {
    val partRent                   = "partRent"
    val partRentDetails            = "partRentDetails"
    val otherProperty              = "otherProperty"
    val otherPropertyDetails       = "otherPropertyDetails"
    val livingAccommodation        = "livingAccommodation"
    val livingAccommodationDetails = "livingAccommodationDetails"
    val landOnly                   = "landOnly"
    val landOnlyDetails            = "landOnlyDetails"
    val shellUnit                  = "shellUnit"
    val shellUnitDetails           = "shellUnitDetails"
    val rentDetails                = "rentDetails"
    val rentIncludeParking         = "rentIncludeParking"
    val rentIncludeParkingDetails  = "rentIncludeParkingDetails"
    val rentSeparateParkingDetails = "rentSeparateParkingDetails"
    val rentSeparateParking        = "rentSeparateParking"
    val annualSeparateParking      = "annualSeparateParking"
    val annualSeparateParkingDate  = "annualSeparateParkingDate"
  }

  val pageTenMapping: Mapping[WhatRentIncludes] = mapping(
    Keys.partRent            -> mandatoryBooleanWithError(Errors.isRentPaidForPartRequired),
    Keys.otherProperty       -> mandatoryBooleanWithError(Errors.anyOtherBusinessPropertyRequired),
    Keys.livingAccommodation -> mandatoryBooleanWithError(Errors.includesLivingAccommodationRequired),
    Keys.landOnly            -> mandatoryBooleanWithError(Errors.rentBasedOnLandOnlyRequired),
    Keys.shellUnit           -> mandatoryBooleanWithError(Errors.rentBasedOnEmptyBuildingRequired),
    Keys.rentDetails         -> mandatoryIfAnyAreTrue(
      Seq(Keys.shellUnit, Keys.landOnly, Keys.livingAccommodation, Keys.otherProperty, Keys.partRent),
      default(text, "").verifying(
        nonEmpty(errorMessage = "error.rentDetails.required"),
        maxLength(249, "error.rentDetails.maxLength")
      ),
      showNestedErrors = false
    ),
    "parking"                -> ParkingMapping.parkingMapping("parking")
  )(WhatRentIncludes.apply)(o => Some(Tuple.fromProductTyped(o)))

  val pageTenForm: Form[WhatRentIncludes] = Form(pageTenMapping)
}

object ParkingMapping {

  private def rentIncludeParkingMapping(prefix: String) = mandatoryIfTrue(
    prefix + Keys.rentIncludeParking,
    parkingDetailsMapping(s"$prefix${Keys.rentIncludeParkingDetails}")
  )

  private def rentSeparateParkingDetailsMapping(prefix: String) = mandatoryIfTrue(
    prefix + Keys.rentSeparateParking,
    parkingDetailsMapping(s"$prefix${Keys.rentSeparateParkingDetails}")
  )

  def parkingMapping(prefix: String): Mapping[Parking] = {
    val pfx = prefix + "."
    mapping(
      (Keys.rentIncludeParking, mandatoryBooleanWithError(Errors.includesParkingRequired)),
      (Keys.rentIncludeParkingDetails, rentIncludeParkingMapping(pfx)),
      (Keys.rentSeparateParking, mandatoryBooleanWithError(Errors.tenantPaysForParkingRequired)),
      (Keys.rentSeparateParkingDetails, rentSeparateParkingDetailsMapping(pfx)),
      (Keys.annualSeparateParking, mandatoryIfTrue(pfx + Keys.rentSeparateParking, currencyMapping(".annualSeparateParkingAmount"))),
      (
        Keys.annualSeparateParkingDate,
        mandatoryIfTrue(
          pfx + Keys.rentSeparateParking,
          monthYearRoughDateMapping(pfx + Keys.annualSeparateParkingDate, ".annualSeparateParkingDate")
        )
      )
    )(Parking.apply)(o => Some(Tuple.fromProductTyped(o)))
  }
}
