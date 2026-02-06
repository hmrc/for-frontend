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

package security

import connectors.Document
import models.FORLoginResponse
import models.serviceContracts.submissions.Address
import useCases.ReferenceNumber
import useCases.SaveInProgressSubmissionForLater.UpdateDocumentInCurrentSession

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime

object LoginToHOD {
  type Postcode                  = String
  type StartTime                 = ZonedDateTime
  type AuthToken                 = String
  type LoginToHOD                = (ReferenceNumber, Postcode, StartTime) => Future[LoginResult]
  type VerifyCredentials         = (ReferenceNumber, Postcode) => Future[FORLoginResponse]
  type LoadSavedForLaterDocument = (AuthToken, ReferenceNumber) => Future[Option[Document]]

  def apply(
    v: VerifyCredentials,
    l: LoadSavedForLaterDocument,
    u: UpdateDocumentInCurrentSession
  )(
    referenceNumber: ReferenceNumber,
    pc: Postcode,
    st: StartTime
  )(implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[LoginResult] =
    for {
      lr <- v(referenceNumber, pc)
      _  <- u(hc, referenceNumber, doc(referenceNumber, lr.address, st))
      sd <- l(lr.forAuthToken, referenceNumber)
    } yield sd map { _ => dps(lr.forAuthToken, lr.address) } getOrElse ned(lr.forAuthToken, lr.address)

  private def doc(r: ReferenceNumber, a: Address, d: StartTime) = Document(r, d, address = Some(a))
  private def dps                                               = DocumentPreviouslySaved.apply
  private def ned                                               = NoExistingDocument.apply
}

sealed trait LoginResult
case class DocumentPreviouslySaved(token: String, address: Address) extends LoginResult
case class NoExistingDocument(token: String, address: Address) extends LoginResult
