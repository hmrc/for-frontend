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

import form.PageOneForm.pageOneForm
import models.serviceContracts.submissions.*
import play.api.data.Form
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part1Spec extends QuestionViewBehaviours[Address]:

  private def page1 = inject[views.html.part1]

  private val messageKeyPrefix = "section1"

  val form: Form[Address] = pageOneForm

  private def createView = () => page1(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[Address]) => page1(form, completeFullPathJourney)(using getRequest, messages)

  "Page One view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, "buildingNameNumber", "street1", "street2", "postcode")

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
