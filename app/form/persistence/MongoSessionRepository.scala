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

package form.persistence

import java.time.Instant

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes, __}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoSessionRepository @Inject() (mongo: ReactiveMongoComponent, ec: ExecutionContext) {

  val collection: BSONCollection = mongo.mongoConnector.db().collection("sessionFormData")

  val logger = Logger(getClass())

  ensureIndexes() //TODO - Change to standard index definition

  def ensureIndexes(): Future[Unit] = {
    implicit  val executionContext = ec

    val index = Index(Seq("lastUpdated" -> IndexType.Ascending),
      name = Some("lastUpdatedIndex"),options = BSONDocument("expireAfterSeconds" -> 3600)
    )
    collection.indexesManager.ensure(index).recover{
      case ex: Exception => {
        logger.error("Unable to create ttl index on session collection", ex)
        false
      }
    }.map(_ => ())

  }


  def fetchAndGetEntry[T](cacheId: String, key: String)( implicit rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] = {

    val q = Json.obj("_id" -> cacheId)
    val projection = Json.obj(key -> 1)

    collection.find(q, projection = Some(projection)).one[JsObject].map { maybeDocument =>
      maybeDocument
        .flatMap ( document => document.value.get(key))
        .flatMap( x => rds.reads(x).asOpt)
    }

  }

  /*
     {
       _id: "88993-234234-234234-sessionId-cacheId",
       ttl: BSONDate(2019-09-24T10:09:01+0000),
       9999000123: { // formId
         page1: {
           inputFieldName: "value",
           landlordName: "landlordName"
         },
         page2: {
           asdasasda: "asdad"
         }
       },
       9999000321: {
         page1: {
           inputFieldName: "value",
           landlordName: "landlordName"
         },
         page2: {
           asdasasda: "asdad"
         }
       }
      */


  def cache[A](cacheId: String, formId: String, body: A)(implicit wts: Writes[A], executionContext: ExecutionContext): Future[UpdateWriteResult] = {
    val q = Json.obj("_id" -> cacheId)

    val u = Json.obj(
      "$set" -> Json.obj(
        "lastUpdated" -> Instant.now(),
        formId -> wts.writes(body)
      )
    )
    collection.update(false).one(q,u, upsert = true)
  }

  def removeCache(cacheId: String) = {
    val q = Json.obj("_id" -> cacheId)
    implicit val _ec = ec
    collection.delete(false).one(q, limit = Option(1)).map(_ => ())
  }


  implicit val instantTimeRead: Reads[Instant] =
    (__ \ "$date").read[Long].map { dateTime =>
      Instant.ofEpochMilli(dateTime)
    }

  implicit val instantTimeWrite: Writes[Instant] = new Writes[Instant] {
    override def writes(instant: Instant): JsValue = Json.obj(
      "$date" -> ((instant.getEpochSecond * 1000L) + (instant.getNano / 1000000L))
    )
  }



}
