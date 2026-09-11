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

import form.PageFiveForm.*
import models.pages.PageFive
import models.serviceContracts.submissions.LandlordConnectionType
import play.api.data.Form
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.FormBindingTestAssertions.*
import utils.MappingSpecs.*

class PageFiveMappingSpec extends BaseSpec:

  import TestData.*

  "PageFiveForm" should {
    "bind with the fields and not return issues" in
      mustBind(bind(baseData))(_ => ())

    "require the field landlordFulName" in {
      val data = baseData - "landlordFullName"
      val form = bind(data)

      mustContainError("landlordFullName", "error.landlordFullName.required", form)
    }

    "allow letters, numbers, spaces and special chars with upto 50 chars for landlord's name" in
      validateFullName(pageFiveForm, baseData, "landlordFullName", Some("error.landlordFullName.maxLength"))

    "allow address to be optional" in {
      val data = baseData -- addressFields

      mustBind(bind(data))(_.landlordAddress.isDefined shouldBe false)
    }

    "allow letters, numbers, spaced and special chars up to 100 chars for connection details" in
      validateLettersNumsSpecCharsUptoLength("landlordConnectText", 100, pageFiveForm, baseData, Some("error.landlordConnectText.maxLength"))

    "bind with the fields and return issues when connection type selection missing" in {
      val data = baseData - "landlordConnectType"
      val form = bind(data)

      mustOnlyContainError("landlordConnectType", Errors.LandlordConnectionTypeRequired, form)
    }

    "return required error if landlord connection text is missing and connection type is other" in {
      val data = baseData - "landlordConnectText"
      val form = bind(data)

      mustContainError("landlordConnectText", "error.landlordConnectText.required", form)
    }

    "bind with the fields and return with no errors" in {
      val data = baseData
      val form = bind(data)

      doesNotContainErrors(form)
    }

    "never return validation errors for address" in {
      val data = baseData
        .updated("original.landlordAddress.buildingNameNumber", "1")
        .updated("original.landlordAddress.street1", "The Road")
        .updated("original.landlordAddress.postcode", "AA11 1AA")
      val form = bind(data)

      doesNotContainErrors(form)
    }

    "not return validation errors for even when postcode alone is filled out" in {
      val data = baseData - addressBuildingName._1 - addressStreet1._1 - addressStreet2._1
      val form = bind(data)

      doesNotContainErrors(form)
    }
  }

  object TestData:
    val landlordFullName: (String, String)    = "landlordFullName"                   -> "Some Geezer"
    val addressBuildingName: (String, String) = "landlordAddress.buildingNameNumber" -> "Our House"
    val addressStreet1: (String, String)      = "landlordAddress.street1"            -> "Middle of Our street"
    val addressStreet2: (String, String)      = "landlordAddress.street2"            -> "Our House"
    val addressPostcode: (String, String)     = "landlordAddress.postcode"           -> "AA11 1AA"
    val landlordConnType: (String, String)    = "landlordConnectType"                -> LandlordConnectionType.other.toString
    val landlordConnText: (String, String)    = "landlordConnectText"                -> "Fraternal bonds"

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
