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

import ConditionalMappings.mandatoryIfTrue
import play.api.data.{Form, FormError}
import play.api.data.Forms.*
import uk.gov.hmrc.vo.unit.test.BaseSpec

class ProblemWithPlayFrameworkMappingsSpec extends BaseSpec:

  case class Model(nonUkResident: Boolean, country: Option[String], email: String)

  val form: Form[Model] = Form(mapping(
    "nonUkResident" -> boolean,
    "country"       -> optional(nonEmptyText),
    "email"         -> nonEmptyText
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o)))
    .verifying("Error.countryRequired", x => x.nonUkResident && x.country.isDefined))

  val form2countryRequiredForNonUkResident: Form[Model] = Form(mapping(
    "nonUkResident" -> boolean,
    "country"       -> mandatoryIfTrue("nonUkResident", nonEmptyText),
    "email"         -> nonEmptyText
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  "Built-in Play mappings" should {
    "not contain an error for the conditional validation when there is a field-level error" in {
      val data = Map("nonUkResident" -> "true")
      val res  = form.bind(data)

      res.errors.length   shouldBe 1
      res.errors.head.key shouldBe "email"

      res.errors.head shouldBe FormError("email", List("error.required"))
    }

    "not allow an field-level error message for a conditional validation" in {
      val data = Map("nonUkResident" -> "true", "email" -> "abc@gov.uk")
      val res  = form.bind(data)

      res.errors.length   shouldBe 1
      res.errors.head.key shouldBe ""

      res.errors.head shouldBe FormError("", List("Error.countryRequired"))
    }
  }

  "ConditionalMappings" should {
    "contain a field level errors for the field and conditional mappings" in {
      val data = Map("nonUkResident" -> "true")
      val res  = form2countryRequiredForNonUkResident.bind(data)

      res.errors.length   shouldBe 2
      res.errors.head.key shouldBe "country"
      res.errors.last.key shouldBe "email"

      res.errors shouldBe Seq(
        FormError("country", List("error.required")),
        FormError("email", List("error.required"))
      )
    }
  }
