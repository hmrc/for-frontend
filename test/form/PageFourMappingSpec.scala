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

import models._
import models.pages._
import models.serviceContracts.submissions.{Address, SubletAll, SubletPart}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.FormError

class PageFourMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageFourForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "Page four mapping" should "validate sublet tenant address when sublet is true and is tenants address" in {
    validateAddress(pageFourForm, fullData, "sublet[0].tenantAddress")
  }

  it should "allow letters, numbers, spaces and special chars with upto 50 chars for a name" in {
    validateFullName(pageFourForm, fullData, keys.tenantFullName, Some("error.sublet.tenantFullName.maxLength"))
  }

  it should "allow letters, numbers, spaced and special chars upto 100 chars for property part sublet" in {
    validateLettersNumsSpecCharsUptoLength(keys.subletPropertyPartDescription, 100, pageFourForm, fullData,
      Some("error.subletPropertyPartDescription.maxLength"))
  }

  it should "allow letters, numbers, spaced and special chars upto 100 chars for property part sublet reason" in {
    validateLettersNumsSpecCharsUptoLength(keys.subletPropertyReasonDescription, 100, pageFourForm, fullData,
      Some("error.subletPropertyReasonDescription.maxLength"))
  }

  it should "validate the annual rent as a valid annual rent" in {
    validateCurrency(keys.annualRentExcludingVat, pageFourForm, fullData, ".sublet.annualRent")
  }

  it should "only allow valid dates for the rent fixed date" in {
    validatePastDate(keys.rentFixedDate, pageFourForm, fullData, ".sublet.rentFixedDate")
  }

  it should "return a required error for sublet type" in {
    val data = fullData - keys.subletType
    val form = bind(data)

    mustContainError(keys.subletType, Errors.subletTypeRequired, form)
  }

  it should "not return required error for sublet information when we sublet whole property" in {
    val data = (fullData - keys.subletPropertyPartDescription) + (keys.subletType -> SubletAll.name)
    val form = bind(data)

    form.errors should be(empty)

  }

  it should "return required error fields for all sublet information when there is a sublet" in {
    val data = Map(keys.propertyIsSublet -> "true")
    val form = bind(data)

    mustContainError(keys.rentFixedDateMonth, "error.sublet.rentFixedDate.month.required", form)
    mustContainError(keys.rentFixedDateYear, "error.sublet.rentFixedDate.year.required", form)
    mustContainError(keys.subletPropertyReasonDescription, "error.subletPropertyReasonDescription.required", form)
    mustContainError(keys.subletType, Errors.subletTypeRequired, form)
  }

  it should "return a required error for sublet tenant address when sublet address is tenants address" in {
    val data = fullData -- allAddressFields
    val form = bind(data)

    mustContainError(keys.addrBuildingNameNumber, "error.buildingNameNumber.required", form)
    mustContainError(keys.addrPostcode, "error.postcode.required", form)
    mustContainError(keys.tenantFullName, "error.sublet.tenantFullName.required", form)
  }

  it should "bind to a PageFourData with no sublet information when the property is not sublet" in {
    val expectedData = PageFour(propertyIsSublet = false, List.empty)
    val form = bind(fullData.updated(keys.propertyIsSublet, "false"))

    mustBind(form) { data => data should be(expectedData) }
  }

  it should "bind to a PageFourData with full sublet information when the property is sublet" in {
    val expectedSubletData = SubletDetails(tenantFullName = "Korky the Cat",
      tenantAddress = Address("12", Some("Some Street"), Some("Some Place"), "AA11 1AA"),
      subletPropertyPartDescription = Option("basement flat"),
      subletPropertyReasonDescription = "residential",
      annualRent = BigDecimal(123.45),
      rentFixedDate = new RoughDate(2, 2015),
      subletType = SubletPart
    )

    val expectedData = PageFour(propertyIsSublet = true, List(expectedSubletData))

    val form = bind(fullData.updated(keys.propertyIsSublet, "true"))

    mustBind(form) { data => data should be(expectedData) }
  }

  it should "return a required error for tenant full name when property is sublet but tenant full name is missing" in {
    val submittedData = fullData - keys.tenantFullName
    val form = bind(submittedData)

    mustContainError(keys.tenantFullName, "error.sublet.tenantFullName.required", form)
  }

  it should "result in a validation error when the part of property is sublet but Property Part Description is missing" in {
    val submittedData = fullData - keys.subletPropertyPartDescription
    val res = bind(submittedData)

    res.errors should not be empty
    res.errors should have size(1)

    hasError(res.errors, keys.subletPropertyPartDescription, Errors.required)
    res.value.isDefined should be(false)
    res.data should be(submittedData)
  }

  it should "result in a validation error when the property is sublet but Property Reason Description is missing" in {
    val submittedData = fullData - keys.subletPropertyReasonDescription
    val res = bind(submittedData)

    res.errors.isEmpty should be(false)
    res.errors.size should be(1)

    hasError(res.errors, keys.subletPropertyReasonDescription, Errors.required)
    res.value.isDefined should be(false)
    res.data should be(submittedData)
  }

  it should "return a required error for annual rent when the property is sublet and rent length type is supplied but annual rent is missing" in {
    val submittedData = fullData - keys.annualRentExcludingVat
    val form = bind(submittedData)

    mustContainError(keys.annualRentExcludingVat, "error.required.sublet.annualRent", form)
  }

  it should "result in a single validation error when the property is sublet but both rent fixed fields are missing" in {
    val submittedData = fullData - keys.rentFixedDateMonth - keys.rentFixedDateYear
    val res = bind(submittedData)

    res.errors.isEmpty should be(false)
    res.errors.size should be(2)

    hasError(res.errors, keys.rentFixedDateYear, Errors.required)
    res.value.isDefined should be(false)
    res.data should be(submittedData)
  }

  it should "not allow more than 5 sublets" in {
    val with5SteppedRents = addSublets(4, fullData)
    mustBind(bind(with5SteppedRents)) { _ => () }

    val with6SteppedRents = addSublets(5, fullData)
    val form = bind(with6SteppedRents)
    mustOnlyContainError("sublet", Errors.tooManySublets, form)
  }

  object TestData {
    val keys = new {
      val propertyIsSublet = "propertyIsSublet"
      val addrBuildingNameNumber = "sublet[0].tenantAddress.buildingNameNumber"
      val addrStreet1 = "sublet[0].tenantAddress.street1"
      val addrStreet2 = "sublet[0].tenantAddress.street2"
      val addrPostcode = "sublet[0].tenantAddress.postcode"
      val tenantFullName = "sublet[0].tenantFullName"
      val subletType = "sublet[0].subletType"
      val subletPropertyPartDescription = "sublet[0].subletPropertyPartDescription"
      val subletPropertyReasonDescription = "sublet[0].subletPropertyReasonDescription"
      val annualRentExcludingVat = "sublet[0].annualRent"
      val rentFixedDate = "sublet[0].rentFixedDate"
      val rentFixedDateMonth = "sublet[0].rentFixedDate.month"
      val rentFixedDateYear = "sublet[0].rentFixedDate.year"
    }

    val mandatoryAddressFields = Seq(keys.addrBuildingNameNumber, keys.addrPostcode, keys.tenantFullName)
    val allAddressFields = mandatoryAddressFields ++ Seq(keys.addrStreet1, keys.addrStreet2)

    def bind(dataMap: Map[String, String]) = {
      val bound = pageFourForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    val fullData = Map(
      keys.propertyIsSublet -> "true",
      keys.addrBuildingNameNumber -> "12",
      keys.addrStreet1 -> "Some Street",
      keys.addrStreet2 -> "Some Place",
      keys.addrPostcode -> "AA11 1AA",
      keys.subletType -> "part",
      keys.tenantFullName -> "Korky the Cat",
      keys.subletPropertyPartDescription -> "basement flat",
      keys.subletPropertyReasonDescription -> "residential",
      keys.annualRentExcludingVat -> "123.45",
      keys.rentFixedDateMonth -> "2",
      keys.rentFixedDateYear -> "2015"
      )

    def addSublets(amount: Int, data: Map[String, String]): Map[String, String] = {
      (1 to amount).foldLeft(data) { (d, n) =>
        d.updated(keys.addrBuildingNameNumber.replace("[0]", s"[$n]"), data(keys.addrBuildingNameNumber))
         .updated(keys.addrStreet1.replace("[0]", s"[$n]"), data(keys.addrStreet1))
         .updated(keys.addrStreet2.replace("[0]", s"[$n]"), data(keys.addrStreet2))
         .updated(keys.addrPostcode.replace("[0]", s"[$n]"), data(keys.addrPostcode))
         .updated(keys.tenantFullName.replace("[0]", s"[$n]"), data(keys.tenantFullName))
          .updated(keys.subletType.replace("[0]", s"[$n]"), data(keys.subletType))
         .updated(keys.subletPropertyPartDescription.replace("[0]", s"[$n]"), data(keys.subletPropertyPartDescription))
         .updated(keys.subletPropertyReasonDescription.replace("[0]", s"[$n]"), data(keys.subletPropertyReasonDescription))
         .updated(keys.annualRentExcludingVat.replace("[0]", s"[$n]"), data(keys.annualRentExcludingVat))
         .updated(keys.rentFixedDateMonth.replace("[0]", s"[$n]"), data(keys.rentFixedDateMonth))
         .updated(keys.rentFixedDateYear.replace("[0]", s"[$n]"), data(keys.rentFixedDateYear))

      }
    }
    def hasError(errors: Seq[FormError], matcher: FormError => Boolean): Boolean = {
      errors.exists(matcher)
    }

    def hasError(errors: Seq[FormError], key: String, message: String) = {
      errors.exists { err => err.key == key && err.messages.contains(message) }
    }
  }

}
