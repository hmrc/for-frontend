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

package useCases

import _root_.utils.stubs._
import connectors.{Document, Page}
import models._
import models.serviceContracts.submissions._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FreeSpec, Matchers}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Await
import scala.concurrent.duration._

class SubmitBusinessRentalInformationSpec extends FreeSpec with Matchers {
	import TestData._
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

  "Assuming a complete, valid document (representing FOR submission details) exists for a refNum" - {
    val repo = StubFormDocumentRepo((sessionId, refNum, document))
    builder.stubBuild(document, submission)

    "When a submission for the refNum is received" - {
      Await.result(SubmitBusinessRentalInformation(repo, builder, subConnector)(refNum), 10 seconds)

      "The information will be formatted using the submission schema and posted to the back-end" in {
        subConnector.verifyWasSubmitted(refNum, submission)
      }
    }

  }

  "An error is returned when a document for the refNum does not exist" in {
    val invalidRefNum = "adlkjfalsjd"
    val ex = intercept[RentalInformationCouldNotBeRetrieved] {
      Await.result(SubmitBusinessRentalInformation(StubFormDocumentRepo(), builder, subConnector)(invalidRefNum), 10 seconds)
    }
    assert(ex.refNum === invalidRefNum)
  }

  object TestData {
    val submission = Submission(
      Some(PropertyAddress(true, None)),
      Some(CustomerDetails("fn", UserTypeOccupier, ContactTypeEmail, ContactDetails(None, None, None))),
      Some(TheProperty("Stuff", OccupierTypeIndividuals, None, None, false, None)),
      Some(Sublet(false, List.empty)),
      Some(Landlord("abc", Some(Address("abc", None, Some("xyz"), "blah")), LandlordConnectionTypeNone, None)),
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
