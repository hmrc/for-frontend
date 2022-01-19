/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import controllers.dataCapturePages.UrlFor
import models.journeys.SummaryPage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.test.FakeHeaders

class UrlBuilderSpec extends AnyFlatSpec with should.Matchers {

  behavior of "URL Builder"

  it should "add the anchor of the element the user was last editing if the user is in edit mode" in {
    val refererHeader = FakeHeaders(Seq(("referer", "http://localhost/page1?edit=anAnchor")))
    val url = UrlFor(SummaryPage, refererHeader)      
    assert(url === routes.ApplicationController.checkYourAnswers.url + "#anAnchor")
  }

  it should "return just the base url if the user is not in edit mode" in {
    val url = UrlFor(SummaryPage, FakeHeaders())
    assert(url === routes.ApplicationController.checkYourAnswers.url)
  }
}
