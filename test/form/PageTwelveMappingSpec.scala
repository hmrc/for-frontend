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

package form

import form.PageTwelveForm.*
import models.pages.PageTwelve
import models.serviceContracts.submissions.{ChargeDetails, ResponsibleType}
import play.api.data.Form
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.FormBindingTestAssertions.*
import utils.MappingSpecs.*

class PageTwelveMappingSpec extends BaseSpec:

  import TestData.*

  "PageTwelveForm" should {
    "bind with the fields and not return issues" in
      mustBind(bind(baseData))(_ => ())

    "bind with the fields and return no errors, when the unnecessary details for ndr are not there" in {
      val data = baseData.updated("ndrCharges", "false") - "ndrDetails"

      mustBind(bind(data))(_ => ())
    }

    "bind with the fields and return errors, when the cost for ndr services and costs is not there" in {
      val data = baseData - "ndrDetails"
      val form = bind(data).convertGlobalToFieldErrors()

      mustContainError("ndrDetails", "error.required.businessRatesPerYear", form)
    }

    "bind with the fields and return no errors, when the unnecessary details for other included services are not there" in {
      val data = baseData.updated("includedServices", "false") - "includedServices.chargeDescription" - "includedServices.chargeCost"

      mustBind(bind(data))(_ => ())
    }

    "bind with the fields and return errors, when the necessary cost details for other included services are not there" in {
      val data = baseData - getKeyService(0).cost
      val form = bind(data)

      mustContainError(getKeyService(0).cost, "error.required.serviceChargesPerYear", form)
    }

    "bind with the fields and return errors, when the necessary description details for other included services are not there" in {
      val data = baseData - getKeyService(0).description
      val form = bind(data)

      mustContainError(getKeyService(0).description, "error.detailsOfService.required", form)
    }

    "allow upto 8 services" in {
      val d = addServices(7, baseData)
      mustBind(bind(d))(_ shouldBe responsibilitiesWith8Services)

      val form = bind(addServices(8, baseData))
      mustContainError("includedServicesDetails", Errors.tooManyServices, form)
    }

    "bind with the fields and return errors, when missing responsibility for outside repairs" in {
      val data = baseData - "responsibleOutsideRepairs"
      val form = bind(data)

      mustOnlyContainError("responsibleOutsideRepairs", Errors.responsibleOutsideRepairsRequired, form)
    }

    "bind with the fields and return errors, when missing responsibility for inside repairs" in {
      val data = baseData - "responsibleInsideRepairs"
      val form = bind(data)

      mustOnlyContainError("responsibleInsideRepairs", Errors.responsibleInsideRepairsRequired, form)
    }

    "bind with the fields and return errors, when missing responsibility for building insurance" in {
      val data = baseData - "responsibleBuildingInsurance"
      val form = bind(data)

      mustOnlyContainError("responsibleBuildingInsurance", Errors.responsibleBuildingInsuranceRequired, form)
    }

    "bind with the fields and return errors, when missing business rates" in {
      val data = baseData - "ndrCharges"
      val form = bind(data)

      mustOnlyContainError("ndrCharges", Errors.businessRatesRequired, form)
    }

    "bind with the fields and return no errors, when the unnecessary cost details for water charges are not there" in {
      val data = baseData.updated("waterCharges", "false") - "waterChargesCost"
      val form = bind(data)

      doesNotContainErrors(form)
    }

    "bind with the fields and return errors, when the necessary cost details for water charges are not there" in {
      val data = baseData - "waterChargesCost"
      val res  = bind(data).convertGlobalToFieldErrors()

      mustContainError("waterChargesCost", "error.required.waterChargesPerYear", res)
    }

    "validate the included non-domestic rate amount" in
      validateCurrency("ndrDetails", pageTwelveForm, baseData, ".businessRatesPerYear")

    "validate the included water services amount" in
      validateCurrency("waterChargesCost", pageTwelveForm, baseData, ".waterChargesPerYear")

    "validate the details of the first included service" in
      validateLettersNumsSpecCharsUptoLength(getKeyService(0).description, 50, pageTwelveForm, baseData, Some("error.detailsOfService.maxLength"))

    "validate the cost of the first included service" in
      validateCurrency(getKeyService(0).cost, pageTwelveForm, baseData, ".serviceChargesPerYear")

    "validate the description of services is no more than 50 characters" in
      validateLettersNumsSpecCharsUptoLength(getKeyService(0).description, 50, pageTwelveForm, dataWithSecondService, Some("error.detailsOfService.maxLength"))

    "validate the cost of the second included service" in
      validateCurrency(getKeyService(1).cost, pageTwelveForm, dataWithSecondService, ".serviceChargesPerYear")

    "show sub-field level errors for first service detail when service details are required" in {
      val data = baseData - getKeyService(0).description - getKeyService(0).cost
      val form = bind(data)

      mustContainError(getKeyService(0).description, "error.detailsOfService.required", form)
      mustContainError(getKeyService(0).cost, "error.required.serviceChargesPerYear", form)
    }
  }

  object TestData:

    def getKeyService(idx: Int): GetKeyService = GetKeyService(idx)

    class GetKeyService(idx: Int):
      val description: String     = s"includedServicesDetails[$idx].chargeDescription"
      val cost: String            = s"includedServicesDetails[$idx].chargeCost"
      val parentFieldName: String = s"includedServicesDetails[$idx]"

    def bind(data: Map[String, String]): Form[PageTwelve] = pageTwelveForm.bind(data).convertGlobalToFieldErrors()

    val responsibleOutsideRepairs: (String, String)    = "responsibleOutsideRepairs"    -> "tenant"
    val responsibleInsideRepairs: (String, String)     = "responsibleInsideRepairs"     -> "tenant"
    val responsibleBuildingInsurance: (String, String) = "responsibleBuildingInsurance" -> "tenant"
    val ndrCharges: (String, String)                   = "ndrCharges"                   -> "true"
    val ndrDetail: (String, String)                    = "ndrDetails"                   -> "99.99"
    val waterCharges: (String, String)                 = "waterCharges"                 -> "true"
    val waterChargesCost: (String, String)             = "waterChargesCost"             -> "120"
    val includedServices: (String, String)             = "includedServices"             -> "true"
    val service1Description: (String, String)          = getKeyService(0).description   -> "security"
    val service1Cost: (String, String)                 = getKeyService(0).cost          -> "200"

    val baseData: Map[String, String] = Map(
      responsibleOutsideRepairs,
      responsibleInsideRepairs,
      responsibleBuildingInsurance,
      ndrCharges,
      ndrDetail,
      waterCharges,
      waterChargesCost,
      includedServices,
      service1Description,
      service1Cost
    )

    val dataWithSecondService: Map[String, String] = baseData.updated(getKeyService(1).description, "insecurity").updated(getKeyService(1).cost, "250")

    def addServices(n: Int, data: Map[String, String]): Map[String, String] = (1 to n).foldLeft(data) { (s, v) =>
      s.updated(s"includedServicesDetails[$v].chargeDescription", "blah blah blah")
        .updated(s"includedServicesDetails[$v].chargeCost", "45")
    }

    val responsibilitiesWith8Services: PageTwelve = PageTwelve(
      ResponsibleType.tenant,
      ResponsibleType.tenant,
      ResponsibleType.tenant,
      true,
      Some(99.99),
      true,
      Some(120),
      true,
      List(
        ChargeDetails("security", 200),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45),
        ChargeDetails("blah blah blah", 45)
      )
    )
