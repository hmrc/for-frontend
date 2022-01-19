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

import connectors.{Document, Page}
import controllers.toFut
import models.pages.{Summary, SummaryBuilder}

import scala.concurrent.{ExecutionContext, Future}

class SaveFormInRepository(repository: FormDocumentRepository, builder: SummaryBuilder)(implicit ec: ExecutionContext) extends SaveForm {

  def apply(formData: Option[Map[String, Seq[String]]], sessionId: String, referenceNumber: String, pageNumber: Int):
    Future[Option[(Map[String, Seq[String]], Summary)]] =
    repository.findById(sessionId, referenceNumber) flatMap {
      case Some(doc) =>
        save(doc, formData, pageNumber, sessionId, referenceNumber) flatMap {
          case Some((savedFields, page)) => Some((savedFields, builder.build(doc.add(page))))
          case None => None
        }
      case None => None
    }

  private def save(doc: Document, formData: Option[Map[String, Seq[String]]], pageNumber: Int, sessionId: String, refNum: String) =
    formData map { fields =>
      val nonEmptyFields = fields.filterNot(x => x._2.isEmpty || x._2.head.isEmpty || x._1 == "csrfToken") // when JS is not enabled lots of empty fields will be passed in
      val trimmed = nonEmptyFields.map(x => (x._1, x._2.map(_.trim))) // users frequently complain about leading and trailling whitespace causing validation errors
      val page = Page(pageNumber, trimmed)
      repository.updatePage(sessionId, refNum, page).map { _ => Some((trimmed, page))}
    } getOrElse Future.successful(None)

}

trait SaveForm {
  def apply(formData: Option[Map[String, Seq[String]]], sessionId: String, referenceNumber: String, pageNumber: Int):
    Future[Option[(Map[String, Seq[String]], Summary)]]
}
