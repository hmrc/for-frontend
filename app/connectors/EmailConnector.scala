/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._

@Singleton
class EmailConnector @Inject()(config: ServicesConfig, http: ForHttp)(implicit ec: ExecutionContext) {

  lazy val emailUrl = config.baseUrl("email")


  def sendEmail(refNumber: String, postcode: String, email: Option[String], expiryDate: LocalDate)(implicit hc: HeaderCarrier, messages: Messages) = {
    email.map { e =>
      val formattedExpiryDate = s"${expiryDate.getDayOfMonth} ${Messages(s"month.${expiryDate.monthOfYear.getAsText}")} ${expiryDate.getYear}"
      val json = Json.obj(
        "to" -> JsArray(Seq(JsString(e))),
        "templateId" -> JsString("rald_alert"),
        "parameters" -> JsObject(Seq(
          "referenceNumber" -> JsString(s"""${Messages("saveForLater.refNum")}: $refNumber"""),
          "postcode" -> JsString(s"""${Messages("saveForLater.postcode")}: $postcode"""),
          "expiryDate" -> JsString(s"""${Messages("saveForLater.paragraph")} $formattedExpiryDate""")
        )),
        "force" -> JsBoolean(false)
      )
      http.POST[JsObject, HttpResponse](s"$emailUrl/send-templated-email/", json).map( _ => ())
    } getOrElse Future.unit
  }
}
