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

package views.behaviours

import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase:

  def labelDefinedAndUsedOnce(option: String, prefix: String, view: () => HtmlFormat.Appendable): Assertion =
    val doc   = asDocument(view())
    assert(messages.isDefinedAt(s"$prefix.$option"))
    val label = doc.select(s"label[for=$prefix.$option]")
    assert(label.size() == 1)

  def normalPage(view: () => HtmlFormat.Appendable, messageKeyPrefix: String, expectedGuidanceKeys: String*): Unit =
    "behave like a normal page" when {
      "rendered" should {
        "have the correct banner title" in {
          val doc  = asDocument(view())
          val link = doc.select(".govuk-service-navigation__service-name a").first()
          link.text shouldBe messagesApi("service.name")(using lang)
        }

        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsValue(doc, "title", s"${messages(s"$messageKeyPrefix.heading")} - ${messages("service.name")} - ${messages("gov.name")}")
        }

        "display the correct page title" in {
          val doc = asDocument(view())
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {
          val doc = asDocument(view())
          doc.getElementById("cymraeg-switch") != null ||
          !doc
            .getElementsByAttributeValue("href", "/valuation-office-agency-contact-frontend/language/cymraeg")
            .isEmpty
        }
      }
    }

  protected def checkServiceNameInHeaderBanner(view: () => HtmlFormat.Appendable): Assertion =
    val doc  = asDocument(view())
    val link = doc.select(".govuk-service-navigation__service-name a").first()
    link.text shouldBe messagesApi("service.name")(using lang)
