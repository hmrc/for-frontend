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

import form.persistence.FormDocumentRepository
import javax.inject.Singleton
import models.serviceContracts.submissions.Submission
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import useCases.SubmitBusinessRentalInformation
import utils.stubs.StubFormDocumentRepoProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class FORSubmissionControllerSpec extends AnyFlatSpec with should.Matchers with GivenWhenThen with GuiceOneAppPerSuite {

  import TestData._

  override def fakeApplication() = new GuiceApplicationBuilder()
    .overrides(
      bind[SubmitBusinessRentalInformation].to[StubSubmitBRI].in[Singleton],
      bind[FormDocumentRepository].toProvider[StubFormDocumentRepoProvider].in[Singleton]
    )
    .configure(Map("auditing.enabled" -> false)).build()

  def submit: StubSubmitBRI = app.injector.instanceOf[SubmitBusinessRentalInformation].asInstanceOf[StubSubmitBRI]

  def controller: FORSubmissionController = app.injector.instanceOf[FORSubmissionController]

  "When a submission is received and the declaration has been agreed to" should
    "A 302 response redirecting to the confirmation page is returned" in {
      val request = FakeRequest().withSession("refNum" -> refNum).withFormUrlEncodedBody("declaration" -> "true").withHeaders(HeaderNames.xSessionId -> sessionId)
      val response = Await.result(controller.submit()(request), 5 seconds)

      response.header.status should equal(302)
      assert(response.header.headers("Location") === confirmationUrl)


      And("The Business rental information submission process is initiated")
      submit.assertBRISubmittedFor(refNum)
  }

  "When a submission is received and the declaration has not been agreed to" should
    "A redirect to the declaration error page is returned" in {

      val request = FakeRequest().withSession(("refNum" -> refNum)).withFormUrlEncodedBody(("declaration" -> "false"))
      val response = Await.result(controller.submit()(request), 5 seconds)

      response.header.status should equal(302)
      assert(response.header.headers("Location") === declarationErrorUrl)
  }

  object TestData {
    lazy val refNum = "adfiwerq08342kfad"
    lazy val sessionId = "sessionid"

    lazy val confirmationUrl = controllers.feedback.routes.SurveyController.confirmation.url

    lazy val declarationErrorUrl = controllers.routes.ApplicationController.declarationError.url

  }

}

object StubSubmitBRI {
  def apply() = new StubSubmitBRI
}

class StubSubmitBRI extends SubmitBusinessRentalInformation with should.Matchers {
  lazy val stubSubmission = Submission(
    None, None, None, None, None, None, None, None, None, None, None, None, None, None, None
  )

  var submittedRefNums: Seq[String] = Seq.empty

  def apply(refNum: String)(implicit hc: HeaderCarrier): Future[Submission] = {
    Console.println(s"=== called apply with : ${refNum} ===")
    Future.successful {
      submittedRefNums = submittedRefNums :+ refNum;
      stubSubmission
    }
  }

  def assertBRISubmittedFor(refNum: String) {
    submittedRefNums should equal(Seq(refNum))
  }

}
