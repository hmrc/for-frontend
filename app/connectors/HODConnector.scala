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

import controllers.toFut
import models.FORLoginResponse
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Format, JsValue}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import useCases.ReferenceNumber

import scala.concurrent.Future
import views.html.helper.urlEncode

object HODConnector extends HODConnector with ServicesConfig {
  implicit val f: Format[Document] = Document.formats

  lazy val serviceUrl = baseUrl("for-hod-adapter")

  val http = playconfig.WSHttp

  private def url(path: String) = s"$serviceUrl/for/$path"

  override def verifyCredentials(ref1: String, ref2: String, postcode: String)(implicit hc: HeaderCarrier): Future[FORLoginResponse] = {
    val parts = Seq(ref1, ref2, postcode).map(urlEncode)
    http.GET[FORLoginResponse](url(s"${parts.mkString("/")}/verify"))
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
