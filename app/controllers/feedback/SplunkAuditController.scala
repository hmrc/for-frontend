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

package controllers.feedback

import actions.RefNumAction
import connectors.Audit
import form.persistence.FormDocumentRepository
import models.Addresses
import models.pages.SummaryBuilder
import play.api.libs.json.Json
import play.api.mvc._
import playconfig.SessionId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

/**
 * @author Yuriy Tumakha
 */
@Singleton
class SplunkAuditController @Inject()(
                                    refNumAction: RefNumAction,
                                    documentRepository: FormDocumentRepository,
                                    audit: Audit,
                                    cc: MessagesControllerComponents
                                  )(implicit ec: ExecutionContext) extends FrontendController(cc) {

  def submitPageHelp = refNumAction(parse.formUrlEncoded).async { implicit request =>
    val data: Map[String, Seq[String]] = request.body

    documentRepository.findById(SessionId(hc), request.refNum).map {
      case Some(doc) => Addresses.addressJson(SummaryBuilder.build(doc))
      case None => Json.obj()
    }.map { address =>
      val json = Json.obj(
          "name" -> data("report-name").head,
          "email" -> data("report-email").head,
          "action" -> data("report-action").head,
          "problem" -> data("report-error").head,
          "referer" -> data("referer").head,
          Audit.referenceNumber -> request.refNum
        ) ++ address

      audit.sendExplicitAudit("PageHelp", json)

      Ok(Json.obj("status" -> "OK"))
    }
  }

}
