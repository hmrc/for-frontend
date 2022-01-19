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

package form

import models.serviceContracts.submissions
import models.serviceContracts.submissions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.FormError

class PageTwoFormMappingSpec extends AnyFlatSpec with should.Matchers {

  import TestData._
  import form.PageTwoForm._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

  behavior of "page two form mapping"

  "page two mapping" should "show required errors for fullName, userType, contactType and contactAddressType when given empty data" in {
    val formData: Map[String, String] = Map()
    val form = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
    mustContainError(errorKey.contactType, Errors.contactTypeRequired, form)
  }

  it should "error if fullName is missing " in {
    val formData = baseFormData - errorKey.fullName
    val form = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
  }

  it should "error if userType is missing" in {
    val formData = baseFormData - errorKey.userType
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
  }

  it should "error if contactType is missing " in {
    val formData = baseFormData - errorKey.contactType
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.contactType, Errors.contactTypeRequired, form)
  }


  it should "not return a required error for contactAddressType if the userType is a type of agent" in {
    Seq(UserTypeOccupiersAgent.name, UserTypeOwnersAgent.name).foreach { ut =>
      val data = baseFormData.updated(errorKey.userType, ut) - errorKey.contactAddressType
      val form = pageTwoForm.bind(data)

      doesNotContainErrors(form)
    }
  }

  it should "error if invalid userType is provided" in {
    val formData = baseFormData.updated("userType", "owner1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
  }

  it should "error if invalid contactType is provided" in {
    val formData = baseFormData.updated("contactType", "phone1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.contactType, Errors.contactTypeRequired, form)
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

  it should "error if email adress is longer than 50 characters" in {
    submissions.ContactTypes.all.filter(_ != ContactTypePhone).foreach { ct =>
      val formData: Map[String, String] = baseFormData
        .updated("contactType", ct.name)
        .updated("contactDetails.email1", tooLongEmail)
        .updated("contactDetails.email2", tooLongEmail)
      val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

      mustContainError(errorKey.email1, errorKey.email1TooLong, form)
      mustContainError(errorKey.email2, errorKey.email2TooLong, form)
    }
  }

  it should "validate the phone number when the preferred contact method is phone" in {
    validatePhone(pageTwoForm, baseFormData, "contactDetails")
  }

  it should "validate full name" in {
    validateLettersNumsSpecCharsUptoLength(errorKey.fullName, 50, pageTwoForm, baseFormData)
  }

  it should "not require alternative contact details when contact address type is alternative contact IF the user is a type of agent" in {
    Seq(UserTypeOwnersAgent.name, UserTypeOccupiersAgent.name) foreach { agentType =>
      val d = baseFormData.updated(errorKey.userType, agentType)
        .updated(errorKey.contactAddressType, ContactAddressTypeAlternativeContact.name)

      mustBind(pageTwoForm.bind(d)) { x => assert(x.userType.name == agentType) }
    }
  }

  it should "only require one of phone, email, or address for alternative contact" in {
    val formData = baseFormData.updated("contactAddressType", ContactAddressTypeAlternativeContact.name)
      .updated(errorKey.alternativeContactFullName, "11111")
      .updated(errorKey.alternativeContactPhone, "12345 12345")
      .updated(errorKey.alternativeContactEmail1, "david@test.com")
      .updated(errorKey.alternativeContactEmail2, "david@test.com")
      .updated(errorKey.alternativeContactBuildingName, "15")
      .updated(errorKey.alternativeContactPostcode, "AA11 1AA")

    val noPhone = pageTwoForm.bind(formData - errorKey.alternativeContactPhone)
    doesNotContainErrors(noPhone)

    val noEmail = pageTwoForm.bind(formData - errorKey.alternativeContactEmail1 - errorKey.alternativeContactEmail2)
    doesNotContainErrors(noEmail)

    val noAddress = pageTwoForm.bind(formData - errorKey.alternativeContactBuildingName - errorKey.alternativeContactPostcode)
    doesNotContainErrors(noAddress)

    val noPhoneOrEmail = pageTwoForm.bind(formData - errorKey.alternativeContactPhone - errorKey.alternativeContactEmail1 - errorKey.alternativeContactEmail2)
    doesNotContainErrors(noPhoneOrEmail)
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
      val email1TooLong = "contactDetails.email1.email.tooLong"
      val email2TooLong = "contactDetails.email2.email.tooLong"
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

    val tooLongEmail = "email_too_long_for_validation_againt_business_rules_specify_but_DB_constraints@something.co.uk"
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
