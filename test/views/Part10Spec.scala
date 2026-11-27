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

package views

import form.PageTenForm.pageTenForm
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part10Spec extends QuestionViewBehaviours[WhatRentIncludes]:

  private def page10 = inject[views.html.part10]

  private val messageKeyPrefix = "section10"

  val form: Form[WhatRentIncludes] = pageTenForm

  private def createView = () => page10(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[WhatRentIncludes]) => page10(form, completeFullPathJourney)(using getRequest, messages)

  "Page ten view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      "parking.rentIncludeParkingDetails.openSpaces",
      "parking.rentIncludeParkingDetails.coveredSpaces",
      "parking.rentIncludeParkingDetails.garages",
      "parking.rentSeparateParkingDetails.openSpaces",
      "parking.rentSeparateParkingDetails.coveredSpaces",
      "parking.rentSeparateParkingDetails.garages",
      "parking.annualSeparateParking"
    )

    // Does this rent include any living accommodation
    "contains question does this rent include any living accommodation" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.livingAccommodation"))
      assertContainsText(doc, messages("hint.livingAccommodation"))

      assertContainsRadioButton(doc, "livingAccommodation", "livingAccommodation", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "livingAccommodation-2", "livingAccommodation", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("livingAccommodation", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Is the rent paid for only part of 001, GORING ROAD, GORING-BY-SEA, WORTHING, BN12 4AX
    "contains question is the rent paid for only part of 001, GORING ROAD, GORING-BY-SEA, WORTHING, BN12 4AX" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.partRent", completeFullPathJourney.address.map(_.singleLine).getOrElse("")))
      assertContainsText(doc, messages("hint.partRent"))

      assertContainsRadioButton(doc, "partRent", "partRent", "true", false)
      assertContainsText(doc, messages("label.yes.ie"))

      assertContainsRadioButton(doc, "partRent-2", "partRent", "false", false)
      assertContainsText(doc, messages("label.no.nage"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("partRent", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Does this rent include any other business property
    "contains question does this rent include any other business property" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.otherProperty"))
      assertContainsText(doc, messages("hint.otherProperty"))

      assertContainsRadioButton(doc, "otherProperty", "otherProperty", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "otherProperty-2", "otherProperty", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("otherProperty", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Is this rent based on use of the land only
    "contains question is this rent based on use of the land only" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.landOnly"))
      assertContainsText(doc, messages("hint.landOnly"))

      assertContainsRadioButton(doc, "landOnly", "landOnly", "true", false)
      assertContainsText(doc, messages("label.yes.ie"))

      assertContainsRadioButton(doc, "landOnly-2", "landOnly", "false", false)
      assertContainsText(doc, messages("label.no.nage"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("landOnly", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Is this rent based on an empty (shell) building
    "contains question is this rent based on an empty (shell) building" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.shellUnit"))
      assertContainsText(doc, messages("hint.shellUnit"))

      assertContainsRadioButton(doc, "shellUnit", "shellUnit", "true", false)
      assertContainsText(doc, messages("label.yes.ie"))

      assertContainsRadioButton(doc, "shellUnit-2", "shellUnit", "false", false)
      assertContainsText(doc, messages("label.no.nage"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("shellUnit", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Provide more details about your answer
    "contains question provide more details about your answer" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentDetails"))
      assertContainsText(doc, messages("hint.rentDetails.livingAccommodation"))
      assertContainsText(doc, messages("hint.rentDetails.partRent"))
      assertContainsText(doc, messages("hint.rentDetails.otherProperty"))
      assertContainsText(doc, messages("hint.rentDetails.landOnly"))
      assertContainsText(doc, messages("hint.rentDetails.shellUnit"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("rentDetails", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Is the use of parking spaces or garages included in this rent
    "contains question is the use of parking spaces or garages included in this rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.parking.rentIncludeParking"))
      assertContainsText(doc, messages("hint.label.parking.rentIncludeParking"))

      assertContainsRadioButton(doc, "parking.rentIncludeParking", "parking.rentIncludeParking", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "parking.rentIncludeParking-2", "parking.rentIncludeParking", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      assertContainsText(doc, messages("label.parking.rentIncludeParkingDetails"))
      assertContainsText(doc, messages("label.openSpaces"))
      assertContainsText(doc, messages("label.coveredSpaces"))
      assertContainsText(doc, messages("label.garages"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("parking.rentIncludeParking", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // does the tenant pay extra for the use of parking spaces or garages
    "contains question does the tenant pay extra for the use of parking spaces or garages" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.parking.rentSeparateParking"))

      assertContainsRadioButton(doc, "parking.rentSeparateParking", "parking.rentSeparateParking", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "parking.rentSeparateParking-2", "parking.rentSeparateParking", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      assertContainsText(doc, messages("label.parking.rentSeparateParkingDetails"))
      assertContainsText(doc, messages("label.openSpaces"))
      assertContainsText(doc, messages("label.coveredSpaces"))
      assertContainsText(doc, messages("label.garages"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("parking.rentSeparateParking", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    "contain continue and save for later buttons" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))

      val saveForLaterButton = doc.getElementById("save-button").text()
      assert(saveForLaterButton == messages("button.save.label"))
    }
  }
