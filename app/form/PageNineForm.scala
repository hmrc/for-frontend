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

import models.pages.PageNine
import models.serviceContracts.submissions.{RentBaseTypeOther, RentBaseTypePercentageOpenMarket, RentBaseTypePercentageTurnover}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import uk.gov.voa.play.form.ConditionalMappings._
import DateMappings._
import MappingSupport._

object PageNineForm {

  val pageNineMaping = mapping(
    "totalRent" -> annualRent,
    "rentBecomePayable" -> dateFieldsMapping("rentBecomePayable"),
    "rentActuallyAgreed" -> dateFieldsMapping("rentActuallyAgreed"),
    "negotiatingNewRent" -> mandatoryBooleanWithError(Errors.negotiatingNewRentRequired),
    "rentBasedOn" -> rentBaseTypeMapping,
    "rentBasedOnDetails" -> mandatoryAndOnlyIfAnyOf("rentBasedOn",
      Seq(RentBaseTypePercentageOpenMarket.name, RentBaseTypePercentageTurnover.name, RentBaseTypeOther.name),
      nonEmptyText(maxLength = 250))
  )(PageNine.apply)(PageNine.unapply)

  val pageNineForm = Form(pageNineMaping)
}
