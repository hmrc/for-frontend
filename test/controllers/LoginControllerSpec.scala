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
import connectors.Audit
import form.persistence.FormDocumentRepository
import models.*
import models.serviceContracts.submissions.Address
import org.scalatest.flatspec.AnyFlatSpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import config.LoginToHODAction
import security.LoginToHOD.{Postcode, StartTime}
import security.NoExistingDocument
import uk.gov.hmrc.http.HeaderCarrier
import useCases.ReferenceNumber
import util.DateUtil.nowInUK
import utils.Helpers.fakeRequest2MessageRequest
import views.html.{login, loginFailed}

import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.{ExecutionContext, Future}

class LoginControllerSpec extends TestBaseSpec {
  private val documentRepo = mock[FormDocumentRepository]

  private val testAddress = Address("13", Some("Street"), Some("City"), "AA11 1AA")

  "login controller" should "Audit successful login" in {

    val audit = mock[Audit]
    doNothing.when(audit).sendExplicitAudit(anyString, any[JsObject])(using any[HeaderCarrier], any[ExecutionContext])

    val loginToHodFunction = (referenceNumber: ReferenceNumber, _: Postcode, _: StartTime) => {
      assert(referenceNumber.equals("01234567000"))
      Future.successful(NoExistingDocument("token", testAddress))
    }

    val loginToHod = mock[LoginToHODAction]
    val time       = nowInUK
    when(loginToHod.apply(using any[HeaderCarrier], any[ExecutionContext])).thenReturn(loginToHodFunction)

    val loginController = new LoginController(
      audit,
      documentRepo,
      loginToHod,
      stubMessagesControllerComponents(),
      mock[login],
      mock[views.html.error.error],
      mock[loginFailed],
      mock[views.html.lockedOut]
    )

    val fakeRequest = FakeRequest()
    // should strip out all non digits then split string 3 from end to create ref1/ref2
    val response    = loginController.verifyLogin("01234567/*ok blah 000", "BN12 1AB", time)(using fakeRequest)

    status(response) shouldBe SEE_OTHER

    verify(audit).sendExplicitAudit(
      eqTo("UserLogin"),
      eqTo(Json.obj(Audit.referenceNumber -> "01234567000", "returningUser" -> false, "address" -> Json.toJsObject(testAddress)))
    )(using any[HeaderCarrier], any[ExecutionContext])

  }

  "Login Controller" should "Audit logout event" in {
    val audit = mock[Audit]
    doNothing.when(audit).sendExplicitAudit(any[String], any[JsObject])(using any[HeaderCarrier], any[ExecutionContext])

    val loginController = new LoginController(
      audit,
      documentRepo,
      null,
      stubMessagesControllerComponents(),
      mock[login],
      mock[views.html.error.error],
      mock[loginFailed],
      mock[views.html.lockedOut]
    )

    val fakeRequest = FakeRequest()

    val response = loginController.logout(fakeRequest)

    status(response) shouldBe SEE_OTHER

    verify(audit).sendExplicitAudit(eqTo("Logout"), eqTo(Json.obj(Audit.referenceNumber -> "-")))(using any[HeaderCarrier], any[ExecutionContext])

  }

}
