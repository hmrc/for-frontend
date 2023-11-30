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

package playconfig

import com.google.inject.ImplementedBy
import connectors.HODConnector
import form.persistence.FormDocumentRepository

import javax.inject.{Inject, Singleton}
import models.journeys.Journey
import models.pages.SummaryBuilder
import security.LoginToHOD._
import uk.gov.hmrc.http._
import useCases.ContinueWithSavedSubmission.ContinueWithSavedSubmission
import useCases.SaveInProgressSubmissionForLater.SaveInProgressSubmissionForLater
import useCases._
import util.DateUtil.nowInUK

import scala.concurrent.ExecutionContext


object SessionId {
  def apply(implicit hc: HeaderCarrier): String = hc.sessionId.map(_.value).getOrElse(throw SessionIdMissing())
}
case class SessionIdMissing() extends Exception

object SaveForLater {
  def apply()(implicit ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    Generate7LengthLowercaseAlphaNumPassword(), StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
  def apply(pwd: String)(implicit ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): SaveInProgressSubmissionForLater = implicit hc => SaveInProgressSubmissionForLater(
    UseUserAlphaNumPassword(pwd), StoreInProgressSubmissionFor90Days.apply _,
    UpdateDocumentInCurrentSession.apply _
  )
}

object ContinueWithSavedSubmission {
  def apply()(implicit hc: HeaderCarrier, ec: ExecutionContext, hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository): ContinueWithSavedSubmission = useCases.ContinueWithSavedSubmission(
    LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply,
    SummaryBuilder.build, Journey.pageToResumeAt, () => nowInUK
  )
}


/**
 * Temporal solution before we move all login logic to separate service class.
 * This allow us to test login controller without starting google guice.
 */
@ImplementedBy(classOf[DefaultLoginToHodAction])
trait LoginToHODAction {
  def apply(implicit hc: HeaderCarrier, ec: ExecutionContext): LoginToHOD
}

@Singleton
class DefaultLoginToHodAction @Inject() (implicit hodConnector: HODConnector, formDocumentRepository: FormDocumentRepository) extends LoginToHODAction {

  override def apply(implicit hc: HeaderCarrier, ec: ExecutionContext): LoginToHOD = security.LoginToHOD(
    hodConnector.verifyCredentials, LoadSavedForLaterDocument.apply, UpdateDocumentInCurrentSession.apply
  )
}
