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
import connectors._
import controllers._
import controllers.dataCapturePages.ForDataCapturePage._
import form._
import form.persistence.{BuildForm, FormDocumentRepository, SaveForm, SaveFormInRepository}
import models.Addresses
import models.journeys._
import models.pages.{Summary, SummaryBuilder}
import play.api.Logging
import play.api.data.Form
import play.api.libs.json.{Format, JsObject, Json}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.shaded.ahc.io.netty.handler.codec.http.QueryStringDecoder
import play.twirl.api.Html
import playconfig.SessionId
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

abstract class ForDataCapturePage[T] (audit: Audit,
                                      formDocumentRepository: FormDocumentRepository,
                                      refNumAction: RefNumAction,
                                      override val controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with Logging {

  implicit val format: Format[T]
  implicit val ec: ExecutionContext = controllerComponents.executionContext

  def emptyForm: Form[T]

  def pageNumber: Int

  def saveForm: SaveForm = new SaveFormInRepository(formDocumentRepository, SummaryBuilder)

  def repository: FormDocumentRepository = formDocumentRepository

  def template(form: Form[T], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html

  def show: Action[AnyContent] = refNumAction.async { implicit request =>
    repository.findById(SessionId(hc), request.refNum) flatMap {
      case Some(doc) => showThisPageOrGoToNextAllowed(doc, request)
      case None =>
        logger.error(s"Could not find document in current session - ${request.refNum} - ${hc.sessionId}")
        throw new BadRequestException("Could not find document in current session")
    }
  }

  private def showThisPageOrGoToNextAllowed(doc: Document, request: RefNumRequest[AnyContent]): Future[Result] = {
    val sub = SummaryBuilder.build(doc)
    Journey.nextPageAllowable(pageNumber, sub) match {
      case PageToGoTo(page) if isThisPage(page) => displayForm(BuildForm(doc, page, emptyForm), sub, request)
      case p => RedirectTo(p, request.headers)
    }
  }

  private def isThisPage(page: Int) = page == pageNumber

  def save: Action[AnyContent] = refNumAction.async { implicit request: RefNumRequest[AnyContent] =>
    saveForm(request.body.asFormUrlEncoded, SessionId(hc), request.refNum, pageNumber) flatMap {
      case Some((savedFields, summary)) => goToNextPage(extractAction(request.body.asFormUrlEncoded), summary, savedFields)
      case None => throw new BadRequestException("go to error page")
    }
  }

  def goToNextPage(action: FormAction, summary: Summary, savedFields: Map[String, Seq[String]])
    (implicit request: RefNumRequest[AnyContent]) = {
    action match {
      case controllers.dataCapturePages.ForDataCapturePage.Continue => bindForm(savedFields).fold(
        formWithErrors => displayForm(formWithErrors, summary, request),
        pageData => {
          auditFormSubmission(pageData, summary)
          getPage(pageNumber + 1, summary, request)
        }
      )
      case controllers.dataCapturePages.ForDataCapturePage.Update => bindForm(savedFields).fold(
        formWithErrors => displayForm(formWithErrors, summary, request),
        pageData => {
          auditFormSubmission(pageData, summary)
          RedirectTo(Journey.pageToResumeAt(summary), request.headers)
        }
      )
      case controllers.dataCapturePages.ForDataCapturePage.Save => Redirect(controllers.routes.SaveForLaterController.saveForLater(request.path)) //TODO capture page number
      case controllers.dataCapturePages.ForDataCapturePage.Back => getPage(pageNumber - 1, summary, request)
      case controllers.dataCapturePages.ForDataCapturePage.Unknown => redirectToPage(pageNumber)
    }
  }

  private def auditFormSubmission(formData: T, summary: Summary)(implicit request: RefNumRequest[AnyContent]): Unit = { //TODO maybe future?? or fire&forget
    //Get only form data, not additional POST data like CSRF token.
    val json = Json.toJson(emptyForm.fill(formData).data).as[JsObject] ++
      Json.obj(Audit.referenceNumber -> request.refNum) ++ Addresses.addressJson(summary)

    println(Json.prettyPrint(json))

    audit.sendExplicitAudit("ContinueNextPage", json)
  }

  private def bindForm(requestData: Map[String, Seq[String]]) = emptyForm.bindFromRequest(requestData).convertGlobalToFieldErrors()

  private def displayForm(form: Form[T], summary: Summary, request: RefNumRequest[AnyContent]) =
    request.flash.get(SaveForLaterController.s4lIndicator) match {
      case Some(_) => Ok(template(form.copy(errors = Seq.empty), summary)(request))
      case _ if form.hasErrors => BadRequest(template(form, summary)(request))
      case _ => Ok(template(form, summary)(request))
    }

  private def getPage(nextPage: Int, summary: Summary, request: RefNumRequest[AnyContent]) = {
    val p = Journey.nextPageAllowable(nextPage, summary, Some(pageNumber))
    RedirectTo(p, request.headers)
  }

  private def redirectToPage(page: Int) = Redirect(routes.PageController.showPage(page))

}

object UrlFor {

  def apply(p: TargetPage, hs: Headers): String = UrlFor(actionFor(p), hs)

  def apply(c: Call, hs: Headers): String = {
    val referer = hs.get("referer")
    referer.flatMap(new QueryStringDecoder(_).parameters.asScala.get("edit").map(_.asScala.head)).map { r =>
      c.url + "#" + r
    } getOrElse c.url
  }

  private def actionFor(p: TargetPage) = p match {
    case PageToGoTo(p) => dataCapturePages.routes.PageController.showPage(p)
    case SummaryPage => controllers.routes.ApplicationController.checkYourAnswers
  }
}

object RedirectTo {
  def apply(p: TargetPage, hs: Headers): Result = Redirect(UrlFor(p, hs))
}

object ForDataCapturePage {

  sealed trait FormAction
  object Continue extends FormAction
  object Back extends FormAction
  object Save extends FormAction
  object Update extends FormAction
  object Unknown extends FormAction

  def extractAction(fields: Option[Map[String, Seq[String]]]): FormAction = fields map { fs =>
    fs.get("continue_button").map(_ => Continue)
      .orElse(fs.get("save_button").map(_ => Save))
      .orElse(fs.get("back_button").map(_ => Back))
      .orElse(fs.get("update_button").map(_ => Update))
      .getOrElse(Unknown)
  } getOrElse(Unknown)
}
