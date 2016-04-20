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

import models.serviceContracts.submissions
import models.serviceContracts.submissions._
import org.scalatest.{FlatSpec, Matchers}
import play.api.data.FormError

class PageTwoFormMappingSpec extends FlatSpec with Matchers {

  import TestData._
  import form.PageTwoForm._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  behavior of "page two form mapping"

  "page two mapping" should "show required errors for fullName, userType, contactType and contactAddressType when given empty data" in {
    val formData: Map[String, String] = Map()
    val form = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
    mustContainError(errorKey.userType, Errors.noValueSelected, form)
    mustContainError(errorKey.contactType, Errors.noValueSelected, form)
  }

  it should "error if fullName is missing " in {
    val formData = baseFormData - errorKey.fullName
    val form = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
  }

  it should "error if userType is missing" in {
    val formData = baseFormData - errorKey.userType
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.noValueSelected, form)
  }

  it should "error if contactType is missing " in {
    val formData = baseFormData - errorKey.contactType
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.contactType, Errors.noValueSelected, form)
  }

  it should "return required error for contactAddressType if contactAddressType is empty and userType is owner or occupier" in {
    Seq(UserTypeOwner.name, UserTypeOccupier.name).foreach { ut =>
      val data = baseFormData.updated(errorKey.userType, ut) - errorKey.contactAddressType
      val form = pageTwoForm.bind(data)

      mustContainRequiredErrorFor(errorKey.contactAddressType, form)
    }
  }

  it should "not return a required error for contactAddressType if the userType is a type of agent" in {
    Seq(UserTypeOccupiersAgent.name, UserTypeOwnersAgent.name).foreach { ut =>
      val data = baseFormData.updated(errorKey.userType, ut) - errorKey.contactAddressType
      val form = pageTwoForm.bind(data)

      doesNotContainErrors(form)
    }
  }

  it should "bind to a PageTwoData if a full set of valid data is supplied" in {
    val formData = baseFormData
    val boundForm = pageTwoForm.bind(formData)
    boundForm.hasErrors should be(false)
    boundForm.value.isDefined should be(true)

    val pageTwoData = boundForm.value.get

    pageTwoData.fullName should be("Mr John Smith")
    pageTwoData.userType should be(UserTypeOwner)
    pageTwoData.contactType should be(ContactTypePhone)
    pageTwoData.contactAddressType should be(Some(ContactAddressTypeMain))
  }

  it should "error if invalid userType is provided" in {
    val formData = baseFormData.updated("userType", "owner1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.noValueSelected, form)
  }

  it should "error if invalid contactType is provided" in {
    val formData = baseFormData.updated("contactType", "phone1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.contactType, Errors.noValueSelected, form)
  }

  it should "error if the contact's email addresses do not match" in {
    val formData = baseFormData.updated("contactType", "both").updated("contactDetails.email2", "other@gmail.com")
    val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

    mustContainPrefixedError(errorKey.email1, Errors.emailMismatch, form)
    mustContainPrefixedError(errorKey.email2, Errors.emailMismatch, form)
  }

  it should "error if the contact type is Phone or Both but there is no phone number" in {
    ContactTypes.all.filter(_ != ContactTypeEmail).foreach { ct =>
      val formData: Map[String, String] = baseFormData.updated("contactType", ct.name) - errorKey.contactDetailsPhone
      val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

      mustContainError(errorKey.phone, Errors.required, form)
    }
  }

  it should "error if the contact type is Email or Both but there is no email address" in {
    submissions.ContactTypes.all.filter(_ != ContactTypePhone).foreach { ct =>
      val formData: Map[String, String] = baseFormData.updated("contactType", ct.name) - "contactDetails.email1" - "contactDetails.email2"
      val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

      mustContainError(errorKey.email1, Errors.required, form)
    }
  }

  it should "error if user type is owner or occupier and the contact address type is alternative address but there is no alternative address" in {
    Seq(UserTypeOwner.name, UserTypeOccupier.name).foreach { ut =>
      val formData = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeAddress.name).updated("userType", ut)
      val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

      val requiredFields = Seq(errorKey.alternativeAddressBuilding, errorKey.alternativeAddressPostCode)
      mustOnlyContainRequiredErrorsFor(requiredFields, form)
    }
  }

  it should "require an email, a phone number, or an address when alternative contact is specified" in {
    val formData = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeContact.name)
      .updated(errorKey.alternativeContactFullName, "11111") -
      errorKey.alternativeContactEmail1 - errorKey.alternativeContactEmail2 - errorKey.alternativeContactPhone -
      errorKey.alternativeAddressBuilding - errorKey.alternativeAddressStreet1 - errorKey.alternativeAddressPostCode

    mustContainError("alternativeContact", Errors.contactDetailsMissing, pageTwoForm.bind(formData))
  }

  it should "validate alternative address when contact address type is alternative address" in {
    val data = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeAddress.name)
    validateAddress(pageTwoForm, data, "alternativeAddress")
  }

  it should "validate the phone number when the preferred contact method is phone" in {
    validatePhone(pageTwoForm, baseFormData, "contactDetails")
  }

  it should "validate the phone number when contact address type is alternative contact" in {
    val data = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeContact.name)
      .updated(errorKey.alternativeContactFullName, "CONTACT FULL NAME")
      .updated(errorKey.alternativeContactPhone, "12345 12345")
      .updated(errorKey.alternativeContactEmail1, "david@test.com")
      .updated(errorKey.alternativeContactEmail2, "david@test.com")
      .updated(errorKey.alternativeContactBuildingName, "15")
      .updated(errorKey.alternativeContactPostcode, "AA11 1AA")
    validatePhone(pageTwoForm, data, "alternativeContact.contactDetails")
  }

  it should "validate full name" in {
    validateLettersNumsSpecCharsUptoLength(errorKey.fullName, 50, pageTwoForm, baseFormData)
  }

  it should "bind to the alternative contact details when the contact address type is alternative contact" in {
    val formData = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeContact.name)
      .updated(errorKey.alternativeContactFullName, "CONTACT FULL NAME")
      .updated(errorKey.alternativeContactPhone, "12345 12345")
      .updated(errorKey.alternativeContactEmail1, "david@test.com")
      .updated(errorKey.alternativeContactEmail2, "david@test.com")
      .updated(errorKey.alternativeContactBuildingName, "15")
      .updated(errorKey.alternativeContactPostcode, "AA11 1AA")
    val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

    mustBind(form) { pageTwoData =>
      pageTwoData.contactAddressType should be(Some(ContactAddressTypeAlternativeContact))
      pageTwoData.alternativeContact.get.contactDetails.get.phone.get should be("12345 12345")
      pageTwoData.alternativeContact.get.contactDetails.get.email.get should be("david@test.com")
      pageTwoData.alternativeContact.get.address.get.buildingNameNumber should be("15")
      pageTwoData.alternativeContact.get.address.get.postcode should be("AA11 1AA")
    }
  }

  it should "restrict the alterantive contact's name to 50 characters" in {
    val formData = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeContact.name)
      .updated(errorKey.alternativeContactFullName, "11111")
      .updated(errorKey.alternativeContactPhone, "12345 12345")
      .updated(errorKey.alternativeContactEmail1, "david@test.com")
      .updated(errorKey.alternativeContactEmail2, "david@test.com")
      .updated(errorKey.alternativeContactBuildingName, "15")
      .updated(errorKey.alternativeContactPostcode, "AA11 1AA")

    validateLettersNumsSpecCharsUptoLength(errorKey.alternativeContactFullName, 50, pageTwoForm, formData)
  }

  it should "not require alternative contact details when contact addres type is alternative contact IF the user is a type of agent" in {
    Seq(UserTypeOwnersAgent.name, UserTypeOccupiersAgent.name) foreach { agentType =>
      val d = baseFormData.updated(errorKey.userType, agentType)
        .updated(errorKey.contactAddressType, ContactAddressTypeAlternativeContact.name)

      mustBind(pageTwoForm.bind(d)) { x => assert(x.userType.name == agentType) }
    }
  }

  object TestData {
    val errorKey = new {
      val fullName: String = "fullName"
      val userType: String = "userType"
      val contactType: String = "contactType"
      val contactAddressType: String = "contactAddressType"
      val phone = "contactDetails.phone"
      val email1 = "contactDetails.email1"
      val email2 = "contactDetails.email2"
      val alternativeAddressBuilding = "alternativeAddress.buildingNameNumber"
      val alternativeAddressStreet1 = "alternativeAddress.street1"
      val alternativeAddressStreet2 = "alternativeAddress.street2"
      val alternativeAddressPostCode = "alternativeAddress.postcode"
      val alternativeContactBuildingName = "alternativeContact.address.buildingNameNumber"
      val alternativeContactStreet1 = "alternativeContact.address.street1"
      val alternativeContactStreet2 = "alternativeContact.address.street2"
      val alternativeContactPostcode = "alternativeContact.address.postcode"
      val alternativeContactEmail1 = "alternativeContact.contactDetails.email1"
      val alternativeContactEmail2 = "alternativeContact.contactDetails.email2"
      val alternativeContactPhone = "alternativeContact.contactDetails.phone"
      val alternativeContactFullName = "alternativeContact.fullName"
      val contactDetailsPhone = "contactDetails.phone"
    }

    val formErrors = new {
      val required = new {
        val fullName = FormError(errorKey.fullName, Errors.required)
      }
    }

    val baseFormData: Map[String, String] = Map(
      "userType" -> "owner",
      "contactType" -> "phone",
      "contactAddressType" -> "mainAddress",
      "contactDetails.phone" -> "01234 123123",
      "contactDetails.email1" -> "blah.blah@test.com",
      "contactDetails.email2" -> "blah.blah@test.com",
      "fullName" -> "Mr John Smith")
  }

}
