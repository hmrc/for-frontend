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

import base.TestBaseSpec
import connectors.{Audit, SubmissionConnector}
import form.persistence.{FormDocumentRepository, MongoSessionRepository}
import org.scalatest.flatspec.AnyFlatSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.Helpers.refNumAction
import views.html.{confirmNotConnected, notConnectedCheckYourAnswers}

import scala.concurrent.ExecutionContext

class NotConnectedCheckYourAnswersControllerSpec extends TestBaseSpec {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "NotConnectedCheckYourAnswersController" should "Audit submission" in {

    val formDocumentRepository = mock[FormDocumentRepository]
    val submissionConnector    = mock[SubmissionConnector]
    val cache                  = mock[MongoSessionRepository]
    val audit                  = mock[Audit]

    val controller = new NotConnectedCheckYourAnswersController(
      formDocumentRepository,
      submissionConnector,
      refNumAction(),
      cache,
      audit,
      stubMessagesControllerComponents(),
      mock[notConnectedCheckYourAnswers],
      mock[confirmNotConnected],
      mock[views.html.error.error]
    )

    val request = FakeRequest()

    val response = controller.onPageSubmit(request)

    status(response) shouldBe SEE_OTHER
  }

}
