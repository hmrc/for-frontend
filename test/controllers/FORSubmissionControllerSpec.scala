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

import actions.RefNumRequest
import form.persistence.FormDocumentRepository
import models.serviceContracts.submissions.Submission
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.vo.unit.test.BaseAppSpec
import useCases.SubmitBusinessRentalInformation
import utils.stubs.StubFormDocumentRepoProvider

import javax.inject.Singleton
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class FORSubmissionControllerSpec extends BaseAppSpec with GivenWhenThen:

  private val refNum    = "adfiwerq08342kfad"
  private val sessionId = "sessionid"

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled" -> false,
      "metrics.enabled"  -> false
    )
    .overrides(
      bind[SubmitBusinessRentalInformation].to[StubSubmitBRI].in[Singleton],
      bind[FormDocumentRepository].toProvider[StubFormDocumentRepoProvider].in[Singleton]
    )
    .build()

  private val submit: StubSubmitBRI               = inject[SubmitBusinessRentalInformation].asInstanceOf[StubSubmitBRI]
  private val controller: FORSubmissionController = inject[FORSubmissionController]

  "When a submission is received and the declaration has been agreed to" should {
    "return 302 response redirecting to the confirmation page" in {
      val request = FakeRequest()
        .withSession("refNum" -> refNum)
        .withFormUrlEncodedBody("declaration" -> "true")
        .withHeaders(HeaderNames.xSessionId -> sessionId)

      val response = controller.submit()(request).futureValue

      response.header.status                should equal(302)
      response.header.headers("Location") shouldBe controllers.feedback.routes.SurveyController.confirmation.url

      And("The Business rental information submission process is initiated")
      submit.assertBRISubmittedFor(refNum)
    }
  }

  "When a submission is received and the declaration has not been agreed to" should {
    "return redirect to the declaration error page" in {
      val request  = FakeRequest().withSession("refNum" -> refNum).withFormUrlEncodedBody("declaration" -> "false")
      val response = Await.result(controller.submit()(request), 5 seconds)

      response.header.status                should equal(302)
      response.header.headers("Location") shouldBe controllers.routes.ApplicationController.declarationError.url
    }
  }

class StubSubmitBRI extends SubmitBusinessRentalInformation with should.Matchers:

  val stubSubmission: Submission = Submission(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  var submittedRefNums: Seq[String] = Seq.empty

  def apply(refNum: String)(using hc: HeaderCarrier, request: RefNumRequest[?]): Future[Submission] =
    Console.println(s"=== called apply with : $refNum ===")
    Future.successful {
      submittedRefNums = submittedRefNums :+ refNum
      stubSubmission
    }

  def assertBRISubmittedFor(refNum: String): Unit =
    submittedRefNums should equal(Seq(refNum))
