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

package template

import play.twirl.api.Html
import play.api.data.Field

object repeatWithIndex {

  def apply(field: play.api.data.Field, max: Int = 1)(f: (Field,Int) => Html) = {
    val numberOfExistingEntries = field.indexes.length
    val amountToCreate = Math.max(max, numberOfExistingEntries)
    (0 until amountToCreate) map { index => buildHtml(field, f, numberOfExistingEntries, index) }
  }

  private def buildHtml(field: Field, f: (Field, Int) => Html, numberOfExistingEntries: Int, index: Int): Html = {
    val html = f(field("[" + index + "]"), index)
    val id = IndexedFieldId(field.name, index)
    if (index > 0 && index >= numberOfExistingEntries)
      decorateWithJsDeleteAndId(html, id)
    else
      justDecorateWithId(html, id)
  }

  private def decorateWithJsDeleteAndId(h: Html, id: String) = Html(s"""<div id="$id" class="deleteifjs" >${h.toString()}</div>""")

  private def justDecorateWithId(h: Html, id: String) = Html(s"""<div id="$id" >${h.toString()}</div>""")
}

object IndexedFieldId {
  def apply(key: String, index: Int) = key + "_" + index
}
