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

class OnlyIfTrueSpec extends BaseSpec:

  import ConditionalMappings.*

  case class Model(source: Boolean, target: Option[String])

  val form = Form(mapping(
    "source" -> boolean,
    "target" -> onlyIfTrue("source", optional(nonEmptyText))
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  "onlyIfTrue" should {
    "apply the mapping to the target field if the source field is true" in {
      val data = Map("source" -> "true", "target" -> "Bonjour")
      val res  = form.bind(data)

      res.value.get shouldBe Model(true, Some("Bonjour"))
    }

    "ignore the mapping and set the default value if the source field is not true" in {
      val data = Map("source" -> "false", "target" -> "Bonjour")
      val res  = form.bind(data)

      res.value.get shouldBe Model(false, None)
    }

    "not mandate the target field even if the source field is true" in {
      val data = Map("source" -> "true")
      val res  = form.bind(data)

      res.errors.isEmpty shouldBe true
    }
  }
