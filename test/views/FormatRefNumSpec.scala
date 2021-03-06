/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{FlatSpec, Matchers}
import template.FormatRefNum

class FormatRefNumSpec extends FlatSpec with Matchers {

  behavior of "format ref num"

  it should "display an 8-digit-prefix ref number as xxxxxxxx/xxx" in {
    assert(FormatRefNum("12345678900") === "12345678/900")
  }

  it should "display a 7-digit-prefix ref number as xxxxxxx/xxx" in {
    assert(FormatRefNum("1234567890") === "1234567/890")
  }
}
