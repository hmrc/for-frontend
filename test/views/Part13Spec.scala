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

import form.PageThirteenForm.pageThirteenForm
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import util.DateExampleUtil
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part13Spec extends QuestionViewBehaviours[PropertyAlterations]:

  private def page13 = inject[views.html.part13]

  private val messageKeyPrefix = "section13"

  val form: Form[PropertyAlterations] = pageThirteenForm

  private def createView = () => page13(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PropertyAlterations]) => page13(form, completeFullPathJourney)(using getRequest, messages)

  "Page thirteen view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "propertyAlterationsDetails[0].cost")

    // Are business rates included in the rent
    "contains question are business rates included in the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.propertyAlterations"))
      assertContainsText(doc, messages("hint.propertyAlterations"))

      assertContainsRadioButton(doc, "propertyAlterations", "propertyAlterations", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "propertyAlterations-2", "propertyAlterations", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("propertyAlterations", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // What works were carried out
    "contains question what works were carried out" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("alteration.section.heading") + " <span>1</span>")
      assertContainsText(doc, messages("label.propertyAlterationsDetails.alterationType"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(0).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.extension"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-2",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(1).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.demolition"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-3",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(2).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.addedMezzanineFloor"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-4",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(3).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.removedWall"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-5",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(4).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.addLift"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-6",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(5).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.addedParking"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-7",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(6).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.addedAirCondition"))

      assertContainsRadioButton(
        doc,
        "propertyAlterationsDetails[0].alterationType-8",
        "propertyAlterationsDetails[0].alterationType",
        AlterationSetByType.values(7).toString,
        false
      )
      assertContainsText(doc, messages("alterationType.fittingOut"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("propertyAlterationsDetails[0].alterationType", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Approximate date of works
    "contains question approximate date of works" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.propertyAlterationsDetails"))
      assertContainsText(doc, messages("help.date.month.year", DateExampleUtil.exampleMonthYear(1)))

      assertRenderedById(doc, "propertyAlterationsDetails[0].date.month")
      assertContainsText(doc, "Month")

      assertRenderedById(doc, "propertyAlterationsDetails[0].date.year")
      assertContainsText(doc, "Year")

      val errorDocDay = asDocument(createViewUsingForm(form.withError(FormError("propertyAlterationsDetails[0].date.month", "error"))))
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-message")

      val errorDocMonth = asDocument(createViewUsingForm(form.withError(FormError("propertyAlterationsDetails[0].date.year", "error"))))
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-message")
    }

    // Cost of work(excluding VAT)
    "contains question cost of work(excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("propertyAlterationsDetails.cost.label"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("propertyAlterationsDetails[0].cost", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Links appear on the page
    "contains the remove and add another links" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.add.remove"))

      assertContainsText(doc, messages("label.add.propertyAlterations"))
    }

    // Was the tenant required to carry out any works as a condition of the current lease or agreement
    "contains question was the tenant required to carry out any works as a condition of the current lease or agreement" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.requiredAnyWorks"))

      assertContainsRadioButton(doc, "requiredAnyWorks", "requiredAnyWorks", "true", false)
      assertContainsText(doc, messages("label.yes.oedd"))

      assertContainsRadioButton(doc, "requiredAnyWorks-2", "requiredAnyWorks", "false", false)
      assertContainsText(doc, messages("label.no.nac.oedd"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("requiredAnyWorks", "error"))))
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
