/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import com.google.inject.ImplementedBy
import controllers.toFut
import crypto.MongoHasher
import models.serviceContracts.submissions.{AddressConnectionTypeYes, AddressConnectionTypeYesChangeAddress}
import models.{Credentials, FORLoginResponse}
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpReads, HttpResponse, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import useCases.ReferenceNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultHODConnector @Inject() (
  config: ServicesConfig,
  http: ForHttp,
  mongoHasher: MongoHasher
)(implicit ec: ExecutionContext
) extends HODConnector {

  implicit val f: Format[Document] = Document.formats

  lazy val serviceUrl: String = config.baseUrl("for-hod-adapter")
  lazy val emailUrl: String   = config.baseUrl("email")

  private def url(path: String) = s"$serviceUrl/for/$path"

  def readsHack(implicit httpReads: HttpReads[FORLoginResponse]): HttpReads[FORLoginResponse] =
    new HttpReads[FORLoginResponse] {

      override def read(method: String, url: String, response: HttpResponse): FORLoginResponse =
        response.status match {
          case 400 => throw new BadRequestException(response.body)
          case 401 => throw new UpstreamErrorResponse(response.body, 401, 401, response.headers)
          case _   => httpReads.read(method, url, response)
        }
    }

  override def verifyCredentials(referenceNumber: String, postcode: String)(implicit hc: HeaderCarrier): Future[FORLoginResponse] = {
    val credentials = Credentials(referenceNumber, postcode)
    http.post[Credentials](url("authenticate"), credentials).map(r => Json.parse(r.body).as[FORLoginResponse])
  }

  override def saveForLater(d: Document)(implicit hc: HeaderCarrier): Future[Unit] = {
    val document = d.copy(saveForLaterPassword = d.saveForLaterPassword.map(mongoHasher.hash))
    http.put(url(s"savedforlater/${document.referenceNumber}"), document) map { _ => () }
  }

  override def loadSavedDocument(r: ReferenceNumber)(implicit hc: HeaderCarrier): Future[Option[Document]] =
    http.get[Document](url(s"savedforlater/$r"), Seq.empty)
      .map(Some(_)).map(splitAddress).map(removeAlterationDescription)
      .recoverWith {
        case _: NotFoundException => None
      }

  def splitAddress(maybeDocument: Option[Document]): Option[Document] = {
    val fixedDocument =
      for {
        doc              <- maybeDocument
        page1            <- doc.page(1)
        isAddressCorrect <- page1.fields.get("isAddressCorrect")
      } yield
        if isAddressCorrect.contains("false") then
          updateChangedAddresToNewModel(doc, page1)
        else
          val page0 = Page(0, form.PageZeroForm.pageZeroForm.fill(AddressConnectionTypeYes).data.view.mapValues(Seq(_)).toMap)
          updateDocWithPageZeroAndRemovePageOne(doc, page0)
    fixedDocument.orElse(maybeDocument)
  }

  def updateChangedAddresToNewModel(document: Document, page1: Page): Document = {
    val page1Data = page1.fields.map { case (key, value) =>
      if (key.startsWith("address.")) {
        (key.replace("address.", ""), value)
      } else {
        (key, value)
      }
    }.view.filterKeys(_ != "isAddressCorrect").toMap

    val newPage1 = page1.copy(fields = page1Data)

    val page0 = Page(0, form.PageZeroForm.pageZeroForm.fill(AddressConnectionTypeYesChangeAddress).data.view.mapValues(Seq(_)).toMap)

    val newPages = Seq(page0, newPage1) ++ document.pages.filterNot(x => x.pageNumber == 0 || x.pageNumber == 1)

    document.copy(pages = newPages)

  }

  def updateDocWithPageZeroAndRemovePageOne(document: Document, page0: Page): Document = {
    val newPages = page0 +: document.pages.filterNot(x => x.pageNumber == 0 || x.pageNumber == 1)
    document.copy(pages = newPages)
  }

  def removeAlterationDescription(maybeDocument: Option[Document]): Option[Document] = {
    val alternationDescriptionPattern = """^propertyAlterationsDetails\[\d{0,2}\]\.description$""".r

    val maybeAlteredDocumment = for {
      document <- maybeDocument
      page13   <- document.page(13)
    } yield {
      val newFields = page13.fields.filterNot(x => alternationDescriptionPattern.unapplySeq(x._1).isDefined)

      val newPage13 = page13.copy(fields = newFields)
      val pages     = (newPage13 +: document.pages.filterNot(_.pageNumber == 13)).sortBy(_.pageNumber)
      document.copy(pages = pages)
    }

    maybeAlteredDocumment.orElse(maybeDocument) // Return altered document or original document.
  }

  def getSchema(schemaName: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    http.get[JsValue](url(s"schema/$schemaName"), Seq.empty)
}

@ImplementedBy(classOf[DefaultHODConnector])
trait HODConnector {
  def verifyCredentials(referenceNumber: String, postcode: String)(implicit hc: HeaderCarrier): Future[FORLoginResponse]
  def saveForLater(d: Document)(implicit hc: HeaderCarrier): Future[Unit]
  def loadSavedDocument(r: ReferenceNumber)(implicit hc: HeaderCarrier): Future[Option[Document]]
  def getSchema(schemaName: String)(implicit hc: HeaderCarrier): Future[JsValue]
}
