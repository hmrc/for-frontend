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

package form.persistence

import form.persistence.MongoSessionRepositorySpecData.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.mongo.cache.CacheItem
import uk.gov.hmrc.vo.unit.test.db.MongoDBAppSpec

import java.util.UUID

class MongoSessionRepositorySpec extends MongoDBAppSpec[CacheItem, MongoSessionRepository]:

  "MongoSessionRepository" should {
    "store data in mongo" in {
      val cacheId    = UUID.randomUUID().toString
      val formId     = "formId"
      val testObject = MongoSessionRepositorySpecData(name = "John", buildingNumber = 100)

      mongoRepository.cache(cacheId, formId, testObject)(using format).futureValue

      val res = mongoRepository.fetchAndGetEntry[MongoSessionRepositorySpecData](cacheId, formId)(using format).futureValue
      res.get shouldBe testObject
    }

    "store multiple pages in mongo and do not affect other" in {
      val cacheId     = UUID.randomUUID().toString
      val page1       = "page1"
      val page2       = "page2"
      val testObject1 = MongoSessionRepositorySpecData(name = "John", buildingNumber = 100)
      val testObject2 = MongoSessionRepositorySpecData(name = "Peter", buildingNumber = -200)

      mongoRepository.cache(cacheId, page1, testObject1)(using format).futureValue
      mongoRepository.cache(cacheId, page2, testObject2)(using format).futureValue

      val testObjectFromDatabase1 = mongoRepository.fetchAndGetEntry[MongoSessionRepositorySpecData](cacheId, page1)(using format).futureValue
      testObjectFromDatabase1.get shouldBe testObject1

      val testObjectFromDatabase2 = mongoRepository.fetchAndGetEntry[MongoSessionRepositorySpecData](cacheId, page2)(using format).futureValue
      testObjectFromDatabase2.get shouldBe testObject2
    }
  }

case class MongoSessionRepositorySpecData(name: String, buildingNumber: Int)

object MongoSessionRepositorySpecData:
  implicit val format: OFormat[MongoSessionRepositorySpecData] = Json.format
