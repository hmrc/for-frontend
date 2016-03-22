/*
 * Copyright 2016 HM Revenue & Customs
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

import form.PageEightForm.pageEightForm
import models._
import models.pages.Summary
import models.serviceContracts.submissions.{RentAgreement, UserTypeOccupiersAgent, UserTypeOwnersAgent}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html

object PageEightController extends ForDataCapturePage[RentAgreement] {
  val format = raf
  val emptyForm = pageEightForm
  val pageNumber: Int = 8

  def template(form: Form[RentAgreement], summary: Summary)(implicit request: Request[AnyContent]): Html = {
    views.html.part8(form, summary)
  }
}