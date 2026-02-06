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

package helpers

import connectors.Audit

import javax.inject.{Inject, Singleton}
import models.pages.Summary
import models.serviceContracts.submissions.Address
import play.api.mvc.Request
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.Future

@Singleton
class AddressAuditing @Inject() (audit: Audit) {

  def apply(s: Summary, request: Request[?]): Future[Unit] = {
    if s.propertyAddress.isDefined then auditManualAddress(s.referenceNumber, s.addressUserBelievesIsCorrect, request)
    s.sublet.foreach(p4 => p4.sublet.foreach(sub => auditManualAddress(s.referenceNumber, sub.tenantAddress, request)))
    Future.successful(())
  }

  private def auditManualAddress(referenceNumber: String, address: Address, request: Request[?]): Future[AuditResult] = {
    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    audit(
      "manualAddressSubmitted",
      detail = Map(
        Audit.referenceNumber -> referenceNumber,
        "submittedLine1"      -> address.buildingNameNumber,
        "submittedLine2"      -> address.street1.getOrElse(""),
        "submittedLine3"      -> address.street2.getOrElse(""),
        "submittedPostcode"   -> address.postcode,
        "transactionName"     -> "sending_rental_information"
      )
    )(using hc)
  }

}
