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
class UserLoginSpec extends AuditEventTest:

  "UserLogin" should {
    "serialize to expected JSON" in {
      val event = UserLogin(
        returningUser = false,
        referenceNumber = "01234567000",
        address = Address("13", Some("Street"), Some("City"), "AA11 1AA")
      )

      verifyEventJson(event, "UserLogin", "UserLogin.json")
    }
  }

  "UserLogin minimal" should {
    "serialize to expected JSON" in {
      val event = UserLogin(
        returningUser = true,
        referenceNumber = "01234567000",
        address = Address("13", None, None, "AA11 1AA")
      )

      verifyEventJson(event, "UserLogin", "UserLogin_minimal.json")
    }
  }
