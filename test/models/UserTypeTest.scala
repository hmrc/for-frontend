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

package models

import models.serviceContracts.submissions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.libs.json.{JsResult, JsSuccess, Json}

class UserTypeTest extends AnyFlatSpec with should.Matchers {

  val jsonOccupier = "\"occupier\""
  val jsonOccupiersAgent = "\"occupiersAgent\""
  val jsonLeaseholder = "\"leaseholder\""
  val jsonLeaseholdersAgent = "\"leaseholdersAgent\""
  val jsonOwner = "\"owner\""
  val jsonOwnersAgent = "\"ownersAgent\""
  val jsonOwnerOccupier = "\"ownerOccupier\""

  def toJson(data: UserType): String = {
    Json.toJson(data).toString
  }

  def fromJson(json: String): JsResult[UserType] = {
    Json.fromJson[UserType](Json.parse(json))
  }

  "UserType reader for 'occupier' " should "map to UserTypeOccupier" in {
    toJson(UserTypeOccupier) should be(jsonOccupier)
  }

  "UserTypeOccupier" should "map to user type 'occupier' " in {
    fromJson(jsonOccupier) should be(JsSuccess(UserTypeOccupier))
  }

  "UserType for type 'occupiersAgent' " should "map to UserTypeOccupiersAgent" in {
    toJson(UserTypeOccupiersAgent) should be(jsonOccupiersAgent)
  }

  "UserTypeOccupiersAgent" should "map to user type occupiersAgent" in {
    fromJson(jsonOccupiersAgent) should be(JsSuccess(UserTypeOccupiersAgent))
  }
 
  "UserType reader for 'owner' " should "map to UserTypeOwner" in {
    toJson(UserTypeOwner) should be(jsonOwner)
  }

  "UserTypeOwner" should "map to user type 'owner' " in {
    fromJson(jsonOwner) should be(JsSuccess(UserTypeOwner))
  }

  "UserType reader for 'ownersAgent' " should "map to UserTypeOwnersAgent" in {
    toJson(UserTypeOwnersAgent) should be(jsonOwnersAgent)
  }

  "UserTypeOwnersAgent" should "map to user type 'ownersAgent' " in {
    fromJson(jsonOwnersAgent) should be(JsSuccess(UserTypeOwnersAgent))
  }
}
