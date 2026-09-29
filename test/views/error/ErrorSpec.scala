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

package views.error

import views.behaviours.ViewBehaviours

class ErrorSpec extends ViewBehaviours:

  private def error = inject[views.html.error.error]

  private val messageKeyPrefix = "error.500"

  private def create409View = () => error(409)(using getRequest, messages)

  private def create500View = () => error(500)(using getRequest, messages)

  "Error view" should {

    behave like normalPage(create500View, messageKeyPrefix)

    "contain 409 message" in {
      val doc = asDocument(create409View())
      assertContainsText(doc, messages("error.409.body"))
    }
  }
