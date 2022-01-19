/*
 * Copyright 2022 HM Revenue & Customs
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

package form


import play.api.data.{FormError, Forms, Mapping}
import play.api.data.format.Formatter

object PostcodeMapping {

  def postcode(missingError: String = form.Errors.invalidPostcode,
               formatError: String = form.Errors.invalidPostcode): Mapping[String]  = Forms.of[String](postcodeFormatter(missingError, formatError))

  val allowedChars = """[^a-zA-Z0-9]""".r

  val postcodeRegex = """^[A-Z]{1,2}[0-9][A-Z0-9]? ?[0-9][A-Z]{2}$""".r

  def postcodeFormatter(missingError: String = "postcode.missing", formatError: String = "postcode.format") = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.get(key)
        .filterNot(_.trim == "")
        .map { rawPostcode =>
          val cleanedPostcode = allowedChars.replaceAllIn(rawPostcode, "").toUpperCase
          cleanedPostcode match {
            case postcodeRegex() => Right(cleanedPostcode.substring(0, cleanedPostcode.length - 3) + " " + cleanedPostcode.substring(cleanedPostcode.length - 3))
            case _ => Left(Seq(FormError(key, formatError, Seq(rawPostcode))))
          }
        }.getOrElse(Left(Seq(FormError(key, missingError))))
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }

}
