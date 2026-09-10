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

import models.serviceContracts.submissions.OccupierType
import play.api.libs.json.{JsResult, JsSuccess, Json}
import uk.gov.hmrc.vo.unit.test.BaseSpec

class OccupierTypeSpec extends BaseSpec:

  private val jsonIndividual = """"individuals""""
  private val jsonCompany    = """"company""""
  private val jsonNobody     = """"nobody""""

  private def toJson(data: OccupierType): String =
    Json.toJson(data).toString

  private def fromJson(json: String): JsResult[OccupierType] =
    Json.fromJson[OccupierType](Json.parse(json))

  "OccupierType reader for 'individual' " should {
    "map to OccupierTypeIndividual" in {
      toJson(OccupierType.individuals) shouldBe jsonIndividual
    }
  }

  "OccupierTypeIndividual" should {
    "map to occupier type 'individual' " in {
      fromJson(jsonIndividual) shouldBe JsSuccess(OccupierType.individuals)
    }
  }

  "OccupierType reader for 'company' " should {
    "map to OccupierTypeCompany" in {
      toJson(OccupierType.company) shouldBe jsonCompany
    }
  }

  "OccupierType" should {
    "map to occupier type 'company' " in {
      fromJson(jsonCompany) shouldBe JsSuccess(OccupierType.company)
    }
  }

  "OccupierType reader for 'nobody' " should {
    "map to OccupierTypeNobody" in {
      toJson(OccupierType.nobody) shouldBe jsonNobody
    }
  }

  "OccupierType" should {
    "map to occupier type 'nobody' " in {
      fromJson(jsonNobody) shouldBe JsSuccess(OccupierType.nobody)
    }
  }
