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

import form.PageFourteenForm.pageFourteenForm
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part14Spec extends QuestionViewBehaviours[OtherFactors]:

  private def page14 = inject[views.html.part14]

//  private val messageKeyPrefix = "section14"

  val form: Form[OtherFactors] = pageFourteenForm

//  private def createView = () => page14(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[OtherFactors]) => page14(form, completeFullPathJourney)(using getRequest, messages)

  "Page fourteen view" should {

    // Note: Heading not working as per the pattern. This can be revisited as part of NGR work.
//    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      "anyOtherFactorsDetails"
    )

    // Has anything else affected the rent
    "contains question has anything else affected the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.anyOtherFactors"))
      assertContainsText(doc, messages("hint.anyOtherFactorsDetails").replace("&", "&amp;"))

      assertContainsRadioButton(doc, "anyOtherFactors", "anyOtherFactors", "true", false)
      assertContainsText(doc, messages("label.yes.oes"))

      assertContainsRadioButton(doc, "anyOtherFactors-2", "anyOtherFactors", "false", false)
      assertContainsText(doc, messages("label.no.nac.oes"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("anyOtherFactors", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // What else has affected the rent
    "contains question what else has affected the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.anyOtherFactorsDetails"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("anyOtherFactorsDetails", "error"))))
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
