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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.Form
import play.api.data.Forms.*

class MandatoryIfAllEqual extends AnyFlatSpec with should.Matchers:

  import ConditionalMappings.*

  val form: Form[Model] = Form(mapping(
    "s1"     -> nonEmptyText,
    "s2"     -> nonEmptyText,
    "s3"     -> nonEmptyText,
    "target" -> mandatoryIfAllEqual(Seq("s1" -> "s1val", "s2" -> "s2val", "s3" -> "s3val"), nonEmptyText)
  )(Model.apply)(o => Some(Tuple.fromProductTyped(o))))

  case class Model(s1: String, s2: String, s3: String, target: Option[String])

  behavior of "mandatory if all equal"

  it should "mandate the target field if all of the source fields match their required value" in {
    val data = Map("s1" -> "s1val", "s2" -> "s2val", "s3" -> "s3val")
    val res  = form.bind(data)

    assert(res.errors.head.key === "target")
  }

  it should "not mandate the target fields if any of the source fields do not match their required value" in {
    val data = Map("s1" -> "s1val", "s2" -> "s2val", "s3" -> "s3val")
    Seq("s1", "s2", "s3") foreach { f =>
      val data2 = data.updated(f, "notrequiredvalue")
      val res   = form.bind(data2)

      assert(res.errors.isEmpty)
    }
  }
