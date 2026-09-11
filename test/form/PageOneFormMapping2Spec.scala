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

import models.serviceContracts.submissions.Address
import play.api.data.Forms.*
import play.api.data.Mapping
import play.api.data.validation.{Invalid, Valid, ValidationResult}
import uk.gov.hmrc.vo.unit.test.BaseSpec

class PageOneFormMapping2Spec extends BaseSpec:

  private def getAddressMapping(strict: Boolean): Mapping[Address] =
    def choose[T](mapping1: Mapping[T], mapping2: Mapping[T]): Mapping[T] =
      if strict then mapping1
      else mapping2

    mapping(
      "buildingNameNumber" -> choose(nonEmptyText, default(nonEmptyText, "")),
      "street1"            -> optional(text),
      "street2"            -> optional(text),
      "postcode"           -> choose(nonEmptyText, default(nonEmptyText, ""))
    )(Address.apply)(a => Some(Tuple.fromProductTyped(a)))

  private val fullyPopulated   = Address("15", Some("street1"), Some("street2, Dundee"), "AB1 2AX")
  private val mandatoryMissing = Address("", Some("street1"), Some("street2"), "")

  "an undefined Address option" should {
    " be valid when validated against an unstrict addresses mapping " in {
      getMappingErrors(None, getAddressMapping(false), "address") shouldBe Valid
    }
  }

  "a fully populated Address option" should {
    " be valid when validated against an unstrict addresses mapping " in {
      getMappingErrors(Some(fullyPopulated), getAddressMapping(false), "address") shouldBe Valid
    }
  }

  "a fully populated Address option" should {
    " be valid when validated against an strict addresses mapping " in {
      getMappingErrors(Some(fullyPopulated), getAddressMapping(true), "address") shouldBe Valid
    }
  }

  "an Address option missing 3 mandatory fields" should {
    " be invalid when validated against an strict addresses mapping " in {
      val res: ValidationResult = getMappingErrors(Some(mandatoryMissing), getAddressMapping(true), "address")
      res match
        case Valid         => fail("expected invalid")
        case Invalid(errs) =>
          errs should contain(createFieldValidationError("address.buildingNameNumber", "error.required"))
          errs should contain(createFieldValidationError("address.postcode", "error.required"))
    }
  }
