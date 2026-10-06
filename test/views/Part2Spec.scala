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

import form.PageTwoForm.pageTwoForm
import models.serviceContracts.submissions.*
import play.api.data.Form
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part2Spec extends QuestionViewBehaviours[CustomerDetails]:

  private def page2 = inject[views.html.part2]

  private val messageKeyPrefix = "section2"

  val form: Form[CustomerDetails] = pageTwoForm

  private def createView = () => page2(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[CustomerDetails]) => page2(form, completeFullPathJourney)(using getRequest, messages)

  "Page Two view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "fullName", "contactDetails.email1", "contactDetails.phone")

    "contain radio buttons for the value occupier or trustee occupier" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "userType", "userType", UserType.values(0).toString, false)
      assertContainsText(doc, messages("userType.occupier.label"))
    }

    "contain radio buttons for the value owner or trustee owner" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "userType-2", "userType", UserType.values(1).toString, false)
      assertContainsText(doc, messages("userType.owner.label"))
    }

    "contain radio buttons for the value occupier agent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "userType-3", "userType", UserType.values(2).toString, false)
      assertContainsText(doc, messages("userType.occupiersAgent.label"))
    }

    "contain radio buttons for the value owner agent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "userType-4", "userType", UserType.values(3).toString, false)
      assertContainsText(doc, messages("userType.ownersAgent.label"))
    }

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
