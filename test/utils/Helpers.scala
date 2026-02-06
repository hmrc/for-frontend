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

package utils

import actions.RefNumAction
import play.api.mvc.*
import play.api.test.FakeRequest

import scala.language.implicitConversions

object Helpers {

  implicit def fakeRequest2MessageRequest[A](fakeRequest: FakeRequest[A]): MessagesRequest[A] =
    new MessagesRequest[A](fakeRequest, play.api.test.Helpers.stubMessagesApi())

  def refNumAction(): RefNumAction = {
    val cc = play.api.test.Helpers.stubControllerComponents()

    new RefNumAction(new play.api.mvc.BodyParsers.Default(cc.parsers), cc.messagesApi)(using cc.executionContext)
  }

}
