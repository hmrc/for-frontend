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
import form.PageFiveForm.pageFiveForm
import form.persistence.FormDocumentRepository
import models.*
import models.pages.{PageFive, Summary}
import play.api.data.Form
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import views.html.part5

import javax.inject.Inject

class PageFiveController @Inject() (
  audit: Audit,
  formDocumentRepository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cc: MessagesControllerComponents,
  part5: part5
) extends ForDataCapturePage[PageFive](audit, formDocumentRepository, refNumAction, cc) {
  val format: OFormat[PageFive] = Json.format
  val emptyForm: Form[PageFive] = pageFiveForm
  val pageNumber: Int           = 5

  def template(form: Form[PageFive], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html =
    part5(form, summary)
}
