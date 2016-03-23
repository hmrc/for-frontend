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

import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import org.scalatest.{ FreeSpec, Matchers, MustMatchers }
import play.api.test.{FakeRequest, FakeApplication}
import play.api.mvc.{ Action, AnyContent, Request, Controller }
import play.api.mvc.Results._
import play.api.mvc.Results
import models.serviceContracts.submissions.Submission

import uk.gov.hmrc.play.http.HeaderCarrier
import actions.{ RefNumRequest, RefNumAction }
import useCases.SubmitBusinessRentalInformation

class FORSubmissionControllerSpec extends FreeSpec with Matchers {
	import TestData._

  "When a submission is received and the declaration has been agreed to" - {
    val submit = StubSubmitBRI()
  	val controller = createController(submit)
    val request = FakeRequest().withSession(("refNum" -> refNum)).withFormUrlEncodedBody(("declaration" -> "true"))
    val response = Await.result(controller.submit()(request), 5 seconds)

    "The Business rental information submission process is initiated" in {
      submit.assertBRISubmittedFor(refNum)
    }

    "A 302 response redirecting to the confirmation page is returned" in {
      response.header.status should equal(302)
      assert(response.header.headers("Location") === confirmationUrl)
    }

  }

  "When a submission is received and the declaration has not been agreed to" - {
    val controller = createController()
    val request = FakeRequest().withSession(("refNum" -> refNum)).withFormUrlEncodedBody(("declaration" -> "false"))
    val response = Await.result(controller.submit()(request), 5 seconds)

    "A redirect to the declaration error page is returned" in {
      response.header.status should equal(302)
      assert(response.header.headers("Location") === declarationErrorUrl)
    }
  }

  object TestData {
  	lazy val refNum = "adfiwerq08342kfad"
  	def confirmationUrl = controllers.feedback.routes.Survey.confirmation.url
    def declarationErrorUrl = controllers.routes.Application.declarationError.url

  	class TestController(val x: SubmitBusinessRentalInformation) extends FORSubmissionController with Controller {
			def submitBusinessRentalInformation = x
		}

    def createController(submitter: StubSubmitBRI = null) = {
      play.api.Play.start(FakeApplication(additionalConfiguration = Map("auditing.enabled" -> false)))
      new TestController(submitter)
    }
  }
}

object StubSubmitBRI {
	def apply() = new StubSubmitBRI
}

class StubSubmitBRI extends SubmitBusinessRentalInformation with MustMatchers {
  lazy val stubSubmission = Submission(
    None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None
  )
	
  var submittedRefNums: Seq[String] = Seq.empty

	def apply(refNum: String)(implicit hc: HeaderCarrier): Future[Submission] = {
		Future.successful { submittedRefNums = submittedRefNums :+ refNum; stubSubmission }
	}

	def assertBRISubmittedFor(refNum: String) {
		submittedRefNums must equal(Seq(refNum))
	}
}
