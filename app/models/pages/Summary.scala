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

package models.pages

import models.serviceContracts.submissions.*
import play.api.libs.json.{Json, OFormat}

import java.time.ZonedDateTime

case class Summary(
  referenceNumber: String,
  journeyStarted: ZonedDateTime,
  addressConnection: Option[AddressConnectionType] = None,
  propertyAddress: Option[Address] = None,
  customerDetails: Option[CustomerDetails] = None,
  theProperty: Option[PageThree] = None,
  sublet: Option[PageFour] = None,
  landlord: Option[PageFive] = None,
  lease: Option[PageSix] = None,
  rentReviews: Option[PageSeven] = None,
  rentAgreement: Option[RentAgreement] = None,
  rent: Option[PageNine] = None,
  rentIncludes: Option[WhatRentIncludes] = None,
  incentives: Option[IncentivesAndPayments] = None,
  responsibilities: Option[PageTwelve] = None,
  alterations: Option[PropertyAlterations] = None,
  otherFactors: Option[OtherFactors] = None,
  address: Option[Address] = None,
  journeyResumptions: Seq[ZonedDateTime] = Seq.empty
):

  def addressVOBelievesIsCorrect: Address = address.getOrElse(throw VOHeldAddressSelectionError(referenceNumber))

  def addressUserBelievesIsCorrect: Address =
    addressConnection.flatMap {
      case AddressConnectionType.`yes-change-address` => propertyAddress orElse address
      case _                                          => address
    }.orElse(address).getOrElse(throw UsersAddressSelectionError(referenceNumber))

  val submitter: String        = customerDetails.map(_.fullName) getOrElse "No Name Supplied"
  val isAgent: Boolean         = customerDetails.exists(c => c.userType == UserType.occupiersAgent || c.userType == UserType.ownersAgent)
  val lastLogin: ZonedDateTime = journeyResumptions.lastOption.getOrElse(journeyStarted)

  def isUnderReview: Boolean = addressConnection match
    case Some(AddressConnectionType.`yes-change-address`) =>
      address.map(_.singleLine) != propertyAddress.map(_.singleLine)
    case _                                                => false

object Summary:
  implicit val format: OFormat[Summary] = Json.format

case class NoMainAddress(refNum: String) extends Exception(refNum)
case object NoReferenceNumber extends Exception
case class UsersAddressSelectionError(refNum: String) extends Exception(refNum)
case class VOHeldAddressSelectionError(refNum: String) extends Exception(refNum)
