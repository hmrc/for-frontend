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

package form.mapping

import uk.gov.hmrc.vo.unit.test.BaseSpec

class IsFalseSpec extends BaseSpec:

  import ConditionalMappings.*

  "isTrue and isFalse" should {
    "apply a mapping with value TRUE is string in a case-variation of FALSE" in {
      isFalse("source")(Map("source" -> "false")) shouldBe true
      isFalse("source")(Map("source" -> "FALSE")) shouldBe true
      isFalse("source")(Map("source" -> "fAlSe")) shouldBe true
    }

    "apply a mapping with value FALSE is string in a case-variation of TRUE" in {
      isFalse("source")(Map("source" -> "true")) shouldBe false
      isFalse("source")(Map("source" -> "TRUE")) shouldBe false
      isFalse("source")(Map("source" -> "TRuE")) shouldBe false
    }

    "apply a mapping with value FALSE is string in not a case-variation of TRUE or FALSE" in {
      isFalse("source")(Map("source" -> "non-sensical")) shouldBe false
    }
  }
