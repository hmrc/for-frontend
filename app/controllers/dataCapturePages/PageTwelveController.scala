/*
 * Copyright 2022 HM Revenue & Customs
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
import form.PageTwelveForm.pageTwelveForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models._
import models.pages.{PageTwelve, Summary}
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import views.html.part12

class PageTwelveController @Inject() (audit: Audit,
                                      formDocumentRepository: FormDocumentRepository,
                                      refNumAction: RefNumAction,
                                      cc: MessagesControllerComponents,
                                      part12:part12)
  extends ForDataCapturePage[PageTwelve](audit, formDocumentRepository, refNumAction, cc) {
  val format = p12f
  val emptyForm = pageTwelveForm
  val pageNumber: Int = 12

  def template(form: Form[PageTwelve], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    part12(form, summary)
  }
}
