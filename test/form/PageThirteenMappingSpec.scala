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
import org.scalatest.{FlatSpec, Matchers}
import PageThirteenForm.pageThirteenForm
import utils.FormBindingTestAssertions._
import utils.MappingSpecs._
import models._

class PageThirteenMappingSpec extends FlatSpec with Matchers {

  import TestData._

  "Page thirteen form" should "successfully bind to a full complement of valid form data" in {
    val form = bind(baseData)

    doesNotContainErrors(form)
  }

  it should "successfully bind to a full complement of valid just being no improvements or alterations" in {
    val testData = Map(keys.propertyAlterations -> "false")
    val form = bind(testData)

    doesNotContainErrors(form)
  }

  it should "return a boolean missing error when no value for whether work was required is entered" in {
    val testData = baseData - keys.alterationsRequired
    val form = bind(testData)

    mustContainBooleanRequiredErrorFor(keys.alterationsRequired, form)
  }

  it should "not throw errors if no improvements are selected, and the other fields exist" in {
    val testData = baseData.updated(keys.propertyAlterations, "false")
      .updated(indexedKey(0).alterationDetailsDescription, "")
      .updated(indexedKey(0).alterationDetailsDateMonth, "")
      .updated(indexedKey(0).alterationDetailsDateYear, "")
      .updated(indexedKey(0).alterationDetailsCost, "")
    val form = bind(testData)

    doesNotContainErrors(form)
  }

  it should "return a boolean missing error when no value for alterations and improvements is selected in the form data" in {
    val testData: Map[String, String] = Map.empty
    val form = bind(testData)

    mustContainBooleanRequiredErrorFor(keys.propertyAlterations, form)
  }

  it should "return an error and not bind when no value for any of the details for the alterations or improvements is input" in {
    val testData = Map("propertyAlterations" -> "true")
    val form = bind(testData)

    mustContainBooleanRequiredErrorFor(keys.alterationsRequired, form)
  }

  it should "return an error and not bind when no value for the cost of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsCost
    val form = bind(testData)

    mustContainRequiredErrorFor(indexedKey(0).alterationDetailsCost, form)
  }

  it should "return an error and not bind when no value for the description of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsDescription
    val form = bind(testData)

    mustContainRequiredErrorFor(indexedKey(0).alterationDetailsDescription, form)
  }

  it should "return an error and not bind when no value for the date of alterations or improvements is input" in {
    val testData = baseData - indexedKey(0).alterationDetailsDateMonth - indexedKey(0).alterationDetailsDateYear
    val form = bind(testData)

    mustContainRequiredErrorFor(indexedKey(0).alterationDetailsDateYear, form)
  }

  it should "return an error and not bind when multiple alterations are entered and one is missing the description" in {
    val testData = baseData ++
      Map(
        indexedKey(1).alterationDetailsDescription -> "",
        indexedKey(1).alterationDetailsCost -> "15.50",
        indexedKey(1).alterationDetailsDateMonth -> "7",
        indexedKey(1).alterationDetailsDateYear -> "1912"
      )
    val form = bind(testData)

    mustContainRequiredErrorFor(indexedKey(1).alterationDetailsDescription, form)
  }

  it should "allow upto 10 alterations" in {
    mustBind(bind(addAlterations(9, baseData))) { x => assert(x === with10Alterations) }

    val form = bind(addAlterations(10, baseData))
    mustContainError("propertyAlterationsDetails", Errors.tooManyAlterations, form)
  }

  it should "validate the date of the first property alteration" in {
    validatePastDate("propertyAlterationsDetails[0].date", pageThirteenForm, baseData)
  }

  it should "validate the details of the first property alteration" in {
    validateLettersNumsSpecCharsUptoLength("propertyAlterationsDetails[0].description", 250, pageThirteenForm, baseData)
  }

  it should "validate the cost of the first property alteration" in {
    validateCurrency("propertyAlterationsDetails[0].cost", pageThirteenForm, baseData)
  }

  it should "validate the date of the second property alteration" in {
    validatePastDate("propertyAlterationsDetails[1].date", pageThirteenForm, dataWithSecondAlteration)
  }

  it should "validate the details of the second property alteration" in {
    validateLettersNumsSpecCharsUptoLength(indexedKey(1).alterationDetailsDescription, 250, pageThirteenForm, dataWithSecondAlteration)
  }

  it should "validate the cost of the second property alteration" in {
    validateCurrency(indexedKey(1).alterationDetailsCost, pageThirteenForm, dataWithSecondAlteration)
  }

  object TestData {
    val keys = new {
    val propertyAlterations = "propertyAlterations"
    val propertyAlterationsDetails = "propertyAlterationsDetails"
    val alterationsRequired = "requiredAnyWorks"
  }

  def bind(data: Map[String, String]) = pageThirteenForm.bind(data).convertGlobalToFieldErrors()

  def indexedKey(idx: Int) = new {
    val alterationDetailsDescription = s"propertyAlterationsDetails[$idx].description"
    val alterationDetailsCost = s"propertyAlterationsDetails[$idx].cost"
    val alterationDetailsDateMonth = s"propertyAlterationsDetails[$idx].date.month"
    val alterationDetailsDateYear = s"propertyAlterationsDetails[$idx].date.year"
  }

  val baseData = Map(keys.propertyAlterations -> "true",
    indexedKey(0).alterationDetailsDescription -> "extension",
    indexedKey(0).alterationDetailsCost -> "5.50",
    indexedKey(0).alterationDetailsDateMonth -> "6",
    indexedKey(0).alterationDetailsDateYear -> "1902",
    keys.alterationsRequired -> "false"
  )

  val dataWithSecondAlteration = baseData.
    updated(indexedKey(1).alterationDetailsDescription, "dungeon").
    updated(indexedKey(1).alterationDetailsCost, "100").
    updated(indexedKey(1).alterationDetailsDateMonth, "1").
    updated(indexedKey(1).alterationDetailsDateYear, "2015")
  }

  def addAlterations(n: Int, data: Map[String, String]) = {
    (1 to n).foldLeft(data) { (s, v) =>
      s.updated(s"propertyAlterationsDetails[$v].description", "desc")
       .updated(s"propertyAlterationsDetails[$v].cost", "25")
       .updated(s"propertyAlterationsDetails[$v].date.month", "05")
       .updated(s"propertyAlterationsDetails[$v].date.year", "2011")
    }
  }

  val with10Alterations = PropertyAlterations(
    true, List(
      PropertyAlterationsDetails( RoughDate(None,  Some(6), 1902),"extension", 5.5),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25),
      PropertyAlterationsDetails(RoughDate(None,  Some(5), 2011), "desc", 25)
    ), Some(false)
  )
}