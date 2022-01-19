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

import models.serviceContracts.submissions.{ContactType, ContactTypeEmail, ContactTypePhone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.libs.json.{JsResult, JsSuccess, Json}


class ContactTypes$Test extends AnyFlatSpec with should.Matchers {

  val jsonPhone = """"phone""""
  val jsonEmail = """"email""""
  val jsonBoth = """"both""""

  def toJson(data: ContactType): String = {
    Json.toJson(data).toString
  }

  def fromJson(json: String): JsResult[ContactType] = {
    Json.fromJson[ContactType](Json.parse(json))
  }

  "ContactTypes reader for 'phone' " should "map to ContactTypesPhone" in {
    toJson(ContactTypePhone) should be(jsonPhone)
  }

  "ContactTypesPhone" should "map to contact type 'phone' " in {
    fromJson(jsonPhone) should be(JsSuccess(ContactTypePhone))
  }
  "ContactTypes reader for 'email' " should "map to ContactTypesEmail" in {
    toJson(ContactTypeEmail) should be(jsonEmail)
  }

  "ContactTypesEmail" should "map to contact type 'email' " in {
    fromJson(jsonEmail) should be(JsSuccess(ContactTypeEmail))
  }
}
