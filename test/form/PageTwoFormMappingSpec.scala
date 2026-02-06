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

package form

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.FormError
import utils.CommonSpecs

class PageTwoFormMappingSpec extends AnyFlatSpec with should.Matchers with CommonSpecs {

  import TestData._
  import form.PageTwoForm._
  import utils.FormBindingTestAssertions._

  behavior of "page two form mapping"

  "page two mapping" should "show required errors for fullName, userType, email and phone when given empty data" in {
    val formData: Map[String, String] = Map()
    val form                          = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
    mustContainError(errorKey.email1, Errors.contactEmailRequired, form)
    mustContainError(errorKey.phone, Errors.contactPhoneRequired, form)
  }

  it should "error if fullName is missing " in {
    val formData = baseFormData - errorKey.fullName
    val form     = pageTwoForm.bind(formData)

    mustContainRequiredErrorFor(errorKey.fullName, form)
  }

  it should "error if userType is missing" in {
    val formData = baseFormData - errorKey.userType
    val form     = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
  }

  it should "error if invalid userType is provided" in {
    val formData = baseFormData.updated("userType", "owner1")
    val form     = pageTwoForm.bind(formData)

    mustContainError(errorKey.userType, Errors.userTypeRequired, form)
  }

  it should "error if email adress is longer than 50 characters" in {
    val formData: Map[String, String] = baseFormData
      .updated("contactDetails.email1", tooLongEmail)
    val form                          = pageTwoForm.bind(formData).convertGlobalToFieldErrors()

    mustContainError(errorKey.email1, errorKey.email1TooLong, form)
  }

  it should "validate the phone number" in {
    val valid = Seq("012345678901", "+4412345678901", "012345 678 901", "012345-678-901", "(012345) 678 901")
    validateNoError("contactDetails.phone", valid, pageTwoForm, baseFormData)

    val form = pageTwoForm.bind(baseFormData - errorKey.phone).convertGlobalToFieldErrors()
    mustContainError(errorKey.phone, Errors.contactPhoneRequired, form)
  }

  it should "validate full name" in
    validateLettersNumsSpecCharsUptoLength(errorKey.fullName, 50, pageTwoForm, baseFormData)

  object TestData {

    val errorKey: ErrorKey = new ErrorKey

    class ErrorKey {
      val fullName: String    = "fullName"
      val userType: String    = "userType"
      val phone               = "contactDetails.phone"
      val email1              = "contactDetails.email1"
      val email1TooLong       = "contactDetails.email1.email.tooLong"
      val contactDetailsPhone = "contactDetails.phone"
    }

    val formErrors: FormErrors = new FormErrors

    class FormErrors {

      val required: Required = new Required

      class Required {
        val fullName: FormError = FormError(errorKey.fullName, Errors.required)
      }
    }

    val tooLongEmail = "email_too_long_for_validation_againt_business_rules_specify_but_DB_constraints@something.co.uk"

    val baseFormData: Map[String, String] = Map(
      "userType"              -> "owner",
      "contactDetails.phone"  -> "12345678901",
      "contactDetails.phone"  -> "01234 123123",
      "contactDetails.email1" -> "blah.blah@test.com",
      "fullName"              -> "Mr John Smith"
    )
  }

}
