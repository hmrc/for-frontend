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

package controllers.localroot

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}

/**
  * @author Yuriy Tumakha
  */
class RootRedirectControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Injecting {

  private val fakeRequest = FakeRequest("GET", "/")
  private val controller  = inject[RootRedirectController]

  "RootRedirectController" should {
    "return redirect to start page" in {
      val result = controller.rootRedirect(fakeRequest)
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationController.index.url)
    }
  }

}
