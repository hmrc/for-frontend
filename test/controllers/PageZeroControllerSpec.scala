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

package controllers

import connectors.{Audit, Document}
import controllers.dataCapturePages.PageZeroController
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.vo.unit.test.BaseAppSpec
import util.DateUtil.nowInUK
import utils.Helpers.refNumAction
import utils.stubs.StubFormDocumentRepo
import views.html.part0

import java.util.UUID

class PageZeroControllerSpec extends BaseAppSpec:

  private val testRefNum         = "1234567890"
  private val sessionId          = UUID.randomUUID.toString
  private val documentRepository = StubFormDocumentRepo((sessionId, testRefNum, Document(testRefNum, nowInUK)))
  private val audit              = mock[Audit]

  "PageZeroController" should {
    "redirect to page 1 if user want to change address" in {
      val pageZeroController = PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = postRequest
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated"       -> "yes-change-address",
          "continue-button" -> ""
        )

      val res = pageZeroController.save(request).futureValue

      status(res) shouldBe SEE_OTHER

      header("location", res) shouldBe Some("/sending-rental-information/page/1")
    }

    "redirect to page 2 if user doesn't want to change address" in {
      val pageZeroController = PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = postRequest
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated"       -> "yes",
          "continue-button" -> ""
        )

      val res = pageZeroController.save(request).futureValue

      status(res)             shouldBe SEE_OTHER
      header("location", res) shouldBe Some("/sending-rental-information/page/2")
    }

    "redirect to not connected page if user is not connected with property " in {
      val pageZeroController = PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = postRequest
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated"       -> "no",
          "continue-button" -> ""
        )

      val res = pageZeroController.save(request).futureValue

      status(res)             shouldBe SEE_OTHER
      header("location", res) shouldBe Some("/sending-rental-information/previously-connected")
    }
  }
