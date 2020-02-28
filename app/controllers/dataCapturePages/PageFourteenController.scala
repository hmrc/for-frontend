/*
 * Copyright 2020 HM Revenue & Customs
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
import form.PageFourteenForm.pageFourteenForm
import javax.inject.Inject
import models._
import models.pages.Summary
import models.serviceContracts.submissions.OtherFactors
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html

class PageFourteenController @Inject() (refNumAction: RefNumAction, cc: MessagesControllerComponents)
  extends ForDataCapturePage[OtherFactors](refNumAction, cc)  {
  val format = otf
  val emptyForm = pageFourteenForm
  val pageNumber: Int = 14

  override def template(form: Form[OtherFactors], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    views.html.part14(form, summary)
  }
}
