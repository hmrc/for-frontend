/*
 * Copyright 2024 HM Revenue & Customs
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

import base.TestBaseSpec
import form.persistence.{FormDocumentRepository, MongoSessionRepository}
import org.scalatest.flatspec.AnyFlatSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.Helpers.refNumAction
import views.html.error.error
import views.html.previouslyConnected

import scala.concurrent.ExecutionContext

class PreviouslyConnectedControllerSpec extends TestBaseSpec {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "PreviouslyConnectedController" should "redirect after form submission" in {

    val cache                  = mock[MongoSessionRepository]
    val formDocumentRepository = mock[FormDocumentRepository]

    val controller =
      new PreviouslyConnectedController(stubMessagesControllerComponents(), cache, formDocumentRepository, refNumAction(), mock[previouslyConnected], mock[error])

    val request = FakeRequest()

    val response = controller.onPageSubmit(request)

    status(response) shouldBe SEE_OTHER

  }

}
