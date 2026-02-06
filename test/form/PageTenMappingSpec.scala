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

import models.*
import models.serviceContracts.submissions.{Parking, ParkingDetails, WhatRentIncludes}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.{Form, FormError}
import org.scalatest.Assertion

class PageTenMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageTenForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "A page ten form" should "bind to what rent includes" in {
    val expectedData = WhatRentIncludes(
      partRent = true,
      otherProperty = true,
      livingAccommodation = true,
      landOnly = true,
      shellUnit = true,
      rentDetails = Some("RENT DETAILS"),
      Parking(
        true,
        Some(ParkingDetails(0, 0, 2)),
        true,
        Some(ParkingDetails(0, 0, 9)),
        Some(599.84),
        Some(RoughDate(None, Some(6), 2012))
      )
    )

    val form = bind(fullData)
    mustBind(form)(x => assert(x === expectedData))
  }

  it should "never allow empty parking values when parking is specified" in {
    val f = bind(realExampleData)
    mustContainError("parking.rentIncludeParkingDetails", "error.required.parking.rentIncludeParkingDetails", f)
    mustContainError("parking.rentSeparateParkingDetails", "error.required.parking.rentSeparateParkingDetails", f)
    mustContainError("parking.annualSeparateParking", "error.required.annualSeparateParkingAmount", f)
    mustContainError("parking.annualSeparateParkingDate.month", "error.annualSeparateParkingDate.month.required", f)
    mustContainError("parking.annualSeparateParkingDate.year", "error.annualSeparateParkingDate.year.required", f)
  }

  it should "return a mandatory error when a value for rentIncludeParking is not supplied" in {
    val data: Map[String, String] = Map.empty
    val form                      = bind(data)

    mustContainError(rentIncludeParkingKey, Errors.includesParkingRequired, form)
  }

  it should "return a mandatory boolean error when a value for rentSeparateParking is not supplied" in {
    val data = fullData - rentSeparateParkingKey
    val form = bind(data)

    mustContainError(rentSeparateParkingKey, Errors.tenantPaysForParkingRequired, form)

  }

  it should "return a required field error when the rent included parking details are all 0" in {
    val data = fullData.updated(rentIncludedParkingGarages, "0")
      .updated(rentIncludedParkingOpen, "0")
      .updated(rentIncludedParkingCovered, "0")
    val form = bind(data)

    mustContainError(rentIncludedParkingDetailsPrefix, "error.required.parking.rentIncludeParkingDetails", form)
  }

  it should "return a required field error when the rent included parking details are all empty" in {
    val data = fullData.updated(rentIncludedParkingGarages, "")
      .updated(rentIncludedParkingOpen, "")
      .updated(rentIncludedParkingCovered, "")
    val form = bind(data)

    mustContainError(rentIncludedParkingDetailsPrefix, "error.required.parking.rentIncludeParkingDetails", form)
  }

  it should "allow up to 249 letters, numbers, spaces, and special characters for rent details" in
    validateLettersNumsSpecCharsUptoLength(Keys.rentDetails, 249, pageTenForm, fullData, Some("error.rentDetails.maxLength"))

  it should "allow upto 4 digits for all car parking quantities" in {
    validateUptoNDigits(
      rentIncludedParkingGarages,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentIncludeParkingDetails.garages"),
      Some("error.invalid_number.parking.rentIncludeParkingDetails.garages")
    )
    validateUptoNDigits(
      rentIncludedParkingOpen,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentIncludeParkingDetails.openSpaces"),
      Some("error.invalid_number.parking.rentIncludeParkingDetails.openSpaces")
    )
    validateUptoNDigits(
      rentIncludedParkingCovered,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentIncludeParkingDetails.coveredSpaces"),
      Some("error.invalid_number.parking.rentIncludeParkingDetails.coveredSpaces")
    )

    validateUptoNDigits(
      rentSeparateParkingGarages,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentSeparateParkingDetails.garages"),
      Some("error.invalid_number.parking.rentSeparateParkingDetails.garages")
    )
    validateUptoNDigits(
      rentSeparateParkingOpen,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentSeparateParkingDetails.openSpaces"),
      Some("error.invalid_number.parking.rentSeparateParkingDetails.openSpaces")
    )
    validateUptoNDigits(
      rentSeparateParkingCovered,
      4,
      pageTenForm,
      fullData,
      Some("error.maxValue.parking.rentSeparateParkingDetails.coveredSpaces"),
      Some("error.invalid_number.parking.rentSeparateParkingDetails.coveredSpaces")
    )
  }

  it should "validate annual payment as 9 digits and 2 decimals" in
    validateCurrency(annualSeparateParking, pageTenForm, fullData, ".annualSeparateParkingAmount")

  it should "validate annual separate parking date as a date in the past" in
    validatePastDate(annualSeparateParkingPaymentFixedDate, pageTenForm, fullData, ".annualSeparateParkingDate")

  it should "return a required error when rent details are required but not given" in {
    val fields = Seq(Keys.partRent, Keys.otherProperty, Keys.livingAccommodation, Keys.landOnly, Keys.shellUnit)

    fields.foreach { field =>
      val data = dataNoDetailsRequired.updated(field, "true") - Keys.rentDetails
      val form = bind(data)

      mustContainError(Keys.rentDetails, "error.rentDetails.required", form)
    }
  }

  it should "bind without errors when the rent details are not required and not given" in {
    val form = bind(dataNoDetailsRequired - Keys.rentDetails)

    doesNotContainErrors(form)
  }

  "when rentIncludeParking is true but no rent included parking details have been supplied" should "return a required error for rentIncludeParkingDetails" in {
    val data = fullData - rentIncludedParkingGarages - rentSeparateParkingOpen - rentIncludedParkingCovered
    val form = bind(data)

    mustContainError(rentIncludedParkingDetailsPrefix, "error.required.parking.rentIncludeParkingDetails", form)
  }

  "when rentSeparateParking is true but no rent separate or annual parking details have been supplied" should
    "return a required error for rentSeparateParkingDetails" in {
      val data = fullData - rentSeparateParkingGarages - annualSeparateParking
      val form = bind(data)

      mustContainError(rentSeparateParkingDetailsPrefix, "error.required.parking.rentSeparateParkingDetails", form)
      mustContainError(annualSeparateParking, "error.required.annualSeparateParkingAmount", form)
    }

  "When rentSeparateParking is true but no annual separate parking amount has been supplied" should "return a required error for annualSeparateParking" in {
    val data = fullData - annualSeparateParking
    val form = bind(data)

    mustContainError(annualSeparateParking, "error.required.annualSeparateParkingAmount", form)
  }

  "When rentSeparateParking is true but no payment fixed date is supplied" should "return a required error for annual separate parking payment fixed date" in {
    val data = fullData - annualSeparateParkingMonths - annualSeparateParkingYear
    val form = bind(data)

    mustContainError(annualSeparateParkingMonths, "error.annualSeparateParkingDate.month.required", form)
    mustContainError(annualSeparateParkingYear, "error.annualSeparateParkingDate.year.required", form)
  }

  checkMissingField(Keys.partRent, Errors.isRentPaidForPartRequired)
  checkMissingField(Keys.otherProperty, Errors.anyOtherBusinessPropertyRequired)
  checkMissingField(Keys.livingAccommodation, Errors.includesLivingAccommodationRequired)
  checkMissingField(Keys.landOnly, Errors.rentBasedOnLandOnlyRequired)
  checkMissingField(Keys.shellUnit, Errors.rentBasedOnEmptyBuildingRequired)

  val fields: Seq[String] = Seq(Keys.partRent, Keys.otherProperty, Keys.livingAccommodation, Keys.landOnly, Keys.shellUnit)

  object TestData {

    def bind(dataMap: Map[String, String]): Form[WhatRentIncludes] = {
      val bound = pageTenForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    lazy val rentIncludeParkingKey                 = "parking.rentIncludeParking"
    lazy val rentIncludedParkingDetailsPrefix      = "parking.rentIncludeParkingDetails"
    lazy val rentIncludedParkingOpen               = "parking.rentIncludeParkingDetails.openSpaces"
    lazy val rentIncludedParkingGarages            = "parking.rentIncludeParkingDetails.garages"
    lazy val rentIncludedParkingCovered            = "parking.rentIncludeParkingDetails.coveredSpaces"
    lazy val rentSeparateParkingKey                = "parking.rentSeparateParking"
    lazy val rentSeparateParkingDetailsPrefix      = "parking.rentSeparateParkingDetails"
    lazy val rentSeparateParkingGarages            = "parking.rentSeparateParkingDetails.garages"
    lazy val rentSeparateParkingOpen               = "parking.rentSeparateParkingDetails.openSpaces"
    lazy val rentSeparateParkingCovered            = "parking.rentSeparateParkingDetails.coveredSpaces"
    lazy val annualSeparateParking                 = "parking.annualSeparateParking"
    lazy val annualSeparateParkingPaymentFixedDate = "parking.annualSeparateParkingDate"
    lazy val annualSeparateParkingMonths           = "parking.annualSeparateParkingDate.month"
    lazy val annualSeparateParkingYear             = "parking.annualSeparateParkingDate.year"

    val fullData: Map[String, String] = Map(
      Keys.partRent               -> "true",
      Keys.otherProperty          -> "true",
      Keys.livingAccommodation    -> "true",
      Keys.landOnly               -> "true",
      Keys.shellUnit              -> "true",
      Keys.rentDetails            -> "RENT DETAILS",
      rentIncludeParkingKey       -> "true",
      rentIncludedParkingGarages  -> "2",
      rentSeparateParkingKey      -> "true",
      rentSeparateParkingGarages  -> "9",
      annualSeparateParking       -> "599.84",
      rentIncludeParkingKey       -> "true",
      rentIncludedParkingGarages  -> "2",
      rentSeparateParkingKey      -> "true",
      rentSeparateParkingGarages  -> "9",
      annualSeparateParking       -> "599.84",
      annualSeparateParkingMonths -> "6",
      annualSeparateParkingYear   -> "2012"
    )

    val realExampleData: Map[String, String] = Map(
      "partRent"                    -> "false",
      "otherProperty"               -> "false",
      "livingAccommodation"         -> "false",
      "landOnly"                    -> "false",
      "shellUnit"                   -> "false",
      "parking.rentIncludeParking"  -> "true",
      "parking.rentSeparateParking" -> "true"
    )

    lazy val dataNoDetailsRequired: Map[String, String] = fullData.updated(Keys.partRent, "false").updated(Keys.otherProperty, "false").updated(
      Keys.livingAccommodation,
      "false"
    ).updated(Keys.landOnly, "false").updated(Keys.shellUnit, "false")

    def hasError(errors: Seq[FormError], key: String, message: String): Assertion = {
      val res = errors.exists(err => err.key == key && err.messages.contains(message))
      res should be(true)
    }

    def checkMissingField(key: String, code: String = Errors.required): Unit =
      "a  form missing the " + key + " field" should "bind result in 1 validation error" in {
        val testData = fullData - key
        val res      = bind(testData)
        res.hasErrors   should be(true)
        res.errors.size should be(1)
        hasError(res.errors, key, code)
      }
  }
}
