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

import form.PageSevenForm.pageSevenForm
import models.pages.PageSeven
import models.serviceContracts.submissions.ReviewIntervalType
import utils.SummaryBuilder.*
import play.api.data.{Form, FormError}
import views.behaviours.QuestionViewBehaviours

class Part7Spec extends QuestionViewBehaviours[PageSeven]:

  private def page7 = inject[views.html.part7]

  private val messageKeyPrefix = "section7"

  val form: Form[PageSeven] = pageSevenForm

  private def createView = () => page7(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageSeven]) => page7(form, completeFullPathJourney)(using getRequest, messages)

  "Page seven view" should {

    behave like normalPage(createView, messageKeyPrefix)

    "contain radio buttons for rent reviews yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "leaseContainsRentReviews", "leaseContainsRentReviews", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for rent reviews no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "leaseContainsRentReviews-2", "leaseContainsRentReviews", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain radio buttons for how often rent reviewed 3 years" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.reviewIntervalType", "rentReviewDetails.reviewIntervalType", ReviewIntervalType.values(0).toString, false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for how often rent reviewed 5 years" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.reviewIntervalType-2", "rentReviewDetails.reviewIntervalType", ReviewIntervalType.values(1).toString, false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain radio buttons for how often rent reviewed 7 years" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.reviewIntervalType-3", "rentReviewDetails.reviewIntervalType", ReviewIntervalType.values(2).toString, false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for how often rent reviewed other" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.reviewIntervalType-4", "rentReviewDetails.reviewIntervalType", ReviewIntervalType.values(3).toString, false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "Contains an error summary for rent review last review date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("rentReviewDetails.lastReviewDate.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for rent review last review date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("rentReviewDetails.lastReviewDate.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for can rent be reduced yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.canRentReduced", "rentReviewDetails.canRentReduced", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for can rent be reduced no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.canRentReduced-2", "rentReviewDetails.canRentReduced", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain radio buttons for can rent result of review yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.rentResultOfRentReview", "rentReviewDetails.rentResultOfRentReview", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for can rent result of review no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.rentResultOfRentReview-2", "rentReviewDetails.rentResultOfRentReview", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "Contains an error summary for rent review when was review date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for rent review when was review date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for rent fixed yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween", "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for rent fixed no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween-2", "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain continue button with the value Continue" in {
      val doc = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
