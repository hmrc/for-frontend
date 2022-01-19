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

class PropertyTypeTest extends AnyFlatSpec with should.Matchers {

  val jsonShop = """"shop""""
  val jsonFactory = """"factory""""
  val jsonOffice = """"office""""
  val jsonWarehouse = """"warehouse""""
  val jsonOther = """"other""""

  def toJson(data: PropertyType): String = {
    Json.toJson(data).toString
  }

  def fromJson(json: String): JsResult[PropertyType] = {
    Json.fromJson[PropertyType](Json.parse(json))
  }

  "PropertyTypes reader for 'shop' " should "map to PropertyTypesShop" in {
    toJson(PropertyTypesShop) should be(jsonShop)
  }

  "PropertyTypesShop" should "map to property type 'shop' " in {
    fromJson(jsonShop) should be(JsSuccess(PropertyTypesShop))
  }

  "PropertyTypes reader for 'factory' " should "map to PropertyTypesFactory" in {
    toJson(PropertyTypesFactory) should be(jsonFactory)
  }

  "PropertyTypeFactory" should "map to property type 'factory' " in {
    fromJson(jsonFactory) should be(JsSuccess(PropertyTypesFactory))
  }

  "PropertyTypes reader for 'office' " should "map to PropertyTypesOffice" in {
    toJson(PropertyTypesOffice) should be(jsonOffice)
  }

  "PropertyTypeOffice" should "map to property type 'office' " in {
    fromJson(jsonOffice) should be(JsSuccess(PropertyTypesOffice))
  }

  "PropertyTypes reader for 'warehouse' " should "map to PropertyTypesWarehouse" in {
    toJson(PropertyTypesWarehouse) should be(jsonWarehouse)
  }

  "PropertyTypeWarehouse" should "map to property type 'warehouse' " in {
    fromJson(jsonWarehouse) should be(JsSuccess(PropertyTypesWarehouse))
  }

  "PropertyTypes reader for 'other' " should "map to PropertyTypesOther" in {
    toJson(PropertyTypesOther) should be(jsonOther)
  }

  "PropertyTypeOther" should "map to property type 'other' " in {
    fromJson(jsonOther) should be(JsSuccess(PropertyTypesOther))
  }

}
