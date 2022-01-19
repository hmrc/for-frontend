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
import controllers.dataCapturePages.ForDataCapturePage.FormAction
import form.PageZeroForm.pageZeroForm
import form.persistence.FormDocumentRepository

import javax.inject.Inject
import models._
import models.pages.Summary
import models.serviceContracts.submissions.{AddressConnectionType, AddressConnectionTypeNo}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import views.html.part0

class PageZeroController @Inject() (audit: Audit,
                                    formDocumentRepository: FormDocumentRepository,
                                    refNumAction: RefNumAction,
                                    cc: MessagesControllerComponents,
                                    part0: part0)
  extends ForDataCapturePage[AddressConnectionType](audit, formDocumentRepository, refNumAction, cc) {

  override implicit val format: Format[AddressConnectionType] = formatAddressConnection

  val emptyForm = pageZeroForm
  val pageNumber: Int = 0

  override def template(form: Form[AddressConnectionType], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    part0(form, summary)
  }

  override def goToNextPage(action: FormAction, summary: Summary, savedFields: Map[String, Seq[String]])(implicit request: RefNumRequest[AnyContent]) = {
    action match {
      case ForDataCapturePage.Continue => summary.addressConnection match {
        case Some(AddressConnectionTypeNo) => Redirect(controllers.routes.PreviouslyConnectedController.onPageView)
        case _ => super.goToNextPage(action, summary, savedFields)
       }
      case _ => super.goToNextPage(action, summary, savedFields)
    }
  }
}