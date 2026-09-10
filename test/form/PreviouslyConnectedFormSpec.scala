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

import models.serviceContracts.submissions.PreviouslyConnected
import uk.gov.hmrc.vo.unit.test.BaseSpec

class PreviouslyConnectedFormSpec extends BaseSpec:

  private val formData: Map[String, String] = Map(
    "haveYouBeenConnected" -> "true"
  )

  "PreviouslyConnectedForm" should {
    "map form with all values" in {
      val formWithData = PreviouslyConnectedForm.formMapping.bind(formData)

      formWithData.errors shouldBe empty
      formWithData.value  shouldBe defined
      formWithData.value  shouldBe Some(PreviouslyConnected(true))
    }

    "show error when empty form is submitted" in {
      val formWithData = PreviouslyConnectedForm.formMapping.bind(Map[String, String]())

      formWithData.errors should not be empty
      formWithData.errors should have size 1
      formWithData.value  should not be defined
    }
  }
