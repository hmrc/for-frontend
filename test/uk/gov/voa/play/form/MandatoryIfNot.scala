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

class MandatoryIfNot extends AnyFlatSpec with should.Matchers {
  import ConditionalMappings._

  it should "mandate the target field if the source field DOES not match the specified value" in {
    val data = Map("source" -> "NotTheMagicValue")
    val res  = form.bind(data)

    assert(res.errors.head.key === "target")
  }

  it should "not mandate the target field if the source field DOES NOT match the specified value" in {
    val data = Map("source" -> "magicValue")
    val res  = form.bind(data)

    assert(res.errors.isEmpty)
  }

  lazy val form: Form[Model] = Form(mapping(
    "source" -> nonEmptyText,
    "target" -> mandatoryIfNot("source", "magicValue", nonEmptyText)
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  case class Model(source: String, target: Option[String])
}
