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
    mustContainError(errorKey.email1, Errors.contactEmailRequired, form)
    mustContainError(errorKey.phone, Errors.contactPhoneRequired, form)
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

  it should "error if invalid userType is provided" in {
    val formData = baseFormData.updated("userType", "owner1")
    val form = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
  }

  it should "error if email adress is longer than 50 characters" in {
    submissions.ContactTypes.all.filter(_ != ContactTypePhone).foreach { ct =>
      val formData: Map[String, String] = baseFormData
        .updated("contactType", ct.name)
        .updated("contactDetails.email1", tooLongEmail)
        .updated("contactDetails.email2", tooLongEmail)
      val form = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

      mustContainError(errorKey.email1, errorKey.email1TooLong, form)
    }
  }

  it should "validate the phone number when the preferred contact method is phone" in {
    validateOptionalPhone(pageTwoForm, baseFormData, "contactDetails")

    val form = pageTwoForm.bind(baseFormData - errorKey.phone).convertGlobalToFieldErrors()
    mustContainError(errorKey.phone, Errors.contactPhoneRequired, form)
  }

  it should "validate full name" in {
    validateLettersNumsSpecCharsUptoLength(errorKey.fullName, 50, pageTwoForm, baseFormData)
  }

  object TestData {
    val errorKey = new {
      val fullName: String = "fullName"
      val userType: String = "userType"
      val phone = "contactDetails.phone"
      val email1 = "contactDetails.email1"
      val email1TooLong = "contactDetails.email1.email.tooLong"
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
      "contactDetails.phone" -> "01234 123123",
      "contactDetails.email1" -> "blah.blah@test.com",
      "fullName" -> "Mr John Smith")
  }

}
