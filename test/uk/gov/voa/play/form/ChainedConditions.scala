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

package uk.gov.voa.play.form

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.Form
import play.api.data.Forms._

// TODO: Remove package uk.gov.voa.play.form if library uk.gov.hmrc:play-conditional-form-mapping_2.13 for Scala 2.13 released
// https://artefacts.tax.service.gov.uk/ui/packages?name=%2Aplay-conditional-form-mapping%2A&type=packages

class ChainedConditions extends AnyFlatSpec with should.Matchers {
  import ConditionalMappings._

  behavior of "chained mappings"

  it should "apply mappings if all of the chained criteria are satisfied" in {
    val data = Map("name" -> "Francoise", "age" -> "21")
    val res  = form.bind(data)

    assert(res.errors.head.key === "favouriteColour")
  }

  it should "not apply mappings if any part of the chained critieria is not satisfied" in {
    val data = Map("name" -> "Francoise", "age" -> "20")
    val res  = form.bind(data)

    assert(res.errors.isEmpty)
  }

  lazy val form = Form(mapping(
    "name"            -> nonEmptyText,
    "age"             -> number,
    "favouriteColour" -> mandatoryIf(
      isEqual("name", "Francoise") `and` isEqual("age", "21"),
      nonEmptyText
    )
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  case class Model(name: String, age: Int, favouriteColour: Option[String])
}
