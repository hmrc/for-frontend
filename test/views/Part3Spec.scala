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

import form.PageThreeForm.pageThreeForm
import models.serviceContracts.submissions.*
import models.pages.PageThree
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part3Spec extends QuestionViewBehaviours[PageThree]:

  private def page3 = inject[views.html.part3]

  private val messageKeyPrefix = "section3"

  val form: Form[PageThree] = pageThreeForm

  private def createView = () => page3(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageThree]) => page3(form, completeFullPathJourney)(using getRequest, messages)

  "Page Three view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "propertyType", "noRentDetails", "occupierCompanyName", "occupierCompanyContact", "mainOccupierName")

    "contain radio buttons for the value one or more individuals" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "occupierType", "occupierType", OccupierType.values(0).toString, false)
      assertContainsText(doc, messages("occupierType.individuals.label"))
    }

    "contain radio buttons for the value a company" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "occupierType-2", "occupierType", OccupierType.values(1).toString, false)
      assertContainsText(doc, messages("occupierType.company.label"))
    }

    "contain radio buttons for the value nobody, the property is empty" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "occupierType-3", "occupierType", OccupierType.values(2).toString, false)
      assertContainsText(doc, messages("occupierType.nobody.label"))
    }

    "Contains an error summary for first occupied date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("firstOccupationDate.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for first occupied date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("firstOccupationDate.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for the own property yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyOwnedByYou", "propertyOwnedByYou", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for the own property no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyOwnedByYou-2", "propertyOwnedByYou", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain radio buttons for the rent property yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyRentedByYou", "propertyRentedByYou", "true", false)
      assertContainsText(doc, messages("label.yes.ydw"))
    }

    "contain radio buttons for the rent property no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyRentedByYou-2", "propertyRentedByYou", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydw"))
    }

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
