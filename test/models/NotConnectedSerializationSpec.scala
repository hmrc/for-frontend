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

package models

import models.serviceContracts.submissions.{Address, NotConnectedSubmission}
import play.api.libs.json.JsString
import uk.gov.hmrc.vo.unit.test.BaseSpec

import java.time.Instant

class NotConnectedSerializationSpec extends BaseSpec:

  /*
    GMT: Wednesday, 31 July 2019 15:18:15
    Your time zone: Wednesday, 31 July 2019 16:18:15 GMT+01:00 DST
   */
  private val UNIX_DATETIME: Long    = 1564586295L
  private val UNIX_MILLISECOND: Long = 258L
  private val ISO_TIME               = "2019-07-31T15:18:15.258Z"
  private val INSTANT: Instant       = Instant.ofEpochMilli((UNIX_DATETIME * 1000) + UNIX_MILLISECOND)

  "NotConnectedSubmission" should {
    "map Java Instant to ISO 8601 format" in {
      val notConnected = NotConnectedSubmission("222", Address("10", None, None, "BN 12 4AX"), "xxx", None, None, None, INSTANT, false)
      val result       = NotConnectedSubmission.format.writes(notConnected)

      result.value("createdAt") shouldBe JsString(ISO_TIME)
    }
  }
