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

package controllers.dataCapturePages

import actions.{RefNumAction, RefNumRequest}
import connectors.Audit
import form.PageSevenForm.pageSevenForm
import form.PageSixForm.pageSixForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models.*
import models.pages.{PageSeven, PageSix, Summary}
import models.serviceContracts.submissions.LeaseAgreementTypesVerbal
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import config.SessionId
import play.api.libs.json.OFormat

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PageSevenController @Inject() (
  audit: Audit,
  formDocumentRepository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cc: MessagesControllerComponents,
  part7: views.html.part7
) extends ForDataCapturePage[PageSeven](audit, formDocumentRepository, refNumAction, cc) {
  val format: OFormat[PageSeven] = p7f
  val emptyForm: Form[PageSeven] = pageSevenForm
  val pageNumber: Int            = 7

  def template(form: Form[PageSeven], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    val updatedForm: Form[PageSeven] = Await.result(
      repository.findById(SessionId(using hc), request.refNum).map { docOpt =>
        (for {
          doc                <- docOpt
          page6              <- doc.page(6)
          pageSix            <- pageSixForm.bindFromRequest(page6.fields).value
          agreementStartDate <- getAgreementStartDate(pageSix)
        } yield form.copy(data = form.data + ("agreementStartDate" -> agreementStartDate.toString))).getOrElse(form)
      },
      20 seconds
    )

    part7(updatedForm, summary)
  }

  private def getAgreementStartDate(pageSix: PageSix): Option[LocalDate] =
    if (pageSix.leaseAgreementType == LeaseAgreementTypesVerbal) {
      pageSix.verbalAgreementDetails.startDate.map(_.toLocalDate)
    } else {
      pageSix.writtenAgreementDetails.map(_.startDate.toLocalDate)
    }

}
