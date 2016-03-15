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

import models.pages.PageFive
import models.serviceContracts.submissions.LandlordConnectionTypeOther
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional}
import uk.gov.voa.play.form.ConditionalMappings._
import ConditionalMapping.ifTrueElse


object PageFiveForm {
  import MappingSupport._

  val pageFiveForm = Form(mapping(
    "overseas" -> mandatoryBoolean,
    "landlordFullName" -> nonEmptyText(maxLength = 50),
    "landlordAddress" -> ifTrueElse("overseas", optional(addressAbroadMapping("landLordAddress")), optional(addressMapping("landlordAddress"))),
    "landlordConnectType" -> landlordConnectionType,
    "landlordConnectText" -> mandatoryIfEqual( 
      "landlordConnectType", LandlordConnectionTypeOther.name, nonEmptyText(maxLength = 100)
    )
  )(PageFive.apply)(PageFive.unapply))
}
