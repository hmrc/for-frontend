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

import javax.inject.{Inject, Singleton}

@Singleton
class ForConfig @Inject() (config: Configuration) {

  lazy val useDummyIp: Boolean          = getBoolean("useDummyTrueIP")
  lazy val startPageRedirect: Boolean   = getBoolean("startPageRedirect")
  lazy val govukStartPage: String       = getString("govukStartPage")
  lazy val agentApiEnabled: Boolean     = getBoolean("agentApi.enabled")
  lazy val apiTestAccountsOnly: Boolean = getBoolean("agentApi.testAccountsOnly")
  lazy val apiTestAccountPrefix: String = getString("agentApi.testAccountPrefix")

  private def getString(key: String): String   = config.getOptional[String](key).getOrElse(throw ConfigSettingMissing(key))
  private def getBoolean(key: String): Boolean = config.getOptional[Boolean](key).getOrElse(throw ConfigSettingMissing(key))

}

case class ConfigSettingMissing(key: String) extends Exception(key)
