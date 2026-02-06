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

import models.pages._
import models.serviceContracts.submissions._
import play.api.libs.json._

package object models:

  private def generateWrites[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Writes[T] = (data: T) => JsString(data.name)

  private def generateReads[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Reads[T] = {
    case JsString(value) =>
      enumObject.fromName(value) match {
        case Some(enumValue) => JsSuccess(enumValue)
        case None            => JsError()
      }
    case _               =>
      JsError()
  }

  private def generateFormat[T <: NamedEnum](enumObject: NamedEnumSupport[T]): Format[T] = Format[T](generateReads(enumObject), generateWrites(enumObject))

  implicit val formatPropertyType: Format[PropertyType]                     = generateFormat(PropertyTypes)
  implicit val formatContactAddressType: Format[ContactAddressType]         = generateFormat(ContactAddressTypes)
  implicit val formatUserType: Format[UserType]                             = generateFormat(UserTypes)
  implicit val formatOccupierType: Format[OccupierType]                     = generateFormat(OccupierTypes)
  implicit val format: Format[LandlordConnectionType]                       = generateFormat(LandlordConnectionTypes)
  implicit val formatLeaseAgreementType: Format[LeaseAgreementType]         = generateFormat(LeaseAgreementTypes)
  implicit val formatNotReviewRentFixedType: Format[NotReviewRentFixedType] = generateFormat(NotReviewRentFixedTypes)
  implicit val formatRentSetByType: Format[RentSetByType]                   = generateFormat(RentSetByTypes)
  implicit val formatRentFixedByType: Format[RentFixedType]                 = generateFormat(RentFixedTypes)
  implicit val formatReviewalIntervalType: Format[ReviewIntervalType]       = generateFormat(ReviewIntervalTypes)
  implicit val formatResponsibleType: Format[ResponsibleType]               = generateFormat(ResponsibleTypes)
//  implicit val formatResponsibleTypeOutsideRepairs: Format[ResponsibleTypeOutsideRepairs] = generateFormat(ResponsibleTypeOutsideRepairs)
  implicit val formatRentBaseType: Format[RentBaseType]                     = generateFormat(RentBaseTypes)
  implicit val formatRentLengthType: Format[RentLengthType]                 = generateFormat(RentLengthTypes)
  implicit val formatAlterationSetByType: Format[AlterationSetByType]       = generateFormat(AlterationSetByType)
  implicit val formatAddressConnection: Format[AddressConnectionType]       = generateFormat(AddressConnectionTypes)
  implicit val tenantAddressType: Format[TenantsAddressType]                = generateFormat(TenantsAddressTypes)
  implicit val formatsubletType: Format[SubletType]                         = generateFormat(SubletType)

  implicit val annr: OFormat[AnnualRent]                                                = Json.format[AnnualRent]
  implicit val chargeDetailFormat: OFormat[ChargeDetails]                               = Json.format[ChargeDetails]
  implicit val roughDateFormat: OFormat[RoughDate]                                      = Json.format[RoughDate]
  implicit val paymentDetailsFormat: OFormat[PaymentDetails]                            = Json.format[PaymentDetails]
  implicit val stepDetailsFormat: OFormat[SteppedDetails]                               = Json.format[SteppedDetails]
  implicit val monthsYearFormat: OFormat[MonthsYearDuration]                            = Json.format[MonthsYearDuration]
  implicit val freePeriodDetailsFormat: OFormat[FreePeriodDetails]                      = Json.format[FreePeriodDetails]
  implicit val capitalDetailsFormat: OFormat[CapitalDetails]                            = Json.format[CapitalDetails]
  implicit val addressFormat: OFormat[Address]                                          = Json.format[Address]
  implicit val contactDetailsFormat: OFormat[ContactDetails]                            = Json.format[ContactDetails]
  implicit val formatParkingDetails: OFormat[ParkingDetails]                            = Json.format[ParkingDetails]
  implicit val formatPropertyAlterationsDetails: OFormat[PropertyAlterationsDetails]    = Json.format[PropertyAlterationsDetails]
  implicit val pageSevenRentReviewResultDetailsFormat: OFormat[RentReviewResultDetails] = Json.format[RentReviewResultDetails]
  implicit val pageSevenRentReviewDetailsFormat: OFormat[RentReviewDetails]             = Json.format[RentReviewDetails]
  implicit val subletDataFormat: OFormat[SubletData]                                    = Json.format[SubletData]
  implicit val pageSevenDetailsFormat: OFormat[PageSevenDetails]                        = Json.format[PageSevenDetails]

  implicit val cdf: OFormat[CustomerDetails]        = Json.format[CustomerDetails]
  implicit val tpf: OFormat[TheProperty]            = Json.format[TheProperty]
  implicit val subf: OFormat[Sublet]                = Json.format[Sublet]
  implicit val llf: OFormat[Landlord]               = Json.format[Landlord]
  implicit val loaf: OFormat[LeaseOrAgreement]      = Json.format[LeaseOrAgreement]
  implicit val rrf: OFormat[RentReviews]            = Json.format[RentReviews]
  implicit val raf: OFormat[RentAgreement]          = Json.format[RentAgreement]
  implicit val renf: OFormat[Rent]                  = Json.format[Rent]
  implicit val parf: OFormat[Parking]               = Json.format[Parking]
  implicit val wrif: OFormat[WhatRentIncludes]      = Json.format[WhatRentIncludes]
  implicit val incf: OFormat[IncentivesAndPayments] = Json.format[IncentivesAndPayments]
  implicit val resf: OFormat[Responsibilities]      = Json.format[Responsibilities]
  implicit val palf: OFormat[PropertyAlterations]   = Json.format[PropertyAlterations]
  implicit val otf: OFormat[OtherFactors]           = Json.format[OtherFactors]
  implicit val smnf: OFormat[Submission]            = Json.format[Submission]
  implicit val wrag: OFormat[WrittenAgreement]      = Json.format[WrittenAgreement]
  implicit val veag: OFormat[VerbalAgreement]       = Json.format[VerbalAgreement]
  implicit val sdfm: OFormat[SubletDetails]         = Json.format[SubletDetails]
  implicit val p3f: OFormat[PageThree]              = Json.format[PageThree]
  implicit val p4f: OFormat[PageFour]               = Json.format[PageFour]
  implicit val p5f: OFormat[PageFive]               = Json.format[PageFive]
  implicit val p6f: OFormat[PageSix]                = Json.format[PageSix]
  implicit val p7f: OFormat[PageSeven]              = Json.format[PageSeven]
  implicit val p9f: OFormat[PageNine]               = Json.format[PageNine]
  implicit val p12f: OFormat[PageTwelve]            = Json.format[PageTwelve]
