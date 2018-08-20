/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatest.{Matchers, FlatSpec}
import PageFourteenForm._
import utils.FormBindingTestAssertions._
import utils.MappingSpecs._

class PageFourteenMappingSpec extends FlatSpec with Matchers {

  "page fourteen form" should "accept the simple answer no for binding form data" in {
    val testData = Map("anyOtherFactors" -> "false")
    val results = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    doesNotContainErrors(results)
  }
  
  it should "not accept when there is no value for other factors, and no binding for form data" in {
    val testData: Map[String, String] = Map.empty
    val results = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    mustContainBooleanRequiredErrorFor("anyOtherFactors", results)
  }

  it should "not accept form data when the other factors is selected, but no details given" in {
    val testData = Map("anyOtherFactors" -> "true")
    val results = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    mustContainRequiredErrorFor("anyOtherFactorsDetails", results)
  }

  it should " accept form data when the other factors is selected, and bind details given" in {
    val testData = Map("anyOtherFactors" -> "true", "anyOtherFactorsDetails" -> "dry rot in ceiling")
    val results = pageFourteenForm.bind(testData).convertGlobalToFieldErrors()

    doesNotContainErrors(results)
  }

  "Page Fourteen Mapping" should "validate the other factors details" in {
    val data = Map("anyOtherFactors" -> "true")
    validateLettersNumsSpecCharsUptoLength("anyOtherFactorsDetails", 124, pageFourteenForm, data)
  }
}
