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

package controllers

import actions.RefNumAction
import config.AppConfig
import connectors.Audit
import form.Errors
import form.persistence.FormDocumentRepository
import models.Addresses

import javax.inject.{Inject, Singleton}
import models.pages.*
import play.api.data.Form
import play.api.data.Forms.*
import play.api.libs.json.Json
import play.api.mvc.*
import config.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class ApplicationController @Inject() (
  cc: MessagesControllerComponents,
  refNumAction: RefNumAction,
  repository: FormDocumentRepository,
  checkYourAnswersView: views.html.checkYourAnswers,
  declarationView: views.html.declaration,
  errorView: views.html.error.error,
  indexView: views.html.index,
  sessionTimeoutView: views.html.sessionTimeout,
  importantInformationView: views.html.importantInformation,
  audit: Audit,
  appConfig: AppConfig
)(using ec: ExecutionContext
) extends FrontendController(cc):

  private def updatePath(hc: HeaderCarrier, path: String): HeaderCarrier =
    val otherHeaders = hc.otherHeaders.map(x => if x._1 == "path" then ("path", path) else x)
    hc.copy(otherHeaders = otherHeaders)

  def declaration: Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)

        val json = Json.obj(Audit.referenceNumber -> request.refNum) ++ Addresses.addressJson(summary)
        audit.sendExplicitAudit("ContinueNextPage", json)(
          using updatePath(implicitly[HeaderCarrier], "/sending-rental-information/check-your-answers"),
          ec
        )

        Ok(declarationView(Form(("", text)), summary))
      case None      => NotFound(errorView(404))
    }
  }

  def declarationError: Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).map {
      case Some(doc) =>
        val summary = SummaryBuilder.build(doc)
        Ok(declarationView(Form(("", text)).withError("declaration", Errors.declaration), summary))
      case None      => NotFound(errorView(404))
    }
  }

  def startAgain: Action[AnyContent] = refNumAction.async { implicit request =>
    repository.clear(SessionId(using hc), request.refNum) map { _ =>
      Redirect(dataCapturePages.routes.PageController.showPage(0))
    }
  }

  def index: Action[AnyContent] = Action { implicit request =>
    if appConfig.startPageRedirect then
      Redirect(appConfig.govukStartPage)
    else
      Ok(indexView())
  }

  def sessionTimeout: Action[AnyContent] = Action(implicit request => Ok(sessionTimeoutView()))

  def error404: Action[AnyContent] = Action { implicit request =>
    Ok(errorView(404))
  }

  def error408: Action[AnyContent] = Action { implicit request =>
    Ok(errorView(408))
  }

  def error409: Action[AnyContent] = Action { implicit request =>
    Ok(errorView(409))
  }

  def error410: Action[AnyContent] = Action { implicit request =>
    Ok(errorView(410))
  }

  def error500: Action[AnyContent] = Action { implicit request =>
    Ok(errorView(500))
  }

  def checkYourAnswers: Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(using hc), request.refNum).map {
      case Some(doc) =>
        val sub = SummaryBuilder.build(doc)
        Ok(checkYourAnswersView(sub))
      case None      =>
        NotFound(errorView(404))
    }
  }

  def importantInformation: Action[AnyContent] = Action { implicit request =>
    if appConfig.getBoolean("notificationBanner.enabled") then
      Ok(importantInformationView())
    else
      Redirect(routes.LoginController.show)
  }
