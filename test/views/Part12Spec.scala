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

import form.PageTwelveForm.pageTwelveForm
import models.pages.PageTwelve
import models.serviceContracts.submissions.*
import play.api.data.{Form, FormError}
import utils.SummaryBuilder.*
import views.behaviours.QuestionViewBehaviours

class Part12Spec extends QuestionViewBehaviours[PageTwelve]:

  private def page12 = inject[views.html.part12]

  private val messageKeyPrefix = "section12"

  val form: Form[PageTwelve] = pageTwelveForm

  private def createView = () => page12(form, completeFullPathJourney)(using getRequest, messages)

  private def createViewUsingForm = (form: Form[PageTwelve]) => page12(form, completeFullPathJourney)(using getRequest, messages)

  "Page twelve view" should {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      "ndrDetails",
      "waterChargesCost",
      "includedServicesDetails[0].chargeCost",
      "includedServicesDetails[0].chargeDescription"
    )

    "contains subheadings" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("info.responsibilitiesAndCosts.paying"))
      assertContainsText(doc, messages("info.responsibilitiesAndCosts.surcharge"))
      assertContainsText(doc, messages("section12.under.terms.text"))
    }

    // Who is responsible for paying for repairs outside
    "contains question who is responsible for paying for repairs outside" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.responsibleOutsideRepairs"))

      assertContainsRadioButton(doc, "responsibleOutsideRepairs", "responsibleOutsideRepairs", ResponsibleType.values(0).toString, false)
      assertContainsText(doc, messages("responsibleType.landlord"))

      assertContainsRadioButton(doc, "responsibleOutsideRepairs-2", "responsibleOutsideRepairs", ResponsibleType.values(1).toString, false)
      assertContainsText(doc, messages("responsibleType.tenant"))

      assertContainsRadioButton(doc, "responsibleOutsideRepairs-3", "responsibleOutsideRepairs", ResponsibleType.values(2).toString, false)
      assertContainsText(doc, messages("responsibleType.both"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("responsibleOutsideRepairs", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Who is responsible for paying for repairs inside
    "contains question Who is responsible for paying for repairs inside" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.responsibleInsideRepairs"))

      assertContainsRadioButton(doc, "responsibleInsideRepairs", "responsibleInsideRepairs", ResponsibleType.values(0).toString, false)
      assertContainsText(doc, messages("responsibleType.landlord"))

      assertContainsRadioButton(doc, "responsibleInsideRepairs-2", "responsibleInsideRepairs", ResponsibleType.values(1).toString, false)
      assertContainsText(doc, messages("responsibleType.tenant"))

      assertContainsRadioButton(doc, "responsibleInsideRepairs-3", "responsibleInsideRepairs", ResponsibleType.values(2).toString, false)
      assertContainsText(doc, messages("responsibleType.both"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("responsibleInsideRepairs", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Who is responsible for paying for buildings insurance
    "contains question who is responsible for paying for buildings insurance" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.responsibleBuildingInsurance"))

      assertContainsRadioButton(doc, "responsibleBuildingInsurance", "responsibleBuildingInsurance", ResponsibleType.values(0).toString, false)
      assertContainsText(doc, messages("responsibleType.landlord"))

      assertContainsRadioButton(doc, "responsibleBuildingInsurance-2", "responsibleBuildingInsurance", ResponsibleType.values(1).toString, false)
      assertContainsText(doc, messages("responsibleType.tenant"))

      assertContainsRadioButton(doc, "responsibleBuildingInsurance-3", "responsibleBuildingInsurance", ResponsibleType.values(2).toString, false)
      assertContainsText(doc, messages("responsibleType.both"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("responsibleBuildingInsurance", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Are business rates included in the rent
    "contains question are business rates included in the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.ndrCharges"))

      assertContainsRadioButton(doc, "ndrCharges", "ndrCharges", "true", false)
      assertContainsText(doc, messages("label.yes.ydyn"))

      assertContainsRadioButton(doc, "ndrCharges-2", "ndrCharges", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydyn"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("ndrCharges", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Total amount per year included in the rent (excluding VAT)
    "contains question ndr total amount per year included in the rent (excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("ndrCharges.ndrDetails.label"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("ndrDetails", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Are water charges included in the rent
    "contains question are water charges included in the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.waterCharges"))

      assertContainsRadioButton(doc, "waterCharges", "waterCharges", "true", false)
      assertContainsText(doc, messages("label.yes.ydyn"))

      assertContainsRadioButton(doc, "waterCharges-2", "waterCharges", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydyn"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("waterCharges", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Total amount per year included in the rent (excluding VAT)
    "contains question water charges total amount per year included in the rent (excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("waterChargesCost.label"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("waterChargesCost", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Are service charges included in the rent
    "contains question are service charges included in the rent" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.includedServices"))

      assertContainsRadioButton(doc, "includedServices", "includedServices", "true", false)
      assertContainsText(doc, messages("label.yes.ydy"))

      assertContainsRadioButton(doc, "includedServices-2", "includedServices", "false", false)
      assertContainsText(doc, messages("label.no.nac.ydy"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("includedServices", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Total amount per year included in the rent (excluding VAT)
    "contains question included services total amount per year included in the rent (excluding VAT)" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("includedServicesDetails.chargeCost.label"))
      assertContainsText(doc, messages("includedServicesDetails.chargeCost.hint"))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("includedServicesDetails[0].chargeCost", "error"))))
      assertRenderedByCssSelector(errorDoc, ".govuk-error-summary")
      assertRenderedByCssSelector(errorDoc, ".govuk-error-message")
    }

    // Provide details of the services
    "contains question Provide details of the services" in {
      val doc = asDocument(createViewUsingForm(form))
      assertContainsText(doc, messages("label.includedServicesDetails.chargeDescription"))
      assertContainsText(doc, messages("hint.includedServicesDetails.chargeDescription").replace("  ", " "))

      val errorDoc = asDocument(createViewUsingForm(form.withError(FormError("includedServicesDetails[0].chargeDescription", "error"))))
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
