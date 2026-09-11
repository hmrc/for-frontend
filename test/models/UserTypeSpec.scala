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

import models.serviceContracts.submissions.*
import play.api.libs.json.{JsResult, JsSuccess, Json}
import uk.gov.hmrc.vo.unit.test.BaseSpec

class UserTypeSpec extends BaseSpec:

  private val jsonOccupier       = "\"occupier\""
  private val jsonOccupiersAgent = "\"occupiersAgent\""
  private val jsonOwner          = "\"owner\""
  private val jsonOwnersAgent    = "\"ownersAgent\""

  private def toJson(data: UserType): String =
    Json.toJson(data).toString

  private def fromJson(json: String): JsResult[UserType] =
    Json.fromJson[UserType](Json.parse(json))

  "UserType reader for 'occupier' " should {
    "map to UserTypeOccupier" in {
      toJson(UserType.occupier) shouldBe jsonOccupier
    }
  }

  "UserTypeOccupier" should {
    "map to user type 'occupier' " in {
      fromJson(jsonOccupier) shouldBe JsSuccess(UserType.occupier)
    }
  }

  "UserType for type 'occupiersAgent' " should {
    "map to UserTypeOccupiersAgent" in {
      toJson(UserType.occupiersAgent) shouldBe jsonOccupiersAgent
    }
  }

  "UserTypeOccupiersAgent" should {
    "map to user type occupiersAgent" in {
      fromJson(jsonOccupiersAgent) shouldBe JsSuccess(UserType.occupiersAgent)
    }
  }

  "UserType reader for 'owner' " should {
    "map to UserTypeOwner" in {
      toJson(UserType.owner) shouldBe jsonOwner
    }
  }

  "UserTypeOwner" should {
    "map to user type 'owner' " in {
      fromJson(jsonOwner) shouldBe JsSuccess(UserType.owner)
    }
  }

  "UserType reader for 'ownersAgent' " should {
    "map to UserTypeOwnersAgent" in {
      toJson(UserType.ownersAgent) shouldBe jsonOwnersAgent
    }
  }

  "UserTypeOwnersAgent" should {
    "map to user type 'ownersAgent' " in {
      fromJson(jsonOwnersAgent) shouldBe JsSuccess(UserType.ownersAgent)
    }
  }
