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
import form.PageElevenForm.pageElevenForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models.*
import models.pages.Summary
import models.serviceContracts.submissions.IncentivesAndPayments
import play.api.data.Form
import play.api.libs.json.OFormat
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html

class PageElevenController @Inject() (
  audit: Audit,
  formDocumentRepository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cc: MessagesControllerComponents,
  part11: views.html.part11
) extends ForDataCapturePage[IncentivesAndPayments](audit, formDocumentRepository, refNumAction, cc) {
  val format: OFormat[IncentivesAndPayments] = incf
  val emptyForm: Form[IncentivesAndPayments] = pageElevenForm
  val pageNumber: Int = 11

  def template(form: Form[IncentivesAndPayments], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html =
    part11(form, summary)
}
