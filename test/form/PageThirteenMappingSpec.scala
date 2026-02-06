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

import form.PageThirteenForm.pageThirteenForm
import models.*
import models.serviceContracts.submissions.{AlterationSetByTypeAddLift, AlterationSetByTypeExtension, PropertyAlterations, PropertyAlterationsDetails}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.data.Form
import utils.FormBindingTestAssertions.*
import utils.MappingSpecs.*

class PageThirteenMappingSpec extends AnyFlatSpec with should.Matchers {

  import TestData._

  "Page thirteen form" should "successfully bind to a full complement of valid form data" in {
    val form = bind(baseData)

    doesNotContainErrors(form)
  }

  it should "successfully bind to a full complement of valid just being no improvements or alterations" in {
    val testData = Map(keys.propertyAlterations -> "false")
    val form     = bind(testData)

    doesNotContainErrors(form)
  }

  it should "return a required error when no value for whether work was required is entered" in {
    val testData = baseData - keys.alterationsRequired
    val form     = bind(testData)

    mustContainError(keys.alterationsRequired, Errors.tenantWasRequiredToMakeAlterationsRequired, form)

  }

  it should "not throw errors if no improvements are selected, and the other fields exist" in {
    val testData = baseData.updated(keys.propertyAlterations, "false")
      .updated(indexedKey(0).alterationDetailsAlterationType, "")
      .updated(indexedKey(0).alterationDetailsDateMonth, "")
      .updated(indexedKey(0).alterationDetailsDateYear, "")
      .updated(indexedKey(0).alterationDetailsCost, "")
    val form     = bind(testData)

    doesNotContainErrors(form)
  }

  it should "return a required error when no value for alterations and improvements is selected in the form data" in {
    val testData: Map[String, String] = Map.empty
    val form                          = bind(testData)

    mustContainError(keys.propertyAlterations, Errors.hasTenantDonePropertyAlterationsRequired, form)

  }

  it should "return an error and not bind when no value for any of the details for the alterations or improvements is input" in {
    val testData = Map("propertyAlterations" -> "true")
    val form     = bind(testData)

    mustContainError(keys.alterationsRequired, Errors.tenantWasRequiredToMakeAlterationsRequired, form)

  }

  it should "return an error and not bind when no value for the cost of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsCost
    val form     = bind(testData)

    mustContainError(indexedKey(0).alterationDetailsCost, "error.required.alternationCost", form)
  }

  it should "return an error and not bind when no value for the alterationType of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsAlterationType
    val form     = bind(testData)

    mustContainError(indexedKey(0).alterationDetailsAlterationType, Errors.whichWorksWereDoneRequired, form)
  }

  it should "return an error and not bind when no value for the date of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsDateMonth - indexedKey(0).alterationDetailsDateYear
    val form     = bind(testData)

    mustContainError(indexedKey(0).alterationDetailsDateYear, "error.alternationCost.year.required", form)
  }

  it should "return an error and not bind when multiple alterations are entered and one is missing the alterationType" in {
    val testData = baseData ++
      Map(
        indexedKey(1).alterationDetailsAlterationType -> "",
        indexedKey(1).alterationDetailsCost           -> "15.50",
        indexedKey(1).alterationDetailsDateMonth      -> "7",
        indexedKey(1).alterationDetailsDateYear       -> "1912"
      )
    val form     = bind(testData)

    mustContainError(indexedKey(1).alterationDetailsAlterationType, Errors.whichWorksWereDoneRequired, form)
  }

  it should "allow upto 10 alterations" in {
    mustBind(bind(addAlterations(9, baseData)))(x => assert(x === with10Alterations))

    val form = bind(addAlterations(10, baseData))
    mustContainError("propertyAlterationsDetails", Errors.tooManyAlterations, form)
  }

  it should "validate the date of the first property alteration" in
    validatePastDate("propertyAlterationsDetails[0].date", pageThirteenForm, baseData, ".alternationCost")

  it should "validate the cost of the first property alteration" in
    validateCurrency("propertyAlterationsDetails[0].cost", pageThirteenForm, baseData, ".alternationCost")

  it should "validate the date of the second property alteration" in
    validatePastDate("propertyAlterationsDetails[1].date", pageThirteenForm, dataWithSecondAlteration, ".alternationCost")

  it should "validate the cost of the second property alteration" in
    validateCurrency(indexedKey(1).alterationDetailsCost, pageThirteenForm, dataWithSecondAlteration, ".alternationCost")

  object TestData {

    val keys: Keys = new Keys

    class Keys {
      val propertyAlterations        = "propertyAlterations"
      val propertyAlterationsDetails = "propertyAlterationsDetails"
      val alterationsRequired        = "requiredAnyWorks"
    }

    def bind(data: Map[String, String]): Form[PropertyAlterations] = pageThirteenForm.bind(data).convertGlobalToFieldErrors()

    def indexedKey(idx: Int): IndexedKey = new IndexedKey(idx)

    class IndexedKey(idx: Int) {
      val alterationDetailsAlterationType: String = s"propertyAlterationsDetails[$idx].alterationType"
      val alterationDetailsCost: String           = s"propertyAlterationsDetails[$idx].cost"
      val alterationDetailsDateMonth: String      = s"propertyAlterationsDetails[$idx].date.month"
      val alterationDetailsDateYear: String       = s"propertyAlterationsDetails[$idx].date.year"
    }

    val baseData: Map[String, String] = Map(
      keys.propertyAlterations                      -> "true",
      indexedKey(0).alterationDetailsAlterationType -> "extension",
      indexedKey(0).alterationDetailsCost           -> "5.50",
      indexedKey(0).alterationDetailsDateMonth      -> "6",
      indexedKey(0).alterationDetailsDateYear       -> "1902",
      keys.alterationsRequired                      -> "false"
    )

    val dataWithSecondAlteration: Map[String, String] = baseData.updated(indexedKey(1).alterationDetailsAlterationType, "extension").updated(
      indexedKey(1).alterationDetailsCost,
      "100"
    ).updated(indexedKey(1).alterationDetailsDateMonth, "1").updated(indexedKey(1).alterationDetailsDateYear, "2015")
  }

  def addAlterations(n: Int, data: Map[String, String]): Map[String, String] = (1 to n).foldLeft(data) { (s, v) =>
    s.updated(s"propertyAlterationsDetails[$v].alterationType", "addLift")
      .updated(s"propertyAlterationsDetails[$v].cost", "25")
      .updated(s"propertyAlterationsDetails[$v].date.month", "05")
      .updated(s"propertyAlterationsDetails[$v].date.year", "2011")
  }

  val with10Alterations: PropertyAlterations = PropertyAlterations(
    true,
    List(
      PropertyAlterationsDetails(RoughDate(None, Some(6), 1902), AlterationSetByTypeExtension, 5.5),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25),
      PropertyAlterationsDetails(RoughDate(None, Some(5), 2011), AlterationSetByTypeAddLift, 25)
    ),
    Some(false)
  )
}
