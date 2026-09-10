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

import form.PageOneForm.pageOneForm
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.MappingSpecs.*

class PageOneFormMappingSpec extends BaseSpec:

  import TestData.*

  "PageOneForm" should {
    "validate the address fields following the standard address convention for this application" in {
      val formData: Map[String, String] =
        Map(errorKey.addressBuildingName -> "buildingNameText", errorKey.addressPostcode -> "AA11 1AA", "street1" -> "street1", "street2" -> "street2")

      validateAddress(pageOneForm, formData)
    }

    "map to fully populated data object if all form fields are present and isAddersCorrect is false" in {
      val formData: Map[String, String] = Map(
        errorKey.addressBuildingName -> "buildingNameText",
        errorKey.addressPostcode     -> "AA11 1AA",
        "street1"                    -> "street1",
        "street2"                    -> "street2"
      )
      val boundForm                     = pageOneForm.bind(formData).convertGlobalToFieldErrors()

      boundForm.hasErrors shouldBe false

      boundForm.value.isDefined shouldBe true

      val pageOneData = boundForm.value.get

      pageOneData.buildingNameNumber shouldBe "buildingNameText"
      pageOneData.street1            shouldBe Some("street1")
      pageOneData.street2            shouldBe Some("street2")
      pageOneData.postcode           shouldBe "AA11 1AA"
    }
  }

  object TestData:
    val errorKey: ErrorKey = new ErrorKey

    class ErrorKey:
      val addressBuildingName: String = "buildingNameNumber"
      val addressPostcode: String     = "postcode"
