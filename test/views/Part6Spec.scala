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

import form.PageSixForm.pageSixForm
import models.pages.PageSix
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part6Spec extends QuestionViewBehaviours[PageSix]:

  private def page6 = inject[views.html.part6]

  private val messageKeyPrefix = "section6"

  val form: Form[PageSix] = pageSixForm

  private def createView = () => page6(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageSix]) => page6(form, completeFullPathJourney)(using getRequest, messages)

  "Page six view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      "writtenAgreement.breakClauseDetails",
      "writtenAgreement.steppedDetails[0].amount",
      "writtenAgreement.steppedDetails[1].amount"
    )

    "contain radio buttons for lease agreement tenancy" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "leaseAgreementType", "leaseAgreementType", LeaseAgreementType.values(0).toString, false)
      assertContainsText(doc, messages("leaseAgreementTypes.leaseTenancy"))
    }

    "contain radio buttons for lease agreement licence" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "leaseAgreementType-2", "leaseAgreementType", LeaseAgreementType.values(1).toString, false)
      assertContainsText(doc, messages("leaseAgreementTypes.licenceOther"))
    }

    "contain radio buttons for lease agreement verbal" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "leaseAgreementType-3", "leaseAgreementType", LeaseAgreementType.values(2).toString, false)
      assertContainsText(doc, messages("leaseAgreementTypes.verbal"))
    }

    // Tennancy/Licence agreement
    "Contains an error summary for licence agreement date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.startDate.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for licence agreement date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.startDate.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for current agreement open ended yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.rentOpenEnded", "writtenAgreement.rentOpenEnded", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for current agreement open ended no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.rentOpenEnded-2", "writtenAgreement.rentOpenEnded", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "Contains an error summary for how long current agreement date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.leaseLength.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for how long current agreement date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.leaseLength.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for end agreement early yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.leaseAgreementHasBreakClause", "writtenAgreement.leaseAgreementHasBreakClause", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for end agreement early no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.leaseAgreementHasBreakClause-2", "writtenAgreement.leaseAgreementHasBreakClause", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "contain radio buttons for stepped rent agreement yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.agreementIsStepped", "writtenAgreement.agreementIsStepped", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for stepped rent agreement no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "writtenAgreement.agreementIsStepped-2", "writtenAgreement.agreementIsStepped", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    // Tennancy/Licence agreement - First rent period
    "Contains an error summary for stepped rent start date day" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.day", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent start date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent start date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent end date day" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.day", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent end date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent end date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[1].stepFrom.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    // Tennancy/Licence agreement - Second rent period
    "Contains an error summary for stepped rent second period starts date day" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.day", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent second period starts date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent second period starts date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent second period end date day" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.day", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent second period end date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for stepped rent second period end date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("writtenAgreement.steppedDetails[0].stepFrom.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    // Verbal agreement
    "Contains an error summary for verbal agreement date month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("verbalAgreement.startDate.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for verbal agreement date year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("verbalAgreement.startDate.year", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "contain radio buttons for verbal agreement open ended yes" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "verbalAgreement.rentOpenEnded", "verbalAgreement.rentOpenEnded", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))
    }

    "contain radio buttons for verbal agreement open ended no" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsRadioButton(doc, "verbalAgreement.rentOpenEnded-2", "verbalAgreement.rentOpenEnded", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))
    }

    "Contains an error summary for how long current agreement month" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("verbalAgreement.leaseLength.month", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

    "Contains an error summary for how long current agreement year" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("verbalAgreement.leaseLength.years", "error"))))
      assertRenderedByCssSelector(doc, ".govuk-error-summary")
    }

//
//    "Contains an error summary for landlord address street1" in {
//      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.street1", "error"))))
//      assertRenderedByCssSelector(doc, ".govuk-error-summary")
//    }
//
//    "Contains an error summary for landlord address street2" in {
//      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.street2", "error"))))
//      assertRenderedByCssSelector(doc, ".govuk-error-summary")
//    }
//
//    "Contains an error summary for tenant address postcode" in {
//      val doc = asDocument(createViewUsingForm(form.withError(FormError("landlordAddress.postcode", "error"))))
//      assertRenderedByCssSelector(doc, ".govuk-error-summary")
//    }
//
//    "contain radio buttons for relationship to landlord mo connection" in {
//      val doc = asDocument(createViewUsingForm(form))
//      assertContainsRadioButton(doc, "landlordConnectType", "landlordConnectType", LandlordConnectionType.values(0).toString, false)
//      assertContainsText(doc, messages("landlordConnectionType.noConnected"))
//    }
//
//    "contain radio buttons for relationship to landlord family" in {
//      val doc = asDocument(createViewUsingForm(form))
//      assertContainsRadioButton(doc, "landlordConnectType-2", "landlordConnectType", LandlordConnectionType.values(1).toString, false)
//      assertContainsText(doc, messages("landlordConnectionType.family"))
//    }
//
//    "contain radio buttons for relationship to landlord other" in {
//      val doc = asDocument(createViewUsingForm(form))
//      assertContainsRadioButton(doc, "landlordConnectType-3", "landlordConnectType", LandlordConnectionType.values(2).toString, false)
//      assertContainsText(doc, messages("landlordConnectionType.other"))
//    }

    "contain continue button with the value Continue" in {
      val doc         = asDocument(createViewUsingForm(form))
      val loginButton = doc.getElementById("continue-button").text()
      assert(loginButton == messages("button.continue.label"))
    }
  }
