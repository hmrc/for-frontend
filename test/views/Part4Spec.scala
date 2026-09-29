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

import form.PageFourForm.pageFourForm
import models.pages.PageFour
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part4Spec extends QuestionViewBehaviours[PageFour]:

  private def page4 = inject[views.html.part4]

  private val messageKeyPrefix = "section4"

  val form: Form[PageFour] = pageFourForm

  private def createView = () => page4(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageFour]) => page4(form, completeFullPathJourney)(using getRequest, messages)

  "Page four view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "sublet[0].tenantFullName", "sublet[0].subletPropertyReasonDescription", "sublet[0].annualRent", "sublet[0].tenantAddress.buildingNameNumber")

    "contain radio buttons for the property sublet yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyIsSublet", "propertyIsSublet", "true", false)
      assertContainsText(doc, messages("label.yes.oes"))
    }

    "contain radio buttons for the property sublet no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "propertyIsSublet-2", "propertyIsSublet", "false", false)
      assertContainsText(doc, messages("label.no.nac.oes"))
    }

    "Contains an error summary for tenant address street1" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sublet[0].tenantAddress.street1", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for tenant address street2" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sublet[0].tenantAddress.street2", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for tenant address postcode" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sublet[0].tenantAddress.postcode", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for the sublet type yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "sublet[0].subletType", "sublet[0].subletType", SubletType.values(0).toString, false)
      assertContainsText(doc, messages("label.yes.oes"))
    }

    "contain radio buttons for the sublet type no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "sublet[0].subletType-2", "sublet[0].subletType", SubletType.values(1).toString, false)
      assertContainsText(doc, messages("label.no.nac.oes"))
    }

    "Contains an error summary for first fixed date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sublet[0].rentFixedDate.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for first fixed date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sublet[0].rentFixedDate.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain continue button with the value Continue" in {
      val doc = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
