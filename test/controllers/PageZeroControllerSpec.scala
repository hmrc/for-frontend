/*
 * Copyright 2016 HM Revenue & Customs
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

import connectors.{Document, Page}
import controllers.dataCapturePages.PageZeroController
import form.persistence.{FormDocumentRepository, SaveForm, SaveFormInRepository}
import models.pages.SummaryBuilder
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import play.filters.csrf.CSRF
import play.filters.csrf.CSRF.SignedTokenProvider
import uk.gov.hmrc.play.http.HeaderNames
import utils.stubs.StubFormDocumentRepo
import play.api.test.Helpers._

class PageZeroControllerSpec extends FreeSpec with MustMatchers with FutureAwaits with DefaultAwaitTimeout with OptionValues {

  val testRefNum = "1234567890"
  val sessionId = java.util.UUID.randomUUID().toString

  private object TestPageZeroController extends PageZeroController {

    override def saveForm: SaveForm = new SaveFormInRepository(repository, SummaryBuilder)

    override def repository: FormDocumentRepository = StubFormDocumentRepo((sessionId, testRefNum, Document(testRefNum, DateTime.now())))
  }

  "When the user still has a relationship with the property" - {
    val request = FakeRequest()
      .withHeaders(HeaderNames.xSessionId -> sessionId)
      .withSession("refNum" -> testRefNum)
      .withFormUrlEncodedBody(
        CSRF.TokenName -> SignedTokenProvider.generateToken,
        "isRelated" -> "true",
        "continue_button" -> ""
      )

    val res = await(TestPageZeroController.save()(request))

    "Then they are directed to page one of the form" in {
      status(res) mustBe SEE_OTHER
      header("location", res).value mustBe "/sending-rental-information/page/1"
    }
  }

  "When the user no longer has a relationship with the property" - {
    val request = FakeRequest()
      .withHeaders(HeaderNames.xSessionId -> sessionId)
      .withSession("refNum" -> testRefNum)
      .withFormUrlEncodedBody(
        CSRF.TokenName -> SignedTokenProvider.generateToken,
        "isRelated" -> "false",
        "continue_button" -> ""
      )

    val res = await(TestPageZeroController.save()(request))

    "Then they are directed to the vacated form" in {
      status(res) mustBe SEE_OTHER
      header("location", res).value mustBe "/sending-rental-information/inpage-vacated-form"
    }
  }

}