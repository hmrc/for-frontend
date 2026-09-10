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

package useCases

import actions.RefNumRequest
import connectors.{Audit, Document, Page}
import helpers.AddressAuditing
import models.*
import models.serviceContracts.submissions.*
import org.scalatest.RecoverMethods.recoverToExceptionIf
import play.api.i18n.DefaultMessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import util.DateUtil.nowInUK
import utils.stubs.*

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class SubmitBusinessRentalInformationSpec extends BaseSpec:

  import TestData.*

  given HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  given RefNumRequest[?] = RefNumRequest("refNum", FakeRequest(), DefaultMessagesApi())

  private val audit          = mock[Audit]
  private val auditAddresses = mock[AddressAuditing]

  "Assuming a complete, valid document (representing FOR submission details) exists for a refNum" when {
    val repo = StubFormDocumentRepo((sessionId, refNum, document))
    builder.stubBuild(document, submission)

    "a submission for the refNum is received" should {
      val submit = SubmitBusinessRentalInformationToBackendApi(repo, builder, subConnector, audit, auditAddresses)
      submit(refNum).futureValue

      "the information will be formatted using the submission schema and posted to the back-end" in
        subConnector.verifyWasSubmitted(refNum, submission)
    }
  }

  "When a document for the refNum does not exist" should {
    "return error" in {
      val invalidRefNum = "adlkjfalsjd"
      val ex            = recoverToExceptionIf[RentalInformationCouldNotBeRetrieved] {
        val submit = SubmitBusinessRentalInformationToBackendApi(StubFormDocumentRepo(), builder, subConnector, audit, auditAddresses)
        submit(invalidRefNum)
      }.futureValue

      ex.refNum shouldBe invalidRefNum
    }
  }

  object TestData:

    val submission: Submission = Submission(
      None,
      Some(CustomerDetails("fn", UserType.occupier, ContactDetails("01234567890", "abc@mailinator.com"))),
      Some(TheProperty("Stuff", OccupierType.individuals, None, None, false, None, None)),
      Some(Sublet(false, List.empty)),
      Some(Landlord("abc", Some(Address("abc", None, Some("xyz"), "blah")), LandlordConnectionType.noConnected, None)),
      Some(LeaseOrAgreement(LeaseAgreementType.verbal, Some(false), None, Some(false), List.empty, Some(RoughDate(None, None, 2011)), Some(false), None)),
      Some(RentReviews(false, None)),
      Some(RentAgreement(false, None, RentSetByType.newLease)),
      Some(Rent(Some(20.1), LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 1), false, RentBaseType.openMarket, None)),
      Some(WhatRentIncludes(false, true, false, false, false, None, Parking(false, None, false, None, None, None))),
      Some(IncentivesAndPayments(false, None, true, None, true, None)),
      Some(Responsibilities(ResponsibleType.landlord, ResponsibleType.landlord, ResponsibleType.landlord, false, true, false, List.empty)),
      Some(PropertyAlterations(false, List.empty, None)),
      Some(OtherFactors(false, Some("xyz")))
    )

    val refNum = "a3akdfjas"

    val pages: Seq[Page]   = Nil
    val document: Document = Document(refNum, nowInUK, pages)

    val subConnector: StubSubmissionConnector = StubSubmissionConnector()
    val builder: StubSubmissionBuilder        = StubSubmissionBuilder()
    val sessionId                             = "sdfjasdljfasldjfasd"
