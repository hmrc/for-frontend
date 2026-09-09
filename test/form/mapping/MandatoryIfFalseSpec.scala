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

import play.api.data.Form
import play.api.data.Forms.*
import uk.gov.hmrc.vo.unit.test.BaseSpec

class MandatoryIfFalseSpec extends BaseSpec:

  import ConditionalMappings.*

  case class Model(source: Boolean, target: Option[String])

  val form: Form[Model] = Form(mapping(
    "source" -> boolean,
    "target" -> mandatoryIfFalse("source", nonEmptyText)
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  "mandatoryIfFalse" should {
    "mandate the target field if the source field is false, with field-level errors" in {
      val data = Map("source" -> "false")
      val res  = form.bind(data)

      res.errors.head.key shouldBe "target"
    }

    "not mandate the target field if the source field is not false" in {
      val res = form.bind(Map.empty[String, String])

      res.errors.isEmpty shouldBe true
    }
  }
