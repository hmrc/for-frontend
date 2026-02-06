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
import form.PageThreeForm.pageThreeForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models.*
import models.pages.{PageThree, Summary}
import play.api.data.Form
import play.api.libs.json.OFormat
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html

class PageThreeController @Inject() (
  audit: Audit,
  formDocumentRepository: FormDocumentRepository,
  refNumAction: RefNumAction,
  cc: MessagesControllerComponents,
  part3: views.html.part3
) extends ForDataCapturePage[PageThree](audit, formDocumentRepository, refNumAction, cc) {
  val format: OFormat[PageThree] = p3f
  val emptyForm: Form[PageThree] = pageThreeForm
  val pageNumber: Int            = 3

  def template(form: Form[PageThree], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html =
    part3(form, summary)

}
