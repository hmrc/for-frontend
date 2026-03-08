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

package form

import play.api.data.Forms.{default, seq, text}
import play.api.data.Mapping

import scala.language.reflectiveCalls
import scala.reflect.Selectable.reflectiveSelectable

/**
  * Provides functionality for binding and unbinding enumeration fields to and from forms.
  *
  * @author Yuriy Tumakha
  */
object Scala3EnumFieldMapping:

  def enumMapping[E <: scala.reflect.Enum](e: { def values: Array[E] }): Mapping[Option[E]] =
    val enumMap: Map[String, E] = e.values.map(v => v.asInstanceOf[Any].toString -> v).toMap

    default(text, "")
      .transform[Option[E]](
        name => enumMap.get(name),
        _.fold("")(_.asInstanceOf[Any].toString)
      )

  def enumMappingRequired[E <: scala.reflect.Enum](
    e: { def values: Array[E] },
    requiredErrorMessage: String = "error.required"
  ): Mapping[E] =
    enumMapping(e)
      .verifying(requiredErrorMessage, _.isDefined)
      .transform[E](_.get, Some(_))

  def enumMappingSeq[E <: scala.reflect.Enum](e: { def values: Array[E] }): Mapping[Seq[E]] =
    seq(enumMapping(e))
      .transform[Seq[E]](_.flatten, _.map(Some(_)))
