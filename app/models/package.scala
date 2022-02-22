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

import models.pages._
import models.serviceContracts.submissions._
import play.api.libs.json._
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

package object models {

  def generateWrites[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Writes[T] = new Writes[T] {
    def writes(data: T): JsValue = {
      JsString(data.name)
    }
  }

  def generateReads[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Reads[T] = new Reads[T] {
    def reads(json: JsValue): JsResult[T] = json match {
      case JsString(value) =>
        enumObject.fromName(value) match {
          case Some(enumValue) => JsSuccess(enumValue)
          case None => JsError()
        }
      case _ =>
        JsError()
    }
  }

  def generateFormat[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Format[T] = Format[T](generateReads(enumObject), generateWrites(enumObject))

  implicit val formatPropertyType: Format[PropertyType] = generateFormat(PropertyTypes)
  implicit val formatContactAddressType: Format[ContactAddressType] = generateFormat(ContactAddressTypes)
  implicit val formatUserType: Format[UserType] = generateFormat(UserTypes)
  implicit val formatOccupierType: Format[OccupierType] = generateFormat(OccupierTypes)
  implicit val format: Format[LandlordConnectionType] = generateFormat(LandlordConnectionTypes)
  implicit val formatLeaseAgreementType: Format[LeaseAgreementType] = generateFormat(LeaseAgreementTypes)
  implicit val formatNotReviewRentFixedType: Format[NotReviewRentFixedType] = generateFormat(NotReviewRentFixedTypes)
  implicit val formatRentSetByType: Format[RentSetByType] = generateFormat(RentSetByTypes)
  implicit val formatRentFixedByType: Format[RentFixedType] = generateFormat(RentFixedTypes)
  implicit val formatReviewalIntervalType: Format[ReviewIntervalType] = generateFormat(ReviewIntervalTypes)
  implicit val formatResponsibleType: Format[ResponsibleType] = generateFormat(ResponsibleTypes)
//  implicit val formatResponsibleTypeOutsideRepairs: Format[ResponsibleTypeOutsideRepairs] = generateFormat(ResponsibleTypeOutsideRepairs)
  implicit val formatRentBaseType: Format[RentBaseType] = generateFormat(RentBaseTypes)
  implicit val formatRentLengthType: Format[RentLengthType] = generateFormat(RentLengthTypes)
  implicit val formatAlterationSetByType: Format[AlterationSetByType] = generateFormat(AlterationSetByType)
  implicit val formatAddressConnection: Format[AddressConnectionType] = generateFormat(AddressConnectionTypes)
  implicit val tenantAddressType: Format[TenantsAddressType] = generateFormat(TenantsAddressTypes)
  implicit val formatsubletType: Format[SubletType] = generateFormat(SubletType)

  implicit val annr = Json.format[AnnualRent]
  implicit val chargeDetailFormat = Json.format[ChargeDetails]
  implicit val roughDateFormat = Json.format[RoughDate]
  implicit val paymentDetailsFormat = Json.format[PaymentDetails]
  implicit val stepDetailsFormat = Json.format[SteppedDetails]
  implicit val monthsYearFormat = Json.format[MonthsYearDuration]
  implicit val freePeriodDetailsFormat = Json.format[FreePeriodDetails]
  implicit val capitalDetailsFormat = Json.format[CapitalDetails]
  implicit val addressFormat = Json.format[Address]
  implicit val contactDetailsFormat = Json.format[ContactDetails]
  implicit val formatParkingDetails = Json.format[ParkingDetails]
  implicit val formatPropertyAlterationsDetails = Json.format[PropertyAlterationsDetails]
  implicit val pageSevenRentReviewResultDetailsFormat = Json.format[RentReviewResultDetails]
  implicit val pageSevenRentReviewDetailsFormat = Json.format[RentReviewDetails]
  implicit val subletDataFormat = Json.format[SubletData]
  implicit val pageSevenDetailsFormat = Json.format[PageSevenDetails]

  implicit val cdf = Json.format[CustomerDetails]
  implicit val tpf = Json.format[TheProperty]
  implicit val subf = Json.format[Sublet]
  implicit val llf = Json.format[Landlord]
  implicit val loaf = Json.format[LeaseOrAgreement]
  implicit val rrf = Json.format[RentReviews]
  implicit val raf = Json.format[RentAgreement]
  implicit val renf = Json.format[Rent]
  implicit val parf = Json.format[Parking]
  implicit val wrif = Json.format[WhatRentIncludes]
  implicit val incf = Json.format[IncentivesAndPayments]
  implicit val resf = Json.format[Responsibilities]
  implicit val palf = Json.format[PropertyAlterations]
  implicit val otf = Json.format[OtherFactors]
  implicit val smnf = Json.format[Submission]
  implicit val wrag = Json.format[WrittenAgreement]
  implicit val veag = Json.format[VerbalAgreement]
  implicit val sdfm = Json.format[SubletDetails]
  implicit val p3f = Json.format[PageThree]
  implicit val p4f = Json.format[PageFour]
  implicit val p5f = Json.format[PageFive]
  implicit val p6f = Json.format[PageSix]
  implicit val p7f = Json.format[PageSeven]
  implicit val p9f = Json.format[PageNine]
  implicit val p12f = Json.format[PageTwelve]
}
