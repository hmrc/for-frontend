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
import form.PageFourForm.pageFourForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models.*
import models.pages.*
import play.api.data.Form
import play.api.libs.json.OFormat
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html

class PageFourController @Inject() (
  audit: Audit,
  formDocumentRepository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cc: MessagesControllerComponents,
  part4: views.html.part4
) extends ForDataCapturePage[PageFour](audit, formDocumentRepository, refNumAction, cc) {
  val format: OFormat[PageFour] = p4f
  val emptyForm: Form[PageFour] = pageFourForm
  val pageNumber: Int = 4

  def template(form: Form[PageFour], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html =
    part4(form, summary)
}
