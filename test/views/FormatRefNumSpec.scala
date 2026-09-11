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

package views

import template.FormatRefNum
import uk.gov.hmrc.vo.unit.test.BaseSpec

class FormatRefNumSpec extends BaseSpec:

  "FormatRefNum" should {
    "display an 8-digit-prefix ref number as xxxxxxxx/xxx" in {
      FormatRefNum("12345678900") shouldBe "12345678/900"
    }

    "display a 7-digit-prefix ref number as xxxxxxx/xxx" in {
      FormatRefNum("1234567890") shouldBe "1234567/890"
    }
  }
