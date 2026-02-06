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

package utils.stubs

import com.google.inject.Provider
import connectors._
import form.persistence.FormDocumentRepository
import helpers.AddressAuditing
import models.pages.Summary
import models.serviceContracts.submissions.{NotConnectedSubmission, Submission}
import org.scalatest.matchers.should
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import useCases.SubmissionBuilder

import scala.concurrent.Future

object StubSubmissionConnector { def apply() = new StubSubmissionConnector }

class StubSubmissionConnector extends SubmissionConnector with should.Matchers {
  var submissions: Map[String, Submission] = Map.empty

  def submit(refNum: String, submission: Submission)(implicit hc: HeaderCarrier): Future[Unit] = {
    submissions = submissions.updated(refNum, submission)
    Future.unit
  }

  def verifyWasSubmitted(refNum: String, sub: Submission): Unit =
    submissions.get(refNum) match {
      case None    => fail(s"No submission for $refNum")
      case Some(x) => assert(x === sub)
    }

  override def submitNotConnected(refNumber: String, submission: NotConnectedSubmission)(implicit hc: HeaderCarrier): Future[Unit] = ???
}

object StubSubmissionBuilder { def apply() = new StubSubmissionBuilder() }

class StubSubmissionBuilder extends SubmissionBuilder {
  private var stubs: Map[Document, Submission] = Map.empty

  def stubBuild(doc: Document, sub: Submission): Unit = stubs = stubs.updated(doc, sub)

  def build(doc: Document): Submission = stubs.getOrElse(doc, throw new Exception(s"No stub for $doc. Stubs: $stubs"))
}

class StubFormDocumentRepoProvider extends Provider[StubFormDocumentRepo] {
  override def get(): StubFormDocumentRepo = StubFormDocumentRepo()
}

case class StubFormDocumentRepo(docs: (String, String, Document)*) extends FormDocumentRepository {

  override def findById(documentId: String, referenceNumber: String): Future[Option[Document]] = {
    val doc = docs.find(d => d._1 == documentId && d._2 == referenceNumber).map(_._3)
    Future.successful(doc)
  }

  var storedPages: Seq[(String, String, Page)] = Seq.empty

  override def updatePage(documentId: String, referenceNumber: String, page: Page): Future[Unit] = {
    storedPages = storedPages :+ ((documentId, referenceNumber, page))
    Future.unit
  }

  override def store(documentId: String, referenceNumber: String, doc: Document): Future[Unit] = ???

  override def clear(documentId: String, referenceNumber: String): Future[Unit] = ???

  override def remove(documentId: String): Future[Unit] = Future.successful(())
}

object StubAddressAuditing extends AddressAuditing(null) {
  override def apply(s: Summary, r: Request[?]): Future[Unit] = Future.successful(())

}
