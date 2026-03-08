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

package models

import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

import scala.compiletime.summonAll
import scala.deriving.Mirror

/**
  * Scala 3 enum JSON (de)serializer.
  *
  * @author Yuriy Tumakha
  */
object Scala3EnumJsonFormat:

  inline def format[E](using m: Mirror.SumOf[E]): Format[E] =
    val enumInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[E]]].map(_.value)

    val enumMap: Map[String, E] = enumInstances.map(o => o.asInstanceOf[Any].toString -> o).toMap

    Format[E](
      {
        case JsString(name) =>
          enumMap
            .get(name)
            .fold(JsError(s"Enum value '$name' is not in allowed list - ${enumMap.keys.mkString(", ")}"))(JsSuccess(_))
        case js             =>
          JsError(s"Invalid Json: expected string, got: $js")
      },
      o => JsString(o.asInstanceOf[Any].toString)
    )
