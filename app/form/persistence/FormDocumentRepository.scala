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

import java.util.Base64

import com.google.inject.ImplementedBy
import connectors.{Document, Page}
import controllers.toFut
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SessionScopedFormDocumentRepository])
trait FormDocumentRepository {
  def findById(documentId: String, referenceNumber: String): Future[Option[Document]]
  def updatePage(documentId: String, referenceNumber: String, page: Page): Future[Unit]
  def store(documentId: String, referenceNumber: String, doc: Document): Future[Unit]
  def clear(documentId: String, referenceNumber: String): Future[Unit]
  def remove(documentId: String): Future[Unit]
}

@Singleton
class SessionScopedFormDocumentRepository @Inject() (cache: MongoSessionRepository)(implicit ec: ExecutionContext)
  extends FormDocumentRepository {

  override def findById(documentId: String, referenceNumber: String): Future[Option[Document]] = {
    cache.fetchAndGetEntry[DocumentWrapper](documentId, referenceNumber).flatMap {
      case Some(dw) =>
        val doc = Json.parse(Base64.getDecoder.decode(dw.b64JsonBlob.getBytes("UTF-8"))).as[Document]
        Some(addBackDotsIntoKeyNames(doc))
      case None => None
    } recoverWith {
      case _ =>
        cache.fetchAndGetEntry[Document](documentId, referenceNumber).map {
          case Some(doc) => Some(addBackDotsIntoKeyNames(doc))
          case None => None
        }
    }
  }

  private def addBackDotsIntoKeyNames(doc: Document) = doc.copy(
    pages = doc.pages.map { p =>  p.copy(fields = p.fields.map(kv => (kv._1.replace("___", "."), kv._2)))  }
  )

  override def updatePage(documentId: String, referenceNumber: String, page: Page): Future[Unit] = {
    findById(documentId, referenceNumber) flatMap {
      case Some(doc) => store(documentId, referenceNumber, doc.add(page))
      case None => ()
    }
  }

  private def stripDotsOutOfKeyNamesToAppeaseMongo(doc: Document) = doc.copy(
    pages = doc.pages.map { p =>  p.copy(fields = p.fields.map(kv => (kv._1.replace(".", "___"), kv._2)))  }
  )

  override def store(documentId: String, referenceNumber: String, doc: Document): Future[Unit] = {
    val dw = DocumentWrapper(toB64Blob(stripDotsOutOfKeyNamesToAppeaseMongo(doc)))
    cache.cache[DocumentWrapper](documentId, referenceNumber, dw) recoverWith {
      case e: Exception =>
        Future.failed(e)
    } map { _ => () }
  }

  // Important: B64 the json string because the json encryptor dramatically multiplies the document size when containing UTF-8 causing 413 in caching client
  private def toB64Blob(doc: Document) = new String(Base64.getEncoder.encode(Json.stringify(Json.toJson(doc)).getBytes("UTF-8")))

  override def clear(documentId: String, referenceNumber: String): Future[Unit] = {
    findById(documentId, referenceNumber) flatMap {
      case Some(doc) => store(documentId, referenceNumber, doc.copy(pages = Seq.empty))
      case None => ()
    }
  }

  override def remove(documentId: String): Future[Unit] = {
    cache.removeCache(documentId)
  }
}

object DocumentWrapper {
  implicit val f: Format[DocumentWrapper] = Json.format[DocumentWrapper]
}
case class DocumentWrapper(b64JsonBlob: String)
