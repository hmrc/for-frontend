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

package models.serviceContracts.submissions

case class Submission(
  propertyAddress: Option[Address],
  customerDetails: Option[CustomerDetails],
  theProperty: Option[TheProperty],
  sublet: Option[Sublet],
  landlord: Option[Landlord],
  lease: Option[LeaseOrAgreement],
  rentReviews: Option[RentReviews],
  rentAgreement: Option[RentAgreement],
  rent: Option[Rent],
  rentIncludes: Option[WhatRentIncludes],
  incentives: Option[IncentivesAndPayments],
  responsibilities: Option[Responsibilities],
  alterations: Option[PropertyAlterations],
  otherFactors: Option[OtherFactors],
  referenceNumber: Option[String] = None)
