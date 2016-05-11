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

package form


import models.serviceContracts.submissions._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._
import MappingSupport._

object PageTwoForm {

  val ownerAndOccupier = Seq(UserTypeOwner.name, UserTypeOccupier.name)
  val agents = Seq(UserTypeOccupiersAgent.name, UserTypeOwnersAgent.name)

  val pageTwoForm: Form[CustomerDetails] = Form(mapping(
    "fullName" -> nonEmptyText(maxLength = 50),
    "userType" -> userType,
    "contactType" -> contactType,
    "contactDetails" -> contactDetailsMappingFor("contactType"),
    "contactAddressType" -> mandatoryAndOnlyIfAnyOf("userType", ownerAndOccupier, contactAddressTypeMapping),
    "alternativeAddress" -> mandatoryIfEqual(
      "contactAddressType", ContactAddressTypeAlternativeAddress.name, addressAbroadMapping("alternativeAddress")
    ),
    "alternativeContact" -> mandatoryIf(
      isEqual("contactAddressType", ContactAddressTypeAlternativeContact.name) and isNotAnyOf("userType", agents),
      alternativeContactMapping("alternativeContact")
    )
  )(CustomerDetails.apply)(CustomerDetails.unapply))
}
