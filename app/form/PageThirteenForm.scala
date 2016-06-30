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

import models.serviceContracts.submissions.{PropertyAlterations, PropertyAlterationsDetails}
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._

import DateMappings._
import MappingSupport._

object PageThirteenForm {

  val pageThirteenForm = Form(mapping(
    keys.propertyAlterations -> mandatoryBoolean,
    keys.propertyAlterationsDetails -> onlyIfTrue(
      keys.propertyAlterations,
      IndexedMapping("propertyAlterationsDetails", propertyAlterationsDetailsMapping).verifying(Errors.tooManyAlterations, _.length <= 10)
    ),
    keys.alterationsRequired -> mandatoryBooleanIfTrue(keys.propertyAlterations, mandatoryBoolean)
  ) (PropertyAlterations.apply)(PropertyAlterations.unapply))

  lazy val propertyAlterationsDetailsMapping = (indexed: String) => mapping(
    s"$indexed.date" -> monthYearRoughDateMapping(s"$indexed.date"),
    s"$indexed.description" -> nonEmptyText(maxLength = 250),
    s"$indexed.cost" -> currencyOver10Million

  )(PropertyAlterationsDetails.apply)(PropertyAlterationsDetails.unapply)

  lazy val keys = new {
    val propertyAlterations = "propertyAlterations"
    val propertyAlterationsDetails = "propertyAlterationsDetails"
    val alterationDetailsDescription = "propertyAlterationsDetails.description"
    val alterationDetailsCost = "propertyAlterationsDetails.cost"
    val alterationDetailsDateMonth = "propertyAlterationsDetails.date.month"
    val alterationDetailsDateYear = "propertyAlterationsDetails.date.year"
    val alterationsRequired = "requiredAnyWorks"
  }
}