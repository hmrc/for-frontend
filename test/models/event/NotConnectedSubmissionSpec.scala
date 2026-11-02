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
class NotConnectedSubmissionSpec extends AuditEventTest:

  "NotConnectedSubmission" should {
    "serialize to expected JSON" in {
      val event = NotConnectedSubmission(
        language = "en",
        referenceNumber = "01234567000",
        address = Some(Address("13", Some("Street"), Some("City"), "AA11 1AA")),
        updatedAddress = Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA"))
      )

      verifyEventJson(event, "NotConnectedSubmission", "NotConnectedSubmission.json")
    }
  }

  "NotConnectedSubmission minimal" should {
    "serialize to expected JSON" in {
      val event = NotConnectedSubmission("cy", "01234567000")

      verifyEventJson(event, "NotConnectedSubmission", "NotConnectedSubmission_minimal.json")
    }
  }
