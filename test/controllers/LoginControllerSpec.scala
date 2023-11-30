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

import connectors.Audit
import form.persistence.FormDocumentRepository
import models.serviceContracts.submissions.Address
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import playconfig.LoginToHODAction
import security.LoginToHOD.{Postcode, Ref1, Ref2, StartTime}
import security.NoExistingDocument
import uk.gov.hmrc.http.HeaderCarrier
import util.DateUtil.nowInUK
import utils.Helpers.fakeRequest2MessageRequest
import views.html.{login, loginFailed}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits._

class LoginControllerSpec extends AnyFlatSpec with should.Matchers with MockitoSugar {
  private val documentRepo = mock[FormDocumentRepository]

  private val testAddress = Address("13", Some("Street"), Some("City"), "AA11 1AA")

  "login controller" should "Audit successful login" in {

    val audit = mock[Audit]
    doNothing.when(audit).sendExplicitAudit(any[String], any[JsObject])(any[HeaderCarrier], any[ExecutionContext])

    val loginToHodFunction = (ref1: Ref1, ref2: Ref2, postcode: Postcode, start:StartTime) =>{
      assert(ref1.equals("01234567"))
      assert(ref2.equals("000"))
      Future.successful(NoExistingDocument("token", testAddress))
    }

    val loginToHod = mock[LoginToHODAction]
    val time = nowInUK
    when(loginToHod.apply(any[HeaderCarrier], any[ExecutionContext])).thenReturn(loginToHodFunction)

    val loginController = new LoginController(audit, documentRepo, loginToHod, stubMessagesControllerComponents(),
      mock[login], mock[views.html.error.error], mock[loginFailed], mock[views.html.lockedOut])

    val fakeRequest = FakeRequest()
    //should strip out all non digits then split string 3 from end to create ref1/ref2
    val response = loginController.verifyLogin("01234567/*ok blah 000", "BN12 1AB", time)(fakeRequest)

    status(response) shouldBe(SEE_OTHER)

    verify(audit).sendExplicitAudit(eqTo("UserLogin"),
      eqTo(Json.obj(Audit.referenceNumber -> "01234567000", "returningUser" -> false, "address" -> testAddress))
    )(any[HeaderCarrier], any[ExecutionContext])

  }

  "Login Controller" should "Audit logout event" in {
    val audit = mock[Audit]
    doNothing.when(audit).sendExplicitAudit(any[String], any[JsObject])(any[HeaderCarrier], any[ExecutionContext])

    val loginController = new LoginController(audit, documentRepo, null, stubMessagesControllerComponents(),
      mock[login], mock[views.html.error.error], mock[loginFailed], mock[views.html.lockedOut])

    val fakeRequest = FakeRequest()

    val response = loginController.logout().apply(fakeRequest)

    status(response) shouldBe(SEE_OTHER)

    verify(audit).sendExplicitAudit(eqTo("Logout"), eqTo(Json.obj(Audit.referenceNumber -> "-"))
    )(any[HeaderCarrier], any[ExecutionContext])

  }

}
