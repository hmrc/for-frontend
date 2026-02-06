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

import play.api.Configuration
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.mongo.cache.CacheIdType.SimpleCacheId
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoSessionRepository @Inject() (
  config: Configuration,
  mongo: MongoComponent,
  timestampSupport: TimestampSupport
)(implicit ec: ExecutionContext
) extends MongoCacheRepository(
    mongoComponent = mongo,
    collectionName = "sessionFormData",
    ttl = Duration(config.get[Long]("session.timeoutSeconds"), SECONDS),
    timestampSupport = timestampSupport,
    cacheIdType = SimpleCacheId
  ) {

  def fetchAndGetEntry[T](cacheId: String, key: String)(implicit rds: Reads[T]): Future[Option[T]] =
    get[T](cacheId)(DataKey(key))

  def cache[A](cacheId: String, formKey: String, body: A)(implicit wts: Writes[A]): Future[CacheItem] =
    put(cacheId)(DataKey(formKey), body)

  def removeCache(cacheId: String): Future[Unit] =
    deleteEntity(cacheId)

  def remove(cacheId: String, key: String): Future[Unit] =
    delete(cacheId)(DataKey(key))

}
