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

package useCases

import connectors.Document
import models.journeys.TargetPage
import models.pages.Summary
import security.LoginToHOD.LoadSavedForLaterDocument
import uk.gov.hmrc.http.HeaderCarrier
import useCases.SaveInProgressSubmissionForLater.UpdateDocumentInCurrentSession

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}

object ContinueWithSavedSubmission {
  type ContinueWithSavedSubmission = (SaveForLaterPassword, ReferenceNumber) => Future[SaveForLaterLoginResult]
  type BuildSummary = Document => Summary
  type GetNextPageOfJourney = Summary => TargetPage

  def apply(l: LoadSavedForLaterDocument, u: UpdateDocumentInCurrentSession, b: BuildSummary, j: GetNextPageOfJourney, n: Now)
           (p: SaveForLaterPassword, r: ReferenceNumber)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SaveForLaterLoginResult] =
    l(auth, r) map {
      case Some(doc) if matches(doc.saveForLaterPassword, p) => u(hc, r, record(doc, n())); PasswordsMatch(j(b(doc)))
      case Some(_) => IncorrectPassword
      case None => ErrorRetrievingSavedDocument
    }

  private def auth(implicit hc: HeaderCarrier) = hc.authorization.map(_.value).getOrElse(throw AuthorizationTokenMissing)

  private def matches(p1: Option[SaveForLaterPassword], p2: SaveForLaterPassword) = p1.contains(p2)

  private def record(d: Document, n: ZonedDateTime) = d.copy(journeyResumptions = d.journeyResumptions :+ n)
}

sealed trait SaveForLaterLoginResult

case class PasswordsMatch(pageToGoTo: TargetPage) extends SaveForLaterLoginResult

case object IncorrectPassword extends SaveForLaterLoginResult

case object ErrorRetrievingSavedDocument extends SaveForLaterLoginResult

case object AuthorizationTokenMissing extends Exception
