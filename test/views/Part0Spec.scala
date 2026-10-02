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

import form.PageZeroForm.pageZeroForm
import models.serviceContracts.submissions.AddressConnectionType
import play.api.data.Form
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part0Spec extends QuestionViewBehaviours[AddressConnectionType]:

  private def page0 = inject[views.html.part0]

  override val form: Form[AddressConnectionType] = pageZeroForm

  private def createView = () => page0(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[AddressConnectionType]) => page0(form, completeFullPathJourney)(using getRequest, messages)

  "Page zero view" should {
    "behave like a normal page" when {
      "rendered" should {
        "have the correct banner title" in
          checkServiceNameInHeaderBanner(createView)

        "display the correct browser title" in {
          val doc = asDocument(createView())
          assertEqualsValue(doc, "title", s"${messages("section0.intro.text", prefilledAddress.singleLine)} - ${messages("service.name")} - ${messages("gov.name")}")
        }

        "display the correct page title" in {
          val doc = asDocument(createView())
          assertEqualsValue(
            doc,
            "h1",
            messages("section0.intro.text", prefilledAddress.singleLine)
          )
        }

        "display language toggles" in {
          val doc = asDocument(createView())
          doc.getElementById("cymraeg-switch") != null ||
          !doc
            .getElementsByAttributeValue("href", "/valuation-office-agency-contact-frontend/language/cymraeg")
            .isEmpty
        }
      }
    }

    "contain radio buttons for the value yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "isRelated", "isRelated", AddressConnectionType.values(0).toString, false)
      assertContainsText(doc, messages("section0.isRelated.yes.label"))
    }

    "contain radio buttons for the value yes edit address" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "isRelated-2", "isRelated", AddressConnectionType.values(1).toString, false)
      assertContainsText(doc, messages("section0.isRelated.yes-change-address.label"))
    }

    "contain radio buttons for the value no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "isRelated-3", "isRelated", AddressConnectionType.values(2).toString, false)
      assertContainsText(doc, messages("section0.isRelated.no.label"))
    }

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
