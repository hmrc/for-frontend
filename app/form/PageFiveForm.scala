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

import form.MappingSupport._
import models.pages.PageFive
import models.serviceContracts.submissions.LandlordConnectionTypeOther
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{maxLength, nonEmpty}
import uk.gov.voa.play.form.ConditionalMappings._

object PageFiveForm {
  val pageFiveForm = Form(mapping(
    "landlordFullName" -> optional(
      text.verifying(maxLength(50, "error.landlordFullName.maxLength"))
    ),
    "landlordAddress" -> optional(optionalAddressMapping("landlordAddress")),
    "landlordConnectType" -> landlordConnectionType,
    "landlordConnectText" -> mandatoryIfEqual(
      "landlordConnectType", LandlordConnectionTypeOther.name, default(text, "").verifying(
        nonEmpty(errorMessage = "error.landlordConnectText.required"),
        maxLength(100, "error.landlordConnectText.maxLength")
      )
    )
  )(PageFive.apply)(PageFive.unapply))
}
