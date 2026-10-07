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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.compatible.Assertion
import play.api.i18n.Lang

import java.util.Locale
import uk.gov.hmrc.vo.unit.test.BaseAppSpec
import play.twirl.api.Html

trait ViewSpecBase extends BaseAppSpec:

  implicit def lang: Lang = Lang(Locale.UK)

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

//  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String): Assertion =
//    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))
//
  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if elements.isEmpty then throw IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    // <p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageTitleEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*): Assertion =
    val headers = doc.getElementsByTag("h1")
    headers.size                                 should equal(1)
    headers.first.text.replaceAll("\u00a0", " ") should be(messages(expectedMessageKey, args*).replaceAll("&nbsp;", " "))

  def assertContainsText(doc: Document, text: String): Assertion =
    assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

//  def assertContainsMessages(doc: Document, expectedMessageKeys: String*): Unit =
//    for (key <- expectedMessageKeys) assertContainsText(doc, messages(key))
//
  def assertRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")

//  def assertContainsLabel(doc: Document, forElement: String, expectedText: String): Assertion =
//    val labels = doc.getElementsByAttributeValue("for", forElement)
//    assert(labels.size == 1, s"\n\nLabel for $forElement was not rendered on the page.")
//    assert(labels.first.text() == expectedText, s"\n\nLabel for $forElement was not $expectedText")

//  def assertElementHasClass(doc: Document, id: String, expectedClass: String): Assertion =
//    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")
//
  def assertContainsRadioButton(doc: Document, id: String, name: String, value: String, isChecked: Boolean): Assertion =
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)
    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")

    if isChecked then assert(radio.attr("checked") == "checked" || radio.hasAttr("checked"), s"\n\nElement $id is not checked")
    else assert(!radio.hasAttr("checked") && radio.attr("checked") != "checked", s"\n\nElement $id is checked")

//  def assertContainsCheckBox(doc: Document, id: String, name: String, value: String, isChecked: Boolean): Assertion =
//    assertRenderedById(doc, id)
//    val checkbox = doc.getElementById(id)
//    assert(checkbox.attr("name") == name, s"\n\nElement $id does not have name $name")
//    assert(checkbox.attr("value") == value, s"\n\nElement $id does not have value $value")
//
//    if isChecked then assert(checkbox.hasAttr("checked"), s"\n\nElement $id is not checked")
//    else assert(!checkbox.hasAttr("checked"), s"\n\nElement $id is checked")
