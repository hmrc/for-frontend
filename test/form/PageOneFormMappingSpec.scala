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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PageOneFormMappingSpec extends AnyFlatSpec with should.Matchers {
  import TestData._
  import form.PageOneForm._
  import utils.MappingSpecs._
  

  "page one mapping" should "validate the address fields following the standard address convention for this application" in {
    val formData: Map[String, String] = Map(errorKey.addressBuildingName -> "buildingNameText",
      errorKey.addressPostcode -> "AA11 1AA",
      "street1" -> "street1",
      "street2" -> "street2")

    validateAddress(pageOneForm, formData)
  }

  "page one mapping" should "map to fully populated data object if all form fields are present and isAddresCorrect is false" in {
    val formData: Map[String, String] = Map(
      errorKey.addressBuildingName -> "buildingNameText",
      errorKey.addressPostcode -> "AA11 1AA",
      "street1" -> "street1",
      "street2" -> "street2")
    val boundForm = pageOneForm.bind(formData).convertGlobalToFieldErrors()

    boundForm.hasErrors should be(false)

    boundForm.value.isDefined should be(true)

    val pageOneData = boundForm.value.get

    pageOneData.buildingNameNumber should be("buildingNameText")
    pageOneData.street1 should be(Some("street1"))
    pageOneData.street2 should be(Some("street2"))
    pageOneData.postcode should be("AA11 1AA")
  }

  object TestData {
    val errorKey = new {
    val addressBuildingName: String = "buildingNameNumber"
    val addressPostcode: String = "postcode"
  }
  
  }
}
