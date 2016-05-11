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

    mustContainError(errorKey.userType, Errors.noValueSelected, form)
  }

  it should "error if invalid contactType is provided" in {
    val formData = baseFormData.updated("contactType", "phone1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.contactType, Errors.noValueSelected, form)
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

  object TestData {
    val errorKey = new {
      val fullName: String = "fullName"
      val userType: String = "userType"
      val contactType: String = "contactType"
      val contactAddressType: String = "contactAddressType"
      val phone = "contactDetails.phone"
      val email1 = "contactDetails.email1"
      val email2 = "contactDetails.email2"
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
