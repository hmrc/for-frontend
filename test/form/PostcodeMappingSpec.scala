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

import org.scalatest.prop.TableFor2
import play.api.data.{FormError, Mapping}
import uk.gov.hmrc.vo.unit.test.BaseSpec

class PostcodeMappingSpec extends BaseSpec:

  private val positiveTestData: TableFor2[String, String] =
    Table(
      ("raw postcode", "formated postcode"),
      ("   ML7 +++\n  8LQ   ++", "ML7 8LQ"),
      ("ML7 8LQ", "ML7 8LQ"),
      ("ML7.8LQ", "ML7 8LQ"),
      ("ML7+8LQ", "ML7 8LQ"),
      ("SW1A.1AA", "SW1A 1AA"),
      ("XM4.5HQ", "XM4 5HQ"),
      ("M L 7    8 L Q ", "ML7 8LQ"),
      ("m l 7 +\n  8 l q ", "ML7 8LQ")
    )

  private val negativeTestData: TableFor2[String, Seq[FormError]] =
    Table(
      ("raw postcode", "Error message"),
      ("ML +++\n  8LQ   ++", Seq(FormError("", "postcode.format", Seq("ML +++\n  8LQ   ++")))),
      ("+", Seq(FormError("", "postcode.format", Seq("+")))),
      ("", Seq(FormError("", "postcode.missing")))
    )

  private val postcodeMapping: Mapping[String] = PostcodeMapping.postcode("postcode.missing", "postcode.format")

  "PostcodeMapping" should {
    "map correct postcode" in {
      val formData = Map("" -> "BN12 4AX")

      postcodeMapping.bind(formData).value shouldBe "BN12 4AX"
    }

    "successfully format and validate all correct postcodes" in
      forAll(positiveTestData) { (rawPostcode: String, formattedPostcode: String) =>
        postcodeMapping.bind(Map("" -> rawPostcode)).value shouldBe formattedPostcode
      }

    "reject all incorrect postcodes" in
      forAll(negativeTestData) { (rawPostcode: String, postcodeError: Seq[FormError]) =>
        postcodeMapping.bind(Map("" -> rawPostcode)).left.value should contain.only(postcodeError*)
      }
  }
