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

import form.PageFourteenForm._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import utils.FormBindingTestAssertions._
import utils.MappingSpecs._

class PageFourteenMappingSpec extends AnyFlatSpec with should.Matchers {

  "page fourteen form" should "accept the simple answer no for binding form data" in {
    val testData = Map("anyOtherFactors" -> "false")
    val results  = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    doesNotContainErrors(results)
  }

  it should "not accept when there is no value for other factors, and no binding for form data" in {
    val testData: Map[String, String] = Map.empty
    val results                       = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    mustContainError("anyOtherFactors", Errors.anyOtherFactorsRequired, results)

  }

  it should "not accept form data when the other factors is selected, but no details given" in {
    val testData = Map("anyOtherFactors" -> "true")
    val results  = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    mustContainError("anyOtherFactorsDetails", "error.anyOtherFactorsDetailsText.required", results)
  }

  it should " accept form data when the other factors is selected, and bind details given" in {
    val testData = Map("anyOtherFactors" -> "true", "anyOtherFactorsDetails" -> "dry rot in ceiling")
    val results  = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    doesNotContainErrors(results)
  }

  "Page Fourteen Mapping" should "validate the other factors details" in {
    val testData = Map(
      "anyOtherFactors"        -> "true",
      "anyOtherFactorsDetails" ->
        "This is exactly 125 characters long. This is to make sure that the code and unit tests validity checks run smoothly. Great!!!"
    )

    validateLettersNumsSpecCharsUptoLength("anyOtherFactorsDetails", 124, pageFourteenForm, testData, Some("error.anyOtherFactorsDetailsText.maxLength"))
  }
}
