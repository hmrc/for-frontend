/*
 * Copyright 2022 HM Revenue & Customs
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
import models.pages.{PageFour, SubletDetails, Summary}
import models.serviceContracts.submissions.{Address, AddressConnectionTypeYesChangeAddress, SubletPart}
import models.{LookupServiceAddress, RoughDate}
import org.joda.time.DateTime
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Disabled
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, DatastreamMetrics}

import scala.concurrent.{ExecutionContext, Future}

class AddressAuditingSpec extends AnyFlatSpec with should.Matchers with MockitoSugar {
  import TestData._

  behavior of "Address Auditing"

  it should "send a manualAddressSubmitted audit when the user submits a corrected property address" in {
    val s = summaryWithPropertyAddress(Some(propertyAddress), Some(oneLineChanged))
    TestAddressAuditing(s, FakeRequest())

    StubAuditer.mustHaveSentAudit("manualAddressSubmitted",
      Map(
        "submittedLine1" -> oneLineChanged.buildingNameNumber,
        "submittedLine2" -> oneLineChanged.street1.getOrElse(""),
        "submittedPostcode" -> oneLineChanged.postcode
      )
    )
  }

  it should "send a manualAddressSubmitted audit when the user submits a corrected sublet address" in {
    val s = summaryWithSubletAddress(Some(propertyAddress), oneLineChanged)
    TestAddressAuditing(s, FakeRequest())

    StubAuditer.mustHaveSentAudit("manualAddressSubmitted",
      Map(
        "submittedLine1" -> oneLineChanged.buildingNameNumber,
        "submittedLine2" -> oneLineChanged.street1.getOrElse(""),
        "submittedPostcode" -> oneLineChanged.postcode
      )
    )
  }

  private def summaryWithPropertyAddress(voaAddress: Option[Address], corrected: Option[Address]): Summary = {
    Summary("1234567890", DateTime.now,
      Some(AddressConnectionTypeYesChangeAddress), corrected,
      None, None, None, None, None, None, None, None, None, None, None, None, None,
      voaAddress, Nil
    )
  }

  private def summaryWithSubletAddress(voaAddress: Option[Address], submitted: Address): Summary = {
    Summary("123467890", DateTime.now, None, None, None, None,
      Some(PageFour(true, List(SubletDetails("Mr Tenant", submitted, SubletPart, Option("Something"), "Stuff", BigDecimal(0.01), RoughDate(None, Some(1), 2011))))),
      None, None, None, None, None, None, None, None, None, None, voaAddress, Nil)
  }

}

object TestData {
  val propertyAddress = Address("1 The Road", Some("The Town"), None, "AA11 1AA")
  val fromPostcodeLookup = LookupServiceAddress("1 The Road", Some("The Town"), None, "AA11 1AA", "GB1234567890")
  val unchanged = Address("1 The Road", Some("The Town"), None, "AA11 1AA")
  val manual = Address("1 The Road", Some("The Town"), None, "AA11 1AA")
  val oneLineChanged = Address("1A The Road", Some("The Town"), None, "AA11 1AA")
  val twoLinesChanged = Address("1 The Other Road", Some("The Other Town"), None, "AA11 1AA")
  val overseas = Address("1 The Road", Some("Atlantis"), None, "The Sea")
}

object TestAddressAuditing extends AddressAuditing(StubAuditer) {
  protected val audit = StubAuditer
}

object StubAuditer extends Audit with should.Matchers with MockitoSugar {
  private case class AuditEvent(event: String, detail: Map[String, String])
  private var lastSentAudit: AuditEvent = null

  override def apply(event: String, detail: Map[String, String])(implicit hc: HeaderCarrier) = {
    lastSentAudit = AuditEvent(event, detail)
    Future.successful(Disabled)
  }

  def mustHaveSentAudit(event: String, detail: Map[String, String]) = {
    lastSentAudit should not equal(null)
    lastSentAudit.event should equal (event)
    detail foreach { d =>
      lastSentAudit.detail should contain (d)
    }
    lastSentAudit = null
  }

  override implicit def ec: ExecutionContext = ExecutionContext.Implicits.global

  override def auditingConfig: AuditingConfig = mock[AuditingConfig]

  override def auditChannel: AuditChannel = mock[AuditChannel]

  override def datastreamMetrics: DatastreamMetrics = mock[DatastreamMetrics]

}
