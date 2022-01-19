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

import models.serviceContracts.submissions.{OccupierType, OccupierTypeCompany, OccupierTypeIndividuals, OccupierTypeNobody}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import play.api.libs.json.{JsResult, JsSuccess, Json}

class OccupierTypes$Test extends AnyFlatSpec with should.Matchers {

  val jsonIndividual = """"individuals""""
  val jsonCompany = """"company""""
  val jsonNobody = """"nobody""""

  def toJson(data: OccupierType): String = {
    Json.toJson(data).toString
  }

  def fromJson(json: String): JsResult[OccupierType] = {
    Json.fromJson[OccupierType](Json.parse(json))
  }

  "OccupierType reader for 'individual' " should "map to OccupierTypeIndividual" in {
    toJson(OccupierTypeIndividuals) should be(jsonIndividual)
  }

  "OccupierTypeIndividual" should "map to occupier type 'individual' " in {
    fromJson(jsonIndividual) should be(JsSuccess(OccupierTypeIndividuals))
  }

  "OccupierType reader for 'company' " should "map to OccupierTypeCompany" in {
    toJson(OccupierTypeCompany) should be(jsonCompany)
  }

  "OccupierType" should "map to occupier type 'company' " in {
    fromJson(jsonCompany) should be(JsSuccess(OccupierTypeCompany))
  }

  "OccupierType reader for 'nobody' " should "map to OccupierTypeNobody" in {
    toJson(OccupierTypeNobody) should be(jsonNobody)
  }

  "OccupierType" should "map to occupier type 'nobody' " in {
    fromJson(jsonNobody) should be(JsSuccess(OccupierTypeNobody))
  }

}
