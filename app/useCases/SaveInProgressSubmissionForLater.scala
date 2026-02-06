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

package useCases

import connectors.{Document, HODConnector}
import config.SessionId
import form.persistence.FormDocumentRepository
import security.LoginToHOD.AuthToken
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import java.security.SecureRandom
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object SaveInProgressSubmissionForLater {
  type SaveInProgressSubmissionForLater = HeaderCarrier => (Document, HeaderCarrier) => Future[SaveForLaterPassword]
  type UpdateDocumentInCurrentSession   = (HeaderCarrier, ReferenceNumber, Document) => Future[Unit]
  type GenerateSaveForLaterPassword     = () => String
  type SaveForLaterPassword             = String
  type StoreInProgressSubmission        = Document => Future[Unit]

  def apply(
    gp: GenerateSaveForLaterPassword,
    s: StoreInProgressSubmission,
    u: UpdateDocumentInCurrentSession
  )(
    d: Document,
    hc: HeaderCarrier
  )(implicit ec: ExecutionContext
  ): Future[String] = {
    val p  = d.saveForLaterPassword getOrElse gp()
    val nd = d.copy(saveForLaterPassword = Some(p))
    s(nd) map { _ => u(hc, d.referenceNumber, nd) } map { _ => p }
  }

  def apply(
    p: SaveForLaterPassword,
    s: StoreInProgressSubmission,
    u: UpdateDocumentInCurrentSession
  )(
    d: Document,
    hc: HeaderCarrier
  )(implicit ec: ExecutionContext
  ): Future[String] = {
    val nd = d.copy(saveForLaterPassword = Some(p))
    s(nd) map { _ => u(hc, d.referenceNumber, nd) } map { _ => p }
  }
}

object StoreInProgressSubmissionFor90Days {
  def apply(d: Document)(implicit hc: HeaderCarrier, hodConnector: HODConnector): Future[Unit] = hodConnector.saveForLater(d)
}

object LoadSavedForLaterDocument {

  def apply(a: AuthToken, r: ReferenceNumber)(implicit hc: HeaderCarrier, hodConnector: HODConnector): Future[Option[Document]] =
    hodConnector.loadSavedDocument(r)(using hc.copy(authorization = Some(Authorization(a))))
}

object UpdateDocumentInCurrentSession {

  def apply(h: HeaderCarrier, r: ReferenceNumber, d: Document)(implicit formDocumentRepository: FormDocumentRepository): Future[Unit] =
    formDocumentRepository.store(SessionId(using h), r, d)
}

object Generate7LengthLowercaseAlphaNumPassword {
  private val validDigits        = 2 to 9
  private val validChars         = "abcdefghjkmnpqrstuvwxyz".toCharArray
  private val allValid: Seq[Any] = validDigits ++ validChars

  def apply(): SaveForLaterPassword = (1 to 7).map(_ => new Random(new SecureRandom()).shuffle(allValid).head).mkString
}

object UseUserAlphaNumPassword {
  def apply(password: String): SaveForLaterPassword = password
}
