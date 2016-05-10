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

package config

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.{Configuration, Play}

import scala.concurrent.duration._

object ForConfig {
  val config = Play.current.configuration
  val http = Play.current.global.asInstanceOf[ForGlobal].forHttp

  lazy val controllerConfigs = config.underlying.as[Config]("controllers")
  def metricsConfig: Option[Configuration] = config.getConfig("microservice.metrics")

  lazy val sessionTimeoutDuration = getInt("sessiontimeoutminutes") minutes
  lazy val useDummyIp = getBoolean("useDummyTrueIP")
  lazy val startPageRedirect = getBoolean("startPageRedirect")
  lazy val govukStartPage = getString("govukStartPage")

  val analytics = new {
    val ga = new {
      lazy val trackingCode = prodOnlyConf("analytics.ga.trackingCode")
      lazy val startDate = prodOnlyConf("analytics.ga.startDate")
      lazy val returnCount = prodOnlyConf("analytics.ga.returnCount")
      lazy val refNum = prodOnlyConf("analytics.ga.refNum")
    }
  }
  private def prodOnlyConf(key: String) = getString(key)

  private def getString(key: String) = config.getString(key).getOrElse(throw ConfigSettingMissing(key))
  private def getInt(key: String): Int = play.api.Play.current.configuration.getInt(key).getOrElse(throw ConfigSettingMissing(key))

  private def getBoolean(key: String) = config.getString(key).map(_.toBoolean).getOrElse(throw ConfigSettingMissing(key))
}

case class ConfigSettingMissing(key: String) extends Exception(key)
