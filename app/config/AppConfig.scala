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

package config

import play.api.Configuration
import controllers.routes
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.vo.service.config.VOServiceConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (val configuration: Configuration) extends VOServiceConfig:

  override def serviceLocalRoot: Call = routes.ApplicationController.index
  override def serviceMenuHome: Call  = routes.ApplicationController.index
  override def theFirstPage: Call     = routes.LoginController.show
  override def feedbackPage: Call     = controllers.feedback.routes.FeedbackController.feedback // TODO: Remove to use feedbackFrontendForm

  override def signOutCall: Option[Call] = Some(routes.SaveForLaterController.timeout)

  override def timeoutCall(using request: RequestHeader): Option[Call] = signOutCall

  override def isWelshTranslationAvailable: Boolean = true

  override def stylesheet: Option[Call] = Some(controllers.routes.Assets.versioned("stylesheets/app.min.css"))

  override def notificationBannerEnabledOn: Set[Call] = Set(
    serviceMenuHome,
    routes.LoginController.show
  )

  override def timeoutDialogEnabledExcept: Set[Call] = Set(
    serviceMenuHome,
    routes.LoginController.show,
    routes.ApplicationController.importantInformation,
    routes.NotConnectedCheckYourAnswersController.onConfirmationView,
    controllers.feedback.routes.SurveyController.confirmation,
    routes.SaveForLaterController.saveForLater(""),
    routes.SaveForLaterController.customPasswordSaveForLater(""),
    routes.SaveForLaterController.timeout
  )

  val useDummyIp: Boolean          = getBoolean("useDummyTrueIP")
  val startPageRedirect: Boolean   = getBoolean("startPageRedirect")
  val govukStartPage: String       = getString("govukStartPage")
  val agentApiEnabled: Boolean     = getBoolean("agentApi.enabled")
  val apiTestAccountsOnly: Boolean = getBoolean("agentApi.testAccountsOnly")
  val apiTestAccountPrefix: String = getString("agentApi.testAccountPrefix")
