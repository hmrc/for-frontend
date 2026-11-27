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

import form.PageEightForm.pageEightForm
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part8Spec extends QuestionViewBehaviours[RentAgreement]:

  private def page8 = inject[views.html.part8]

  private val messageKeyPrefix = "section8"

  val form: Form[RentAgreement] = pageEightForm

  private def createView = () => page8(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[RentAgreement]) => page8(form, completeFullPathJourney)(using getRequest, messages)

  "Page eight view" should {

    behave like normalPage(createView, messageKeyPrefix)

    // Was the rent fixed by tenant or landlord
    "contains question was the rent fixed by tenant or landlord" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.wasRentFixedBetween"))

      assertContainsRadioButton(doc, "wasRentFixedBetween", "wasRentFixedBetween", "true", false)
      assertContainsText(doc, messages("label.yes.do"))

      assertContainsRadioButton(doc, "wasRentFixedBetween-2", "wasRentFixedBetween", "false", false)
      assertContainsText(doc, messages("label.no.naddo"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("wasRentFixedBetween", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Was the rent fixed by
    "contains question was the rent fixed by" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.notReviewRentFixed"))

      assertContainsRadioButton(
        doc,
        "notReviewRentFixed",
        "notReviewRentFixed",
        NotReviewRentFixedType.values(0).toString,
        false
      )
      assertContainsText(doc, messages("notReviewRentFixedTypes.interim").replace("&", "&amp;"))

      assertContainsRadioButton(
        doc,
        "notReviewRentFixed-2",
        "notReviewRentFixed",
        NotReviewRentFixedType.values(1).toString,
        false
      )
      assertContainsText(doc, messages("notReviewRentFixedTypes.finalJudge"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("notReviewRentFixed", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Is this rent
    "contain radio buttons for is this rent a new lease" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.rentSetByType"))

      assertContainsRadioButton(doc, "rentSetByType", "rentSetByType", RentSetByType.values(0).toString, false)
      assertContainsText(doc, messages("rentSetByTypes.newLease"))

      assertContainsRadioButton(doc, "rentSetByType-2", "rentSetByType", RentSetByType.values(1).toString, false)
      assertContainsText(doc, messages("rentSetByTypes.renewedLease"))

      assertContainsRadioButton(doc, "rentSetByType-3", "rentSetByType", RentSetByType.values(2).toString, false)
      assertContainsText(doc, messages("rentSetByTypes.leaseback"))

      assertContainsRadioButton(doc, "rentSetByType-4", "rentSetByType", RentSetByType.values(3).toString, false)
      assertContainsText(doc, messages("rentSetByTypes.surrender"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("rentSetByType", "error"))))
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
