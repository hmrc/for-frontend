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

package controllers

import javax.inject._
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc._
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}

import scala.concurrent.ExecutionContext

@Singleton
class CustomLanguageController @Inject()(configuration: Configuration,
                                         languageUtils: LanguageUtils,
                                         cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends LanguageController(languageUtils, cc) {

  def showEnglish = Action.async { implicit request =>
    switchToLanguage("english")(request).map(_.withHeaders(LOCATION -> routes.LoginController.show.url))
  }

  def showWelsh = Action.async { implicit request =>
    switchToLanguage("cymraeg")(request).map(_.withHeaders(LOCATION -> routes.LoginController.show.url))
  }

  override protected def fallbackURL: String = configuration.get[String]("language.fallbackUrl").getOrElse("/")

  override def languageMap: Map[String, Lang] = CustomLanguageController.languageMap
}

object CustomLanguageController {
  val languageMap: Map[String, Lang] = Map("english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))
}