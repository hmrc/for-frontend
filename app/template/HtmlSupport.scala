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

package template

import play.api.data.FormError
import play.api.data.Field

object HtmlSupport {

  def getArgs(value: String, dataAttributes: Seq[(String, String, Any)] = Seq(), args: Map[Symbol, Any]): Map[Symbol, Any] = {
    val extras: Map[Symbol, Any] = dataAttributes.filter(_._1 == value).map { case (_, key, value) => (Symbol(key), value) }.toMap
    args ++ extras
  }

  def getErrors(fields: Seq[Field]): Seq[FormError] =
    fields.map(_.errors) reduce (_ ++ _)

}
