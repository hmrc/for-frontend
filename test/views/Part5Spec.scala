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

import form.PageFiveForm.pageFiveForm
import models.pages.PageFive
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part5Spec extends QuestionViewBehaviours[PageFive]:

  private def page5 = inject[views.html.part5]

  private val messageKeyPrefix = "section5"

  val form: Form[PageFive] = pageFiveForm

  private def createView = () => page5(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageFive]) => page5(form, completeFullPathJourney)(using getRequest, messages)

  "Page five view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "landlordFullName", "landlordAddress.buildingNameNumber", "landlordConnectText")

    "Contains an error summary for landlord address street1" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.street1", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for landlord address street2" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.street2", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for tenant address postcode" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.postcode", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for relationship to landlord mo connection" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "landlordConnectType", "landlordConnectType", LandlordConnectionType.values(0).toString, false)
      assertContainsText(doc, messages("landlordConnectionType.noConnected"))
    }

    "contain radio buttons for relationship to landlord family" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "landlordConnectType-2", "landlordConnectType", LandlordConnectionType.values(1).toString, false)
      assertContainsText(doc, messages("landlordConnectionType.family"))
    }

    "contain radio buttons for relationship to landlord other" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "landlordConnectType-3", "landlordConnectType", LandlordConnectionType.values(2).toString, false)
      assertContainsText(doc, messages("landlordConnectionType.other"))
    }

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
