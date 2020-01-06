/*
 * Copyright 2020 HM Revenue & Customs
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

import models._
import org.scalatest.{FlatSpec, Matchers}
import play.api.data.{Form, FormError}
import utils.FormBindingTestAssertions._

class PageEightMappingSpec extends FlatSpec with Matchers {

  import PageEightForm._

  val wasFixedBetween = "wasRentFixedBetween" -> "false"
  val notReviewRentFixed = "notReviewRentFixed" -> "interim"
  val rentSetBy = "rentSetByType" -> "newLease"

  val baseData = Map(wasFixedBetween, notReviewRentFixed, rentSetBy)

  def bind(formData: Map[String, String]) = {
    pageEightForm.bind(formData).convertGlobalToFieldErrors()
  }

  def containsError(errors: Seq[FormError], key: String, message: String): Boolean = {
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists should be(true)
    exists
  }

  "PageEightData" should "bind with the fields and not return issues" in {
    val res = bind(baseData)
    res.errors.isEmpty should be(true)
  }

  "PageEightData" should "bind with the fields and return no issues when no value input for the way that rent was fixed, when it is between yourself and landlord" in {
    val data = baseData.updated("wasRentFixedBetween", "true") - "notReviewRentFixed"
    val res = bind(data)
    res.errors.isEmpty should be(true)
    res.errors.size should be(0)
  }

  "PageEightData" should "bind with the fields and return issues when no selection is chosen for if the rent was fixed between you and landlord" in {
    val data = baseData - "wasRentFixedBetween"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "wasRentFixedBetween", "error.boolean_missing")
  }

  "PageEightData" should "not bind with the fields and return issues when no value input for the way that rent was fixed, when not between yourself and landlord" in {
    val data = baseData - "notReviewRentFixed"
    val res = bind(data).convertGlobalToFieldErrors()
    mustContainError("notReviewRentFixed", Errors.noValueSelected, res)
  }

  "PageEightData" should "bind with the fields and return issues when no value input for the way that rent was set" in {
    val data = baseData - "rentSetByType"
    val res = bind(data)
    res.errors.isEmpty should be(false)
    res.errors.size should be(1)
    containsError(res.errors, "rentSetByType", Errors.noValueSelected)
  }
}
