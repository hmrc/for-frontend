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

package connectors

import models.serviceContracts.submissions.Address
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

case class Document(referenceNumber: String, journeyStarted: DateTime,  pages: Seq[Page] = Seq(), address: Option[Address] = None,
                    saveForLaterPassword: Option[String] = None, journeyResumptions: Seq[DateTime] = Seq.empty) {
  def page(pageNumber: Int): Option[Page] = pages.find(_.pageNumber == pageNumber)

  def add(page: Page): Document = {
    val newPages = (pages.filterNot(_.pageNumber == page.pageNumber) :+ page).sortBy(_.pageNumber)
    this.copy(pages =  newPages)
  }
}

object Document {
  implicit val formats = Json.format[Document]
}
