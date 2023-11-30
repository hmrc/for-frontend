/*
 * Copyright 2023 HM Revenue & Customs
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

package security

import connectors.Document
import controllers.toFut
import models.FORLoginResponse
import models.serviceContracts.submissions.Address
import useCases.ReferenceNumber
import useCases.SaveInProgressSubmissionForLater.UpdateDocumentInCurrentSession

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime

object LoginToHOD {
  type Ref1 = String
  type Ref2 = String
  type Postcode = String
  type StartTime = ZonedDateTime
  type SessionID = String
  type AuthToken = String
  type LoginToHOD = (Ref1, Ref2, Postcode, StartTime) => Future[LoginResult]
  type VerifyCredentials = (ReferenceNumber, Postcode) => Future[FORLoginResponse]
  type LoadSavedForLaterDocument = (AuthToken, ReferenceNumber) => Future[Option[Document]]
  type StoreDocumentWithCredentialsInSession = (FORLoginResponse, ReferenceNumber) => Future[Unit]

  def apply(v: VerifyCredentials, l: LoadSavedForLaterDocument, u: UpdateDocumentInCurrentSession)
           (r1: Ref1, r2: Ref2, pc: Postcode, st: StartTime)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LoginResult] =
    for {
      rn <- ref(r1, r2)
      lr <- v(rn, pc)
      _ <- u(hc, rn, doc(rn, lr.address, st))
      sd <- l(lr.forAuthToken, rn)
    } yield sd map { dps(_, lr.forAuthToken, lr.address) } getOrElse ned(lr.forAuthToken, lr.address)

  private def ref(r1: Ref1, r2: Ref2): Future[String] = s"$r1$r2"
  private def doc(r: ReferenceNumber, a: Address, d: StartTime) = Document(r, d, address = Some(a))
  private def dps = DocumentPreviouslySaved.apply _
  private def ned = NoExistingDocument.apply _
}

sealed trait LoginResult
case class DocumentPreviouslySaved(doc: Document, token: String, address: Address) extends LoginResult
case class NoExistingDocument(token: String, address: Address) extends LoginResult
