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

package views

import controllers.LoginController
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

/**
  * @author Yuriy Tumakha
  */
class FeedbackBannerSpec extends BaseAppSpec:

  private val controller: LoginController = inject[LoginController]

  "Feedback link in PhaseBanner" should {
    "be rendered" in {
      val result = controller.show(getRequest.withCSRFToken)

      status(result) shouldBe OK

      val doc          = parsePage(contentAsString(result))
      val feedbackLink = doc.selectFirst(".govuk-phase-banner__text a")

      feedbackLink                should not be null
      feedbackLink.text()       shouldBe "give your feedback (opens in new tab)"
      feedbackLink.attr("href") shouldBe "http://localhost:9514/feedback/rald-inpage"
    }
  }
