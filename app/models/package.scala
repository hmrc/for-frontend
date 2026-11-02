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

import models.pages.*
import models.serviceContracts.submissions.*
import play.api.libs.json.*
import uk.gov.hmrc.vo.service.model.Scala3EnumJsonFormat

package object models:

  implicit val formatUserType: Format[UserType]                             = Scala3EnumJsonFormat.format
  implicit val formatOccupierType: Format[OccupierType]                     = Scala3EnumJsonFormat.format
  implicit val format: Format[LandlordConnectionType]                       = Scala3EnumJsonFormat.format
  implicit val formatLeaseAgreementType: Format[LeaseAgreementType]         = Scala3EnumJsonFormat.format
  implicit val formatNotReviewRentFixedType: Format[NotReviewRentFixedType] = Scala3EnumJsonFormat.format
  implicit val formatRentSetByType: Format[RentSetByType]                   = Scala3EnumJsonFormat.format
  implicit val formatRentFixedByType: Format[RentFixedType]                 = Scala3EnumJsonFormat.format
  implicit val formatReviewalIntervalType: Format[ReviewIntervalType]       = Scala3EnumJsonFormat.format
  implicit val formatResponsibleType: Format[ResponsibleType]               = Scala3EnumJsonFormat.format
  implicit val formatRentBaseType: Format[RentBaseType]                     = Scala3EnumJsonFormat.format
  implicit val formatAlterationSetByType: Format[AlterationSetByType]       = Scala3EnumJsonFormat.format
  implicit val formatAddressConnection: Format[AddressConnectionType]       = Scala3EnumJsonFormat.format
  implicit val formatSubletType: Format[SubletType]                         = Scala3EnumJsonFormat.format

  implicit val formatAnnualRent: OFormat[AnnualRent]                                    = Json.format
  implicit val chargeDetailFormat: OFormat[ChargeDetails]                               = Json.format
  implicit val roughDateFormat: OFormat[RoughDate]                                      = Json.format
  implicit val paymentDetailsFormat: OFormat[PaymentDetails]                            = Json.format
  implicit val stepDetailsFormat: OFormat[SteppedDetails]                               = Json.format
  implicit val monthsYearFormat: OFormat[MonthsYearDuration]                            = Json.format
  implicit val freePeriodDetailsFormat: OFormat[FreePeriodDetails]                      = Json.format
  implicit val capitalDetailsFormat: OFormat[CapitalDetails]                            = Json.format
  implicit val addressFormat: OFormat[Address]                                          = Json.format
  implicit val contactDetailsFormat: OFormat[ContactDetails]                            = Json.format
  implicit val formatParkingDetails: OFormat[ParkingDetails]                            = Json.format
  implicit val formatPropertyAlterationsDetails: OFormat[PropertyAlterationsDetails]    = Json.format
  implicit val pageSevenRentReviewResultDetailsFormat: OFormat[RentReviewResultDetails] = Json.format
  implicit val pageSevenRentReviewDetailsFormat: OFormat[RentReviewDetails]             = Json.format
  implicit val subletDataFormat: OFormat[SubletData]                                    = Json.format
  implicit val pageSevenDetailsFormat: OFormat[PageSevenDetails]                        = Json.format

  implicit val cdf: OFormat[CustomerDetails]        = Json.format
  implicit val tpf: OFormat[TheProperty]            = Json.format
  implicit val subf: OFormat[Sublet]                = Json.format
  implicit val llf: OFormat[Landlord]               = Json.format
  implicit val loaf: OFormat[LeaseOrAgreement]      = Json.format
  implicit val rrf: OFormat[RentReviews]            = Json.format
  implicit val raf: OFormat[RentAgreement]          = Json.format
  implicit val renf: OFormat[Rent]                  = Json.format
  implicit val parf: OFormat[Parking]               = Json.format
  implicit val wrif: OFormat[WhatRentIncludes]      = Json.format
  implicit val incf: OFormat[IncentivesAndPayments] = Json.format
  implicit val resf: OFormat[Responsibilities]      = Json.format
  implicit val palf: OFormat[PropertyAlterations]   = Json.format
  implicit val otf: OFormat[OtherFactors]           = Json.format
  implicit val smnf: OFormat[Submission]            = Json.format
  implicit val wrag: OFormat[WrittenAgreement]      = Json.format
  implicit val veag: OFormat[VerbalAgreement]       = Json.format
  implicit val sdfm: OFormat[SubletDetails]         = Json.format
  implicit val p3f: OFormat[PageThree]              = Json.format
  implicit val p4f: OFormat[PageFour]               = Json.format
  implicit val p5f: OFormat[PageFive]               = Json.format
  implicit val p6f: OFormat[PageSix]                = Json.format
  implicit val p7f: OFormat[PageSeven]              = Json.format
  implicit val p9f: OFormat[PageNine]               = Json.format
  implicit val p12f: OFormat[PageTwelve]            = Json.format
