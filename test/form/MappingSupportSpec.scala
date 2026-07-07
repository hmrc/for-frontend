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

import play.api.data.validation.{Invalid, Valid, ValidationError}
import uk.gov.hmrc.vo.unit.test.BaseSpec

/**
  * @author Yuriy Tumakha
  */
class MappingSupportSpec extends BaseSpec:

  "Constraint emailAddressAtLeastOneDotInDomain" should {
    "allow valid email with one dot in domain part" in {
      MappingSupport.emailAddressAtLeastOneDotInDomain("first.last@domain.com") shouldBe Valid
    }

    "not allow email without dot in domain part" in {
      MappingSupport.emailAddressAtLeastOneDotInDomain("first.last@domain") shouldBe Invalid(ValidationError("error.email"))
    }

    "not allow empty email" in {
      MappingSupport.emailAddressAtLeastOneDotInDomain("") shouldBe Invalid(ValidationError("error.email"))
    }
  }
