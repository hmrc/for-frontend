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

import models.serviceContracts.submissions.OtherFactors
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._

import MappingSupport._

object PageFourteenForm {
  val pageFourteenForm = Form(mapping(
    "anyOtherFactors" -> mandatoryBooleanWithError(Errors.anyOtherFactorsRequired),
    "anyOtherFactorsDetails" -> mandatoryIfTrue("anyOtherFactors",nonEmptyText(maxLength = 124))
  )(OtherFactors.apply)(OtherFactors.unapply))
}
