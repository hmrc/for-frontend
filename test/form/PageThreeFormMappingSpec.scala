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

import form.PageThreeForm._
import models._
import models.pages.PageThree
import models.serviceContracts.submissions.{OccupierTypeCompany, OccupierTypeIndividuals}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import utils.FormBindingTestAssertions._
import utils.MappingSpecs._

class PageThreeFormMappingSpec extends AnyFlatSpec with should.Matchers {

  import TestData._

  "A fully populated form " should "bind to PageThreeData" in {
    mustBind(bind(formData1)) { x => assert(x === data1) }
  }
  "If occupier type is Company and no company name is supplied then" should "error" in {
    val dataMap = formData1.updated(keys.occupierType, OccupierTypeCompany.name) - keys.occupierCompanyName
    val bound = bind(dataMap).convertGlobalToFieldErrors()

    mustContainError(keys.occupierCompanyName, "error.companyName.required", bound)
  }

  "If occupier type is Company and no first occupation date is supplied the" should "error" in {
    val dataMap = formData1.updated(keys.occupierType, OccupierTypeCompany.name) - keys.firstOccupationDateMonth - keys.firstOccupationDateYear
    val form = bind(dataMap)

    mustContainError(keys.firstOccupationDateMonth, "error.month.required", form)
    mustContainError(keys.firstOccupationDateYear, "error.year.required",form)
    form.errors.size should be(2)
  }

  "If occupier type is Individual and no first occupation date is supplied then" should "error" in {
    val dataMap = formData1.updated(keys.occupierType, OccupierTypeIndividuals.name) - keys.firstOccupationDateMonth - keys.firstOccupationDateYear
    val form = bind(dataMap)

    mustContainError(keys.firstOccupationDateMonth, "error.month.required", form)
    mustContainError(keys.firstOccupationDateYear, "error.year.required",form)
  }

  "Page Three mapping" should "allow up to 100 letters, numbers, spaces, and special characters for 'Other' property type details" in {
    validateLettersNumsSpecCharsUptoLength(keys.propertyType, 100, pageThreeForm, formData1, Some("error.propertyType.maxLength"))
  }

  it should "validate the first occupation date when the occupier type is individuals" in {
    val formData = formData1.updated(keys.occupierType, OccupierTypeIndividuals.name).updated(keys.mainOccupierName, "Jimmy Choo")
    validatePastDate("firstOccupationDate", pageThreeForm, formData)
  }

  it should "validate the first occupation date when the occupier type is company" in {
    val formData = formData1.updated(keys.occupierType, OccupierTypeCompany.name)
    validatePastDate("firstOccupationDate", pageThreeForm, formData)
  }

  it should "allow up to 50 letters, numbers, spaces, and special characters for Company name" in {
    validateLettersNumsSpecCharsUptoLength(keys.occupierCompanyName, 50, pageThreeForm, formData1, Some("error.companyName.maxLength"))
  }

  it should "allow up to 50 letters, numbers, spaces, and special characters for Company contact" in {
    validateLettersNumsSpecCharsUptoLength(keys.occupierCompanyContact, 50, pageThreeForm, formData1)
  }

  it should "require an answer to property is rented by you, when specifying property not owned by you" in {
    val data = formData1.updated(keys.propertyOwnedByYou, "false") - keys.propertyRentedByYou
    val form = bind(data)

    mustContainError(keys.propertyRentedByYou, Errors.propertyRentedByYouRequired, form)

  }

  it should "require a main contact name when occupier type is one or more individuals" in {
    val data = formData1.updated(keys.occupierType, OccupierTypeIndividuals.name) - keys.mainOccupierName
    val form = bind(data)

    mustContainError(keys.mainOccupierName, "error.occupiersName.required", form)
  }

  it should "allow upto 50 chars as a main occupier name" in {
    val data = formData1.updated(keys.occupierType, OccupierTypeIndividuals.name) - keys.mainOccupierName

    validateLettersNumsSpecCharsUptoLength(keys.mainOccupierName, 50, pageThreeForm, data, Some("error.occupiersName.maxLength"))
  }

  it should "ignore leading and trailling whitespace in date fields" in {
    val data = formData1.updated(keys.firstOccupationDateMonth, " 3 ")
                        .updated(keys.firstOccupationDateYear, " 2011 ")
    mustBind(bind(data)) { _ => () }
  }

  object TestData {
    val keys = new {
      val occupierCompanyName = "occupierCompanyName"
      val occupierCompanyContact = "occupierCompanyContact"
      val occupierType = "occupierType"
      val otherPropertyType = "otherPropertyType"
      val propertyOwnedByYou = "propertyOwnedByYou"
      val propertyRentedByYou = "propertyRentedByYou"
      val propertyType = "propertyType"
      val firstOccupationDateMonth = "firstOccupationDate.month"
      val firstOccupationDateYear = "firstOccupationDate.year"
      val mainOccupierName = "mainOccupierName"
      val noRentDetails = "noRentDetails"
    }

    def bind(dataMap: Map[String, String]) = {
      val bound = pageThreeForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    val formData1 = Map(
      keys.occupierCompanyName -> "Some Company",
      keys.occupierCompanyContact -> "Some Company Contact",
      keys.occupierType -> OccupierTypeCompany.name,
      keys.otherPropertyType -> "other property type",
      keys.propertyOwnedByYou -> "false",
      keys.propertyRentedByYou -> "true",
      keys.propertyType -> "Stud farm",
      keys.firstOccupationDateMonth -> "2",
      keys.firstOccupationDateYear -> "2015")

    val data1 = PageThree(
      propertyType = "Stud farm",
      occupierType = OccupierTypeCompany,
      occupierCompanyName = Some("Some Company"),
      occupierCompanyContact = Some("Some Company Contact"),
      firstOccupationDate = Some(RoughDate(None, Some(2), 2015)),
      None,
      propertyOwnedByYou = false,
      propertyRentedByYou = Some(true),
      noRentDetails = None)
  }

}
