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

import org.scalatest.Assertion
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.vo.service.model.event.AuditEvent
import uk.gov.hmrc.vo.unit.test.{BaseAppSpec, JsonAssertions}

/**
  * @author Yuriy Tumakha
  */
abstract class AuditEventTest extends BaseAppSpec with JsonAssertions("event"):

  def verifyEventJson[T <: AuditEvent](
    event: T,
    expectedAuditType: String,
    expectedJsonFilename: String
  )(using writes: Writes[T]
  ): Assertion =

    val eventJson = Json.toJson(event)

    event.auditType shouldBe expectedAuditType

    compareJson(eventJson, expectedJsonFilename)
