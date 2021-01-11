/*
 * Copyright 2021 HM Revenue & Customs
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
import form.PageEightForm.pageEightForm
import javax.inject.Inject
import models._
import models.pages.Summary
import models.serviceContracts.submissions.RentAgreement
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html

class PageEightController @Inject() (refNumAction: RefNumAction,
                                     cc: MessagesControllerComponents,
                                    part8: views.html.part8)
  extends ForDataCapturePage[RentAgreement] (refNumAction, cc) {
  val format = raf
  val emptyForm = pageEightForm
  val pageNumber: Int = 8

  def template(form: Form[RentAgreement], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    part8(form, summary)
  }
}
