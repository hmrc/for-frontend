/*
 * Copyright 2023 HM Revenue & Customs
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

package models.pages

import models.serviceContracts.submissions._

import java.time.ZonedDateTime

case class Summary(
  referenceNumber: String,
  journeyStarted: ZonedDateTime,
  addressConnection: Option[AddressConnectionType],
  propertyAddress: Option[Address],
  customerDetails: Option[CustomerDetails],
  theProperty: Option[PageThree],
  sublet: Option[PageFour],
  landlord: Option[PageFive],
  lease: Option[PageSix],
  rentReviews: Option[PageSeven],
  rentAgreement: Option[RentAgreement],
  rent: Option[PageNine],
  rentIncludes: Option[WhatRentIncludes],
  incentives: Option[IncentivesAndPayments],
  responsibilities: Option[PageTwelve],
  alterations: Option[PropertyAlterations],
  otherFactors: Option[OtherFactors],
  address: Option[Address] = None,
  journeyResumptions: Seq[ZonedDateTime] = Seq.empty
  ) {

  lazy val addressVOABelievesIsCorrect = address.getOrElse(throw new VOAHeldAddressSelectionError(referenceNumber))

  def addressUserBelievesIsCorrect: Address = {
    addressConnection.flatMap {
      case AddressConnectionTypeYesChangeAddress => propertyAddress orElse address
      case _ => address
    }.orElse(address).getOrElse(throw UsersAddressSelectionError(referenceNumber) )
  }

//  lazy val addressUserBelievesIsCorrect: Address = {
//    if(propertyAddress.exists(_.isAddressCorrect)) address else propertyAddress.flatMap(_.address)
//  }.getOrElse(throw UsersAddressSelectionError(referenceNumber))

  lazy val submitter = customerDetails.map(_.fullName) getOrElse "No Name Supplied"
  lazy val isAgent = customerDetails.exists(c => c.userType == UserTypeOccupiersAgent || c.userType == UserTypeOwnersAgent)
  lazy val lastLogin = journeyResumptions.lastOption.getOrElse(journeyStarted)
//  lazy val isUnderReview =
//    propertyAddress.exists(_.isAddressCorrect == false) && address.map(_.singleLine) != propertyAddress.flatMap(_.address.map(_.singleLine))

  def isUnderReview: Boolean = addressConnection match {
    case Some(AddressConnectionTypeYesChangeAddress) => {
      address.map(_.singleLine) != propertyAddress.map(_.singleLine)
    }
    case _ => false
  }


}

case class NoMainAddress(refNum: String) extends Exception(refNum)
case object NoReferenceNumber extends Exception
case class UsersAddressSelectionError(refNum: String) extends Exception(refNum)
case class VOAHeldAddressSelectionError(refNum: String) extends Exception(refNum)
