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

import play.api.data.FormError
import uk.gov.hmrc.vo.unit.test.BaseSpec

class FormErrorSupportSpec extends BaseSpec:

  "Converting a field specific FormError" should {
    "result in the same FormError" in {
      val fe1 = FormError("field", "message", "arg1")
      fe1.convert() shouldBe fe1
    }
  }

  "Converting a global FormError that has more than 1 message" should {
    "result in the same FormError" in {
      val fe1 = FormError("", Seq("message1", "message2"), "arg1")
      fe1.convert() shouldBe fe1
    }
  }

  "Converting a global FormError with a message that is just 'fieldError' followed by '|field1.field2|code1.code2'" should {
    "result in the FormError('field1.field2', 'field1.field2.code1.code2'" in {
      val fe1 = FormError("", "fieldError|field1.field2|code1.code2", "arg1")
      val fe2 = FormError("field1.field2", "field1.field2.code1.code2", "arg1")
      fe1.convert() shouldBe fe2
    }
  }
