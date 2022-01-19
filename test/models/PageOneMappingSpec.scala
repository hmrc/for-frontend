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

import models.serviceContracts.submissions.Address
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.libs.json.{JsResult, JsSuccess, Json}

class PageOneMappingSpec extends AnyFlatSpec with should.Matchers {


  val json2 = """{"buildingNameNumber":"Some House","street1":"Some Street","street2":"Some City","postcode":"AA11 1AA"}"""
  val data2 = Address("Some House", Some("Some Street"),Some("Some City"),"AA11 1AA")

  def toJson(data:Address):String = {
    val json = Json.toJson(data).toString
    json
  }

  def fromJson(json:String):JsResult[Address]= {
    Json.fromJson[Address](Json.parse(json))
  }

  "Address with a fully filled in address" should "create a fully filled Address" in {
    fromJson(json2) should be(JsSuccess(data2))
  }

}
