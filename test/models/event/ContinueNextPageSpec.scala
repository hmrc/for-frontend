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

package models.event

import models.serviceContracts.submissions.Address

/**
  * @author Yuriy Tumakha
  */
class ContinueNextPageSpec extends AuditEventTest:

  "ContinueNextPage" should {
    "serialize to expected JSON" in {
      val event = ContinueNextPage(
        url = "/sending-rental-information/page/2",
        referenceNumber = "9999000777",
        address = Some(Address("13", Some("Street"), Some("City"), "AA11 1AA")),
        updatedAddress = Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA")),
        variant = Some("1"),
        data = Some(
          Map(
            "fullName"              -> "Full name",
            "userType"              -> "owner",
            "contactDetails.phone"  -> "1234567890",
            "contactDetails.email1" -> "user@email.com"
          )
        )
      )

      verifyEventJson(event, "ContinueNextPage", "ContinueNextPage.json")
    }
  }

  "ContinueNextPage minimal" should {
    "serialize to expected JSON" in {
      val event = ContinueNextPage(
        url = "/sending-rental-information/page/2",
        referenceNumber = "9999000777"
      )

      verifyEventJson(event, "ContinueNextPage", "ContinueNextPage_minimal.json")
    }
  }
