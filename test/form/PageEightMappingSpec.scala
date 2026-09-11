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

import form.PageEightForm.pageEightForm
import models.serviceContracts.submissions.RentAgreement
import org.scalatest.Assertion
import play.api.data.{Form, FormError}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.FormBindingTestAssertions.*

class PageEightMappingSpec extends BaseSpec:

  private val wasFixedBetween: (String, String)    = "wasRentFixedBetween" -> "false"
  private val notReviewRentFixed: (String, String) = "notReviewRentFixed"  -> "interim"
  private val rentSetBy: (String, String)          = "rentSetByType"       -> "newLease"

  private val baseData: Map[String, String] = Map(wasFixedBetween, notReviewRentFixed, rentSetBy)

  private def bind(formData: Map[String, String]): Form[RentAgreement] =
    pageEightForm.bind(formData).convertGlobalToFieldErrors()

  private def containsError(errors: Seq[FormError], key: String, message: String): Assertion =
    val exists = errors.exists { err =>
      err.key == key && err.messages.contains(message)
    }
    exists shouldBe true

  "Page eight data" should {
    "bind with the fields and not return issues" in {
      val res = bind(baseData)

      res.errors.isEmpty shouldBe true
    }

    "bind with the fields and return no issues when no value input for the way that rent was fixed, when it is between yourself and landlord" in {
      val data = baseData.updated("wasRentFixedBetween", "true") - "notReviewRentFixed"
      val res  = bind(data)

      res.errors.isEmpty shouldBe true
    }

    "bind with the fields and return issues when no selection is chosen for if the rent was fixed between you and landlord" in {
      val data = baseData - "wasRentFixedBetween"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      containsError(res.errors, "wasRentFixedBetween", Errors.wasTheRentFixedBetweenRequired)
    }

    "not bind with the fields and return issues when no value input for the way that rent was fixed, when not between yourself and landlord" in {
      val data = baseData - "notReviewRentFixed"
      val res  = bind(data).convertGlobalToFieldErrors()

      mustContainError("notReviewRentFixed", Errors.whoWasTheRentFixedBetweenRequired, res)
    }

    "bind with the fields and return issues when no value input for the way that rent was set" in {
      val data = baseData - "rentSetByType"
      val res  = bind(data)

      res.errors.isEmpty shouldBe false
      res.errors.size    shouldBe 1
      containsError(res.errors, "rentSetByType", Errors.isThisRentRequired)
    }
  }
