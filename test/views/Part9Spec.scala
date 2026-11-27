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

import form.PageNineForm.pageNineForm
import models.pages.PageNine
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import util.DateExampleUtil
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part9Spec extends QuestionViewBehaviours[PageNine]:

  private def page9 = inject[views.html.part9]

  private val messageKeyPrefix = "section9"

  val form: Form[PageNine] = pageNineForm

  private def createView = () => page9(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageNine]) => page9(form, completeFullPathJourney)(using getRequest, messages)

  "Page nine view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "totalRent.annualRentExcludingVat", "rentBasedOnDetails")

    // What is the current annual rent
    "contains question what is the current annual rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("totalRent.annualRentExcludingVat.label"))
      assertContainsText(doc, messages("totalRent.annualRentExcludingVat.hint"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("totalRent.annualRentExcludingVat", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // When did the tenant start paying the current rent
    "contains question when did the tenant start paying the current rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentBecomePayable"))
      assertContainsText(doc, messages("help.date.day.month.year", DateExampleUtil.exampleDayMonthYear(1)))

      assertRenderedById(doc, "rentBecomePayable.day")
      assertContainsText(doc, "Day")

      assertRenderedById(doc, "rentBecomePayable.month")
      assertContainsText(doc, "Month")

      assertRenderedById(doc, "rentBecomePayable.year")
      assertContainsText(doc, "Year")

      val errorDocDay = asDocument(createViewUsingForm(form.withError(FormError("rentBecomePayable.day", "error"))))
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-message")

      val errorDocMonth = asDocument(createViewUsingForm(form.withError(FormError("rentBecomePayable.month", "error"))))
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-message")

      val errorDocYear = asDocument(createViewUsingForm(form.withError(FormError("rentBecomePayable.year", "error"))))
      assertRenderedByCssSelector(errorDocYear, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocYear, ".govuk-error-message")
    }

    // When was the rent amount agreed or set
    "contains question when was the rent amount agreed or set" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentActuallyAgreed"))
      assertContainsText(doc, messages("help.date.day.month.year", DateExampleUtil.exampleDayMonthYear(1)))

      assertRenderedById(doc, "rentActuallyAgreed.day")
      assertContainsText(doc, "Day")

      assertRenderedById(doc, "rentActuallyAgreed.month")
      assertContainsText(doc, "Month")

      assertRenderedById(doc, "rentActuallyAgreed.year")
      assertContainsText(doc, "Year")

      val errorDocDay = asDocument(createViewUsingForm(form.withError(FormError("rentActuallyAgreed.day", "error"))))
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocDay, ".govuk-error-message")

      val errorDocMonth = asDocument(createViewUsingForm(form.withError(FormError("rentActuallyAgreed.month", "error"))))
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocMonth, ".govuk-error-message")

      val errorDocYear = asDocument(createViewUsingForm(form.withError(FormError("rentActuallyAgreed.year", "error"))))
      assertRenderedByCssSelector(errorDocYear, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDocYear, ".govuk-error-message")
    }

    // Is the tenant (or their agent) negotiating a new rent
    "contains question is the tenant (or their agent) negotiating a new rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.negotiatingNewRent"))
      assertContainsText(doc, messages("hint.negotiatingNewRent"))

      assertContainsRadioButton(doc, "negotiatingNewRent", "negotiatingNewRent", "true", false)
      assertContainsText(doc, messages("label.yes.ydw"))

      assertContainsRadioButton(doc, "negotiatingNewRent-2", "negotiatingNewRent", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydw"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("negotiatingNewRent", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // What is the rent based on
    "contains question is the tenant or their agent negotiating a new rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentBasedOn"))

      assertContainsRadioButton(doc, "rentBasedOn", "rentBasedOn", RentBaseType.values(0).toString, false)
      assertContainsText(doc, messages("rentBaseTypes.openMarket"))
      assertContainsText(doc, messages("rentBasedOn.openMarket.hint"))

      assertContainsRadioButton(doc, "rentBasedOn-2", "rentBasedOn", RentBaseType.values(1).toString, false)
      assertContainsText(doc, messages("rentBaseTypes.percentageOpenMarket"))
      assertContainsText(doc, messages("rentBasedOn.percentageOpenMarket.hint"))

      assertContainsRadioButton(doc, "rentBasedOn-3", "rentBasedOn", RentBaseType.values(2).toString, false)
      assertContainsText(doc, messages("rentBaseTypes.percentageTurnover"))
      assertContainsText(doc, messages("rentBasedOn.percentageTurnover.hint"))

      assertContainsRadioButton(doc, "rentBasedOn-4", "rentBasedOn", RentBaseType.values(3).toString, false)
      assertContainsText(doc, messages("rentBaseTypes.indexation"))
      assertContainsText(doc, messages("rentBasedOn.indexation.hint"))

      assertContainsRadioButton(doc, "rentBasedOn-5", "rentBasedOn", RentBaseType.values(4).toString, false)
      assertContainsText(doc, messages("rentBaseTypes.other"))
      assertContainsText(doc, messages("rentBasedOn.other.hint"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("rentBasedOn", "error"))))
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
