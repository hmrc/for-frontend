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

package useCases

import _root_.utils.stubs._
import actions.RefNumRequest
import connectors.{Audit, Document, Page}
import helpers.AddressAuditing
import models._
import models.serviceContracts.submissions._
import org.joda.time.{DateTime, LocalDate}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.DefaultMessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class SubmitBusinessRentalInformationSpec extends AnyWordSpec with should.Matchers with MockitoSugar {
	import TestData._
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  implicit val request: RefNumRequest[_] = new RefNumRequest("refNum", FakeRequest(), new DefaultMessagesApi)

  private val audit = mock[Audit]
  private val auditAddresses = mock[AddressAuditing]

  "Assuming a complete, valid document (representing FOR submission details) exists for a refNum" when {
    val repo = StubFormDocumentRepo((sessionId, refNum, document))
    builder.stubBuild(document, submission)

    "a submission for the refNum is received" should {
      val submit = new SubmitBusinessRentalInformationToBackendApi(repo, builder, subConnector, audit, auditAddresses)
      Await.result(submit(refNum), 10 seconds)

      "The information will be formatted using the submission schema and posted to the back-end" in {
        subConnector.verifyWasSubmitted(refNum, submission)
      }
    }

  }

  "An error is returned when a document for the refNum does not exist" in {
    val invalidRefNum = "adlkjfalsjd"
    val ex = intercept[RentalInformationCouldNotBeRetrieved] {
      val submit = new SubmitBusinessRentalInformationToBackendApi(StubFormDocumentRepo(), builder, subConnector, audit, auditAddresses)
      Await.result(submit(invalidRefNum), 10 seconds)
    }
    assert(ex.refNum === invalidRefNum)
  }

  object TestData {
    val submission = Submission(
      None,
      Some(CustomerDetails("fn", UserTypeOccupier, ContactDetails("01234567890", "abc@mailinator.com"))),
      Some(TheProperty("Stuff", OccupierTypeIndividuals, None, None, false, None, None)),
      Some(Sublet(false, List.empty)),
      Some(Landlord(Some("abc"), Some(Address("abc", None, Some("xyz"), "blah")), LandlordConnectionTypeNone, None)),
      Some(LeaseOrAgreement(LeaseAgreementTypesVerbal, Some(false), None, Some(false), List.empty, Some(RoughDate(None, None, 2011)), Some(false), None)),
      Some(RentReviews(false, None)),
      Some(RentAgreement(false, None, RentSetByTypeNewLease)),
      Some(Rent(Some(20.1), new LocalDate(2011, 1, 1), new LocalDate(2011, 1, 1), false, RentBaseTypeOpenMarket, None)),
      Some(WhatRentIncludes(false, true, false, false, false, None, Parking(false, None, false, None, None, None))),
      Some(IncentivesAndPayments(false, None, true, None, true, None)),
      Some(Responsibilities(ResponsibleLandlord, ResponsibleLandlord, ResponsibleLandlord, false, true, false, List.empty)),
      Some(PropertyAlterations(false, List.empty, None)),
      Some(OtherFactors(false, Some("xyz"))))

    val refNum = "a3akdfjas"

    val pages = Seq.empty[Page]
    val document = Document(refNum, DateTime.now, pages)

    val subConnector = StubSubmissionConnector()
    val builder = StubSubmissionBuilder()
    val sessionId =  "sdfjasdljfasldjfasd"
  }

}
