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

package connectors

import config.ForConfig
import controllers.toFut
import models.FORLoginResponse
import org.joda.time.LocalDate
import play.api.i18n.{Lang, Messages}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import useCases.ReferenceNumber
import views.html.helper.urlEncode

import scala.concurrent.Future

object HODConnector extends HODConnector with ServicesConfig {
  implicit val f: Format[Document] = Document.formats

  lazy val serviceUrl = baseUrl("for-hod-adapter")
  lazy val emailUrl = baseUrl("email")

  val http = ForConfig.http

  private def url(path: String) = s"$serviceUrl/for/$path"

  override def verifyCredentials(ref1: String, ref2: String, postcode: String)(implicit hc: HeaderCarrier): Future[FORLoginResponse] = {
    val parts = Seq(ref1, ref2, postcode).map(urlEncode)
    http.GET[FORLoginResponse](url(s"${parts.mkString("/")}/verify"))
  }

  def sendEmail(refNumber: String, postcode: String)(implicit hc: HeaderCarrier, lang: Lang) = {
    val expiryDate = LocalDate.now.plusDays(90)
    val formattedExpiryDate = s"${expiryDate.getDayOfMonth} ${Messages(s"month.${expiryDate.monthOfYear.getAsText}")} ${expiryDate.getYear}"
    val json = Json.parse(
      s"""{
          |"to": ["example@gmail.com"],
          |"templateId": "rald_alert",
          |"parameters": {
          | "referenceNumber": "${Messages("saveForLater.refNum")}: $refNumber",
          | "postcode": "${Messages("saveForLater.postcode")}: $postcode",
          | "expiryDate": "${Messages("saveForLater.paragraph")} $formattedExpiryDate"
          |},
          |"force": false
          |}""".stripMargin)
    http.POST(s"$emailUrl/send-templated-email/", json)
  }

  def saveForLater(d: Document)(implicit hc: HeaderCarrier): Future[Unit] =
    http.PUT(url(s"savedforlater/${d.referenceNumber}"), d) map { _ => () }

  def loadSavedDocument(r: ReferenceNumber)(implicit hc: HeaderCarrier): Future[Option[Document]] = {
    http.GET[Document](url(s"savedforlater/$r")).map(Some.apply) recoverWith {
      case n: NotFoundException => None
    }
  }

  def getSchema(schemaName: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET[JsValue](url(s"schema/$schemaName"))
  }
}

trait HODConnector {
  def verifyCredentials(ref1: String, ref2: String, postcode: String)(implicit hc: HeaderCarrier): Future[FORLoginResponse]
}
