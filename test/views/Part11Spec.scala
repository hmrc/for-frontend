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

import form.PageElevenForm.pageElevenForm
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import util.DateExampleUtil
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part11Spec extends QuestionViewBehaviours[IncentivesAndPayments]:

  private def page11 = inject[views.html.part11]

  private val messageKeyPrefix = "section11"

  val form: Form[IncentivesAndPayments] = pageElevenForm

  private def createView = () => page11(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[IncentivesAndPayments]) => page11(form, completeFullPathJourney)(using getRequest, messages)

  "Page nine view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      "rentFreePeriodDetails.rentFreePeriodLength",
      "rentFreePeriodDetails.rentFreePeriodDetails",
      "capitalPaidDetails.capitalSum",
      "capitalReceivedDetails.receivedSum"
    )

    // Was there a rent-free period
    "contains question was there a rent-free period" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentFreePeriod"))

      assertContainsRadioButton(doc, "rentFreePeriod", "rentFreePeriod", "true", false)
      assertContainsText(doc, messages("label.yes.do"))

      assertContainsRadioButton(doc, "rentFreePeriod-2", "rentFreePeriod", "false", false)
      assertContainsText(doc, messages("label.no.naddo"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("rentFreePeriod", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // How long was the rent-free period
    "contains question how long was the rent-free period" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentFreePeriodDetails.rentFreePeriodLength"))
      assertContainsText(doc, messages("hint.rentFreePeriodDetails.rentFreePeriodLength"))

      assertContainsText(doc, messages("label.date.months"))
    }

    // Why was there a rent-free period
    "contains question why was there a rent-free period" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentFreePeriodDetails.rentFreePeriodDetails"))
      assertContainsText(doc, messages("hint.rentFreePeriodDetails.rentFreePeriodDetails"))
    }

    // Did the tenant pay anything to the landlord to take on the lease
    "contains question did the tenant pay anything to the landlord to take on the lease" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.payCapitalSum"))
      assertContainsText(doc, messages("hint.payCapitalSum"))

      assertContainsRadioButton(doc, "payCapitalSum", "payCapitalSum", "true", false)
      assertContainsText(doc, messages("label.yes.ydw"))

      assertContainsRadioButton(doc, "payCapitalSum-2", "payCapitalSum", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydw"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("payCapitalSum", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // amount paid (excluding VAT)
    "contains question amount paid (excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("capitalPaidDetails.capitalSum.label"))
    }

    // Date payment made
    "contains question date payment made" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.capitalPaidDetails.paymentDate"))
      assertContainsText(doc, messages("help.date.month.year", DateExampleUtil.exampleMonthYear(1)))

      assertContainsText(doc, "Month")
      assertContainsText(doc, "Year")
    }

    // Did the tenant receive any money from the landlord or previous tenant to take on the lease
    "contains question did the tenant receive any money from the landlord or previous tenant to take on the lease" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.receiveCapitalSum"))

      assertContainsRadioButton(doc, "receiveCapitalSum", "receiveCapitalSum", "true", false)
      assertContainsText(doc, messages("label.yes.do"))

      assertContainsRadioButton(doc, "receiveCapitalSum-2", "receiveCapitalSum", "false", false)
      assertContainsText(doc, messages("label.no.naddo"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("receiveCapitalSum", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // amount received (excluding VAT)
    "contains question amount received (excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("capitalReceivedDetails.receivedSum.label"))
    }

    // Date payment received
    "contains question date payment received" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.capitalReceivedDetails.paymentDate"))
      assertContainsText(doc, messages("help.date.month.year", DateExampleUtil.exampleMonthYear(1)))

      assertContainsText(doc, "Month")
      assertContainsText(doc, "Year")
    }

    "contain continue and save for later buttons" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))

      val saveForLaterButton = doc.getElementById("save-button").text()
      assert(saveForLaterButton == messages("button.save.label"))
    }

  }
