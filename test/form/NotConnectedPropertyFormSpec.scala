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

import models.serviceContracts.submissions.NotConnected
import org.scalatest.OptionValues._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class NotConnectedPropertyFormSpec extends AnyFlatSpec with should.Matchers {

  val baseDate: Map[String, String] = Map(
    "fullName"              -> "John Doe",
    "email"                 -> "john@example.com",
    "phoneNumber"           -> "078333232211",
    "additionalInformation" -> "Some additional information"
  )

  "Form mapping" should "map form with all values" in {

    val formWithData = NotConnectedPropertyForm.form.bind(baseDate)

    formWithData.errors shouldBe empty

    formWithData.value       shouldBe defined
    formWithData.value.value shouldBe
      NotConnected(
        "John Doe",
        Some("john@example.com"),
        Some("078333232211"),
        Some("Some additional information")
      )
  }

  it should "map form without email" in {

    val formWithData = NotConnectedPropertyForm.form.bind(baseDate - "email")

    formWithData.errors shouldBe empty

    formWithData.value       shouldBe defined
    formWithData.value.value shouldBe
      NotConnected(
        "John Doe",
        None,
        Some("078333232211"),
        Some("Some additional information")
      )
  }

  it should "map form without phoneNumber" in {

    val formWithData = NotConnectedPropertyForm.form.bind(baseDate - "phoneNumber")

    formWithData.errors shouldBe empty

    formWithData.value       shouldBe defined
    formWithData.value.value shouldBe
      NotConnected(
        "John Doe",
        Some("john@example.com"),
        None,
        Some("Some additional information")
      )
  }

  it should "map form without additional information" in {

    val formWithData = NotConnectedPropertyForm.form.bind(baseDate - "additionalInformation")

    formWithData.errors shouldBe empty

    formWithData.value       shouldBe defined
    formWithData.value.value shouldBe
      NotConnected(
        "John Doe",
        Some("john@example.com"),
        Some("078333232211"),
        None
      )
  }

  it should "fail mapping without phoneNumber and email" in {
    val formWithData = NotConnectedPropertyForm.form.bind(baseDate - "phoneNumber" - "email")

    formWithData.value shouldBe None

    formWithData.errors should have size 2

  }

}
