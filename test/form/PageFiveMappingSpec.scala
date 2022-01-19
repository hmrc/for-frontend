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

import models.serviceContracts.submissions.LandlordConnectionTypeOther
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PageFiveMappingSpec extends AnyFlatSpec with should.Matchers {

  import PageFiveForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  "PageFive form" should "bind with the fields and not return issues" in {
    mustBind(bind(baseData)) { _ => () }
  }

  it should "bind with the fields landlordFulName and does not return any errors" in {
    val data = baseData - "landlordFullName"
    val form = bind(data)

    doesNotContainErrors(form)
  }

  it should "allow letters, numbers, spaces and special chars with upto 50 chars for landlord's name" in {
    validateFullName(pageFiveForm, baseData, "landlordFullName")
  }

  it should "allow address to be optional" in {
    val data = baseData -- addressFields
    mustBind(bind(data)) { x => assert(x.landlordAddress.isDefined === false) }
  }


  it should "allow letters, numbers, spaced and special chars up to 100 chars for connection details" in {
    validateLettersNumsSpecCharsUptoLength("landlordConnectText", 100, pageFiveForm, baseData)
  }

  it should "bind with the fields and return issues when connection type selection missing" in {
    val data = baseData - "landlordConnectType"
    val form = bind(data)

    mustOnlyContainError("landlordConnectType", Errors.LandlordConnectionTypeRequired, form)
  }

  it should "return required error if landlord connection text is missing and connection type is other" in {
    val data = baseData - "landlordConnectText"
    val form = bind(data)

    mustOnlyContainRequiredErrorFor("landlordConnectText", form)
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
    lazy val landlordFullName = "landlordFullName" -> "Some Geezer"
    lazy val addressBuildingName = "landlordAddress.buildingNameNumber" -> "Our House"
    lazy val addressStreet1 = "landlordAddress.street1" -> "Middle of Our street"
    lazy val addressStreet2 = "landlordAddress.street2" -> "Our House"
    lazy val addressPostcode = "landlordAddress.postcode" -> "AA11 1AA"
    lazy val landlordConnType = "landlordConnectType" -> LandlordConnectionTypeOther.name
    lazy val landlordConnText = "landlordConnectText" -> "Fraternal bonds"

    val addressFields = Seq(
      "landlordAddress.buildingNameNumber",
      "landlordAddress.street1",
      "landlordAddress.street2",
      "landlordAddress.postcode"
    )

    val baseData = Map(
      landlordFullName,
      addressBuildingName,
      addressStreet1,
      addressStreet2,
      addressPostcode,
      landlordConnType,
      landlordConnText
    )

    def bind(formData: Map[String, String]) = {
      pageFiveForm.bind(formData).convertGlobalToFieldErrors()
    }
  }

}