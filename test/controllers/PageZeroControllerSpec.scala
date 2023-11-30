/*
 * Copyright 2023 HM Revenue & Customs
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
import form.persistence.FormDocumentRepository
import org.mockito.scalatest.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import util.DateUtil.nowInUK
import utils.Helpers.refNumAction
import utils.stubs.StubFormDocumentRepo
import views.html.part0

class PageZeroControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  private val testRefNum = "1234567890"
  private val sessionId = java.util.UUID.randomUUID().toString
  private val documentRepository = StubFormDocumentRepo((sessionId, testRefNum, Document(testRefNum, nowInUK)))
  private val audit = mock[Audit]

  override def fakeApplication(): play.api.Application = {
    new GuiceApplicationBuilder()
      .overrides(
        bind[Audit].toInstance(audit),
        bind[FormDocumentRepository].toInstance(documentRepository)
      )
      .configure(Map("auditing.enabled" -> false, "metrics.enabled" -> false))
      .build()
  }

  "Page zero controller" should {
    "redirect to page 1 if user want to change address" in {
      val pageZeroController = new PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = FakeRequest()
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated" -> "yes-change-address",
          "continue_button" -> ""
        )

      val res = await(pageZeroController.save()(request))

      status(res) mustBe SEE_OTHER

      header("location", res).value mustBe "/sending-rental-information/page/1"
    }

    "redirect to page 2 if user doesn't want to change address" in {
      val pageZeroController = new PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = FakeRequest()
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated" -> "yes",
          "continue_button" -> ""
        )

      val res = await(pageZeroController.save()(request))

      status(res) mustBe SEE_OTHER
      header("location", res).value mustBe "/sending-rental-information/page/2"

    }


    "redirect to not connected page if user is not connected with property " in {
      val pageZeroController = new PageZeroController(audit, documentRepository, refNumAction(), stubMessagesControllerComponents(), mock[part0])

      val request = FakeRequest()
        .withHeaders(HeaderNames.xSessionId -> sessionId)
        .withSession("refNum" -> testRefNum)
        .withFormUrlEncodedBody(
          "isRelated" -> "no",
          "continue_button" -> ""
        )

      val res = await(pageZeroController.save()(request))

      status(res) mustBe SEE_OTHER
      header("location", res).value mustBe "/sending-rental-information/previously-connected"
    }

  }


  /*



  "When the user still has a relationship with the property" - {
     "And want to change address" - {
       "Then they are directed to page one of the form" in {
         running(app) {
           val request = addToken(FakeRequest()
             .withHeaders(HeaderNames.xSessionId -> sessionId)
             .withSession("refNum" -> testRefNum)
             .withFormUrlEncodedBody(
               "isRelated" -> "yes-change-address",
               "continue_button" -> ""
             ))

           val res = await(pageZeroController.save()(request))

           status(res) mustBe SEE_OTHER
           header("location", res).value mustBe "/sending-rental-information/page/1"
         }
       }
     }
    "And doesn't want to change address" - {
      "Then they are directed to page two skipping address change on page one" in {
        running(app) {
          val request = addToken(FakeRequest()
            .withHeaders(HeaderNames.xSessionId -> sessionId)
            .withSession("refNum" -> testRefNum)
            .withFormUrlEncodedBody(
              "isRelated" -> "yes",
              "continue_button" -> ""
            ))

          val res = await(pageZeroController.save()(request))

          status(res) mustBe SEE_OTHER
          header("location", res).value mustBe "/sending-rental-information/page/2"
        }
      }
    }
  }

  "When the user no longer has a relationship with the property" - {
    "Then they are redirected to a not connected form" in {
      running(app) {
        val request = addToken(FakeRequest()
          .withHeaders(HeaderNames.xSessionId -> sessionId)
          .withSession("refNum" -> testRefNum)
          .withFormUrlEncodedBody(
            "isRelated" -> "no",
            "continue_button" -> ""
          ))

        val res = await(pageZeroController.save()(request))

        status(res) mustBe SEE_OTHER
        header("location", res).value mustBe "/sending-rental-information/previously-connected"
      }
    }
  }

   */
}
