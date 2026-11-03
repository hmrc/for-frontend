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

/**
  * @author Yuriy Tumakha
  */
class LockedOutSpec extends AuditEventTest:

  "LockedOut" should {
    "serialize to expected JSON" in {
      val event = LockedOut(
        referenceNumber = "9999000777",
        postcode = "AA111AA",
        postcodeCleaned = "AA11 1AA",
        lockedIP = "172.17.0.1"
      )

      verifyEventJson(event, "LockedOut", "LockedOut.json")
    }
  }
