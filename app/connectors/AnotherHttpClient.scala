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

import org.apache.pekko.actor.ActorSystem
import play.api.Configuration
import play.api.libs.ws.WSClient
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import javax.inject.{Inject, Singleton}

/**
 * Delete after upgrade to bootstrap-frontend-play-xx
 * @param config
 * @param httpAuditing
 * @param wsClient
 * @param actorSystem
 */
@Singleton()
class AnotherHttpClient @Inject() (
  val config: Configuration,
  override val httpAuditing: HttpAuditing,
  override val wsClient: WSClient,
  override protected val actorSystem: ActorSystem
) extends DefaultHttpClient(config, httpAuditing, wsClient, actorSystem)
  with uk.gov.hmrc.http.HttpClient {}
