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

class MandatoryIfExistsSpec extends BaseSpec:

  import ConditionalMappings.*

  case class Model(source: Option[String], target: Option[String])

  val form: Form[Model] = Form(mapping(
    "source" -> optional(nonEmptyText),
    "target" -> mandatoryIfExists("source", nonEmptyText)
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  "mandatoryIfExists" should {
    "mandate the target field if any value for the source field is supplied" in {
      val data = Map("source" -> "any-random-value")
      val res  = form.bind(data)

      res.errors.head.key shouldBe "target"
    }

    "not mandate the target field if no value for the source field is supplied" in {
      val res = form.bind(Map.empty[String, String])

      res.errors.isEmpty shouldBe true
    }
  }
