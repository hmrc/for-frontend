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

import connectors.{Document, SubmissionConnector}
import form.persistence.FormDocumentRepository
import models.journeys.{Journey, Paths}
import models.pages._
import models.serviceContracts.submissions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import playconfig.SessionId
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait SubmitBusinessRentalInformation {
  def apply(refNum: String)(implicit hs: HeaderCarrier): Future[Submission]
}

object SubmitBusinessRentalInformation {
  def apply(repository: FormDocumentRepository, builder: SubmissionBuilder, subConnector: SubmissionConnector): SubmitBusinessRentalInformation = {
    new SubmitBusinessRentalInformationToBackendApi(repository, builder, subConnector)
  }
}

class SubmitBusinessRentalInformationToBackendApi(repository: FormDocumentRepository, builder: SubmissionBuilder,
  subConnector: SubmissionConnector) extends SubmitBusinessRentalInformation {

  def apply(refNum: String)(implicit hc: HeaderCarrier): Future[Submission] = {
    repository.findById(SessionId(hc), refNum) .flatMap {
      case Some(doc) =>
        val sub = builder.build(doc)
        subConnector.submit(refNum, sub) map { _ => sub }
      case None => Future.failed(new RentalInformationCouldNotBeRetrieved(refNum))
    }
  }
}

trait SubmissionBuilder {
  def build(doc: Document): Submission
}

object SubmissionBuilder extends SubmissionBuilder {

  def build(doc: Document): Submission = {
    val s: Summary = SummaryBuilder.build(doc)
    if (Paths.isShortPath(s)) buildShortSubmission(s, doc) else buildSubmission(s, doc)
  }

  private def buildShortSubmission(summary: Summary, doc: Document) = {
    implicit val s: Summary = summary
    Submission(s.propertyAddress, s.customerDetails, s.theProperty.map(toTheProperty), s.sublet.map(toSublet), None,
      None, None, None, None, None, None, None, None, None, Some(doc.referenceNumber))
  }

  private def buildSubmission(summary: Summary, doc: Document) = {
    implicit val s: Summary = summary
    Submission(s.propertyAddress, s.customerDetails, s.theProperty.map(toTheProperty), s.sublet.map(toSublet), s.landlord.map(toLandlord),
      s.lease.map(toLeaseOrAgreement), s.rentReviews.map(toRentReviews), s.rentAgreement,
      s.rent.map(toRent), s.rentIncludes, s.incentives, s.responsibilities.map(toResponsibilities),
      s.alterations, s.otherFactors,
      referenceNumber = Some(doc.referenceNumber)
    )
  }

  private def toTheProperty(p3: PageThree) = TheProperty(
    p3.propertyType, p3.occupierType, occupierNameFor(p3), p3.firstOccupationDate, p3.propertyOwnedByYou, p3.propertyRentedByYou
  )

  private def occupierNameFor(p3: PageThree) = p3.occupierType match {
    case OccupierTypeNobody => Some("Nobody")
    case OccupierTypeIndividuals => Some(p3.mainOccupierName.getOrElse(""))
    case OccupierTypeCompany =>
      Some(Seq(p3.occupierCompanyName, p3.occupierCompanyContact).flatten.mkString(" - ").take(50))
  }

  private def toSublet(p4: PageFour)(implicit sum: Summary) = Sublet(p4.propertyIsSublet, p4.sublet.map(toSubletData))

  private def toSubletData(s: SubletDetails)(implicit sum: Summary) = SubletData(
    s.tenantFullName, tenantsAddress(s, sum), s.subletPropertyPartDescription, s.subletPropertyReasonDescription,
    Some(s.annualRent), s.rentFixedDate
  )

  private def tenantsAddress(s: SubletDetails, sum: Summary) = Address (
    s.tenantAddress.buildingNameNumber, s.tenantAddress.street1, s.tenantAddress.street2, s.tenantAddress.postcode
  )

  private def toLandlord(p5: PageFive) = Landlord(
    p5.landlordFullName, p5.landlordAddress, p5.landlordConnectionType, p5.landlordConnectText
  )

  private def toLeaseOrAgreement(p6: PageSix) = p6 match {
    case PageSix(LeaseAgreementTypesVerbal, _, verbal) =>
      LeaseOrAgreement(
        p6.leaseAgreementType, None, None, None, List.empty, verbal.startDate, verbal.rentOpenEnded,
        verbal.leaseLength
      )
    case PageSix(_, Some(written), _) =>
      LeaseOrAgreement(
        p6.leaseAgreementType, Some(written.leaseAgreementHasBreakClause), written.breakClauseDetails, Some(written.agreementIsStepped),
        written.steppedDetails, Some(written.startDate), Some(written.rentOpenEnded), written.leaseLength
      )
    case _ =>
      LeaseOrAgreement(p6.leaseAgreementType, None, None, None, List.empty, None, None, None)
  }

  private def toRentReviews(p7: PageSeven) = RentReviews(p7.leaseContainsRentReviews, p7.pageSevenDetails.map(toRentReviewDetails))

  private def toRentReviewDetails(p7d: PageSevenDetails) = RentReviewDetails(
    p7d.reviewIntervalType match {
      case ReviewIntervalTypeEvery3Years => Some(MonthsYearDuration(0, 3))
      case ReviewIntervalTypeEvery5Years => Some(MonthsYearDuration(0, 5))
      case ReviewIntervalTypeEvery7Years => Some(MonthsYearDuration(0, 7))
      case ReviewIntervalTypeOther => p7d.reviewIntervalTypeSpecify
    }, p7d.lastReviewDate, p7d.canRentReduced, p7d.rentResultOfRentReview, p7d.reviewDetails
  )

  private def toRent(p9: PageNine) = Rent(
    Some(p9.totalRent.annualRent), p9.rentBecomePayable, p9.rentActuallyAgreed,
    p9.negotiatingNewRent, p9.rentBasis, p9.rentBasisOtherDetails
  )

  private def toResponsibilities(p12: PageTwelve) = Responsibilities(
    p12.responsibleOutsideRepairs, p12.responsibleInsideRepairs, p12.responsibleBuildingInsurance,
    p12.ndrCharges, p12.waterCharges, p12.includedServices, p12.includedServicesDetails ++ ndrAndWaterServices(p12)
  )

  private def ndrAndWaterServices(p12: PageTwelve): Seq[ChargeDetails] = {
    val ndr = p12.ndrDetails.map { ChargeDetails("Non-domestic Rates", _) }
    val water = p12.waterChargesCost.map { ChargeDetails("Water Charges", _) }
    Seq(ndr, water).flatten
  }
}

case class RentalInformationCouldNotBeRetrieved(refNum: String) extends Exception(refNum)
case class TenantsAddressMissing(refNum: String) extends Exception(refNum)
