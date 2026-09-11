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

class ChainedConditionsSpec extends BaseSpec:

  import ConditionalMappings.*

  case class Model(name: String, age: Int, favouriteColour: Option[String])

  val form = Form(mapping(
    "name"            -> nonEmptyText,
    "age"             -> number,
    "favouriteColour" -> mandatoryIf(
      isEqual("name", "Francoise") `and` isEqual("age", "21"),
      nonEmptyText
    )
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  "Chained conditional mappings" should {
    "apply mappings if all of the chained criteria are satisfied" in {
      val data = Map("name" -> "Francoise", "age" -> "21")
      val res  = form.bind(data)

      res.errors.head.key shouldBe "favouriteColour"
    }

    "not apply mappings if any part of the chained criteria is not satisfied" in {
      val data = Map("name" -> "Francoise", "age" -> "20")
      val res  = form.bind(data)

      res.errors.isEmpty shouldBe true
    }
  }
