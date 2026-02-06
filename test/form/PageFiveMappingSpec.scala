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

import models.pages.PageFive
import models.serviceContracts.submissions.LandlordConnectionTypeOther
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.Form

class PageFiveMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageFiveForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "PageFive form" should "bind with the fields and not return issues" in
    mustBind(bind(baseData))(_ => ())

  it should "require the field landlordFulName" in {
    val data = baseData - "landlordFullName"
    val form = bind(data)

    mustContainError("landlordFullName", "error.landlordFullName.required", form)
  }

  it should "allow letters, numbers, spaces and special chars with upto 50 chars for landlord's name" in
    validateFullName(pageFiveForm, baseData, "landlordFullName", Some("error.landlordFullName.maxLength"))

  it should "allow address to be optional" in {
    val data = baseData -- addressFields
    mustBind(bind(data))(x => assert(x.landlordAddress.isDefined === false))
  }

  it should "allow letters, numbers, spaced and special chars up to 100 chars for connection details" in
    validateLettersNumsSpecCharsUptoLength("landlordConnectText", 100, pageFiveForm, baseData, Some("error.landlordConnectText.maxLength"))

  it should "bind with the fields and return issues when connection type selection missing" in {
    val data = baseData - "landlordConnectType"
    val form = bind(data)

    mustOnlyContainError("landlordConnectType", Errors.LandlordConnectionTypeRequired, form)
  }

  it should "return required error if landlord connection text is missing and connection type is other" in {
    val data = baseData - "landlordConnectText"
    val form = bind(data)

    mustContainError("landlordConnectText", "error.landlordConnectText.required", form)
  }

  it should "bind with the fields and return with no errors" in {
    val data = baseData
    val form = bind(data)

    doesNotContainErrors(form)
  }

  it should "never return validation errors for address" in {
    val data = baseData
      .updated("original.landlordAddress.buildingNameNumber", "1")
      .updated("original.landlordAddress.street1", "The Road")
      .updated("original.landlordAddress.postcode", "AA11 1AA")
    val form = bind(data)

    doesNotContainErrors(form)
  }

  it should "not return validation errors for even when postcode alone is filled out" in {
    val data = baseData - addressBuildingName._1 - addressStreet1._1 - addressStreet2._1
    val form = bind(data)

    doesNotContainErrors(form)
  }

  object TestData {
    lazy val landlordFullName: (String, String)    = "landlordFullName"                   -> "Some Geezer"
    lazy val addressBuildingName: (String, String) = "landlordAddress.buildingNameNumber" -> "Our House"
    lazy val addressStreet1: (String, String)      = "landlordAddress.street1"            -> "Middle of Our street"
    lazy val addressStreet2: (String, String)      = "landlordAddress.street2"            -> "Our House"
    lazy val addressPostcode: (String, String)     = "landlordAddress.postcode"           -> "AA11 1AA"
    lazy val landlordConnType: (String, String)    = "landlordConnectType"                -> LandlordConnectionTypeOther.name
    lazy val landlordConnText: (String, String)    = "landlordConnectText"                -> "Fraternal bonds"

    val addressFields: Seq[String] = Seq(
      "landlordAddress.buildingNameNumber",
      "landlordAddress.street1",
      "landlordAddress.street2",
      "landlordAddress.postcode"
    )

    val baseData: Map[String, String] = Map(
      landlordFullName,
      addressBuildingName,
      addressStreet1,
      addressStreet2,
      addressPostcode,
      landlordConnType,
      landlordConnText
    )

    def bind(formData: Map[String, String]): Form[PageFive] =
      pageFiveForm.bind(formData).convertGlobalToFieldErrors()
  }

}
