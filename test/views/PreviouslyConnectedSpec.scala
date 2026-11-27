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

import form.PreviouslyConnectedForm.formMapping
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class PreviouslyConnectedSpec extends QuestionViewBehaviours[PreviouslyConnected]:

  private def previouslyConnectedPage = inject[views.html.previouslyConnected]

//  private val messageKeyPrefix = "notConnected"

  val form: Form[PreviouslyConnected] = formMapping

//  private def createView = () => previouslyConnectedPage(form, incompletePageOneJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PreviouslyConnected]) => previouslyConnectedPage(form, incompletePageOneJourney)(using getRequest, messages)

  "Page previously connected view" should {

    // Note: Heading not working as per the pattern. This can be revisited as part of NGR work.
    //    behave like normalPage(createView, messageKeyPrefix)

    // Have you ever had a connection to the property
    "contains question have you ever had a connection to the property" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("notConnected.haveYouBeenConnected.label"))

      assertContainsRadioButton(doc, "haveYouBeenConnected", "haveYouBeenConnected", "true", false)
      assertContainsText(doc, messages("label.yes.ydw"))

      assertContainsRadioButton(doc, "haveYouBeenConnected-2", "haveYouBeenConnected", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydw"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("haveYouBeenConnected", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    "contain continue button" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
