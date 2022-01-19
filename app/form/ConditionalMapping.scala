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
import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint
import uk.gov.voa.play.form.Condition
import uk.gov.voa.play.form.ConditionalMappings._

object ConditionalMapping {
  def ifTrueElse[T](fieldName: String, trueMapping: Mapping[T], falseMapping: Mapping[T]): Mapping[T] =
    IfElseMapping(isTrue(fieldName), trueMapping, falseMapping)

  def nonEmptyTextOr(fieldName: String, mapping: Mapping[String]): Mapping[String] =
    NonEmptyTextOrMapping(fieldName, mapping)
}

case class IfElseMapping[T](condition: Condition, trueMapping: Mapping[T], falseMapping: Mapping[T],
  keys: Set[String] = Set(), constraints: Seq[Constraint[T]] = Nil) extends Mapping[T] {

  override val format: Option[(String, Seq[Any])] = trueMapping.format

  def verifying(addConstraints: Constraint[T]*): Mapping[T] = {
    this.copy(constraints = constraints ++ addConstraints.toSeq)
  }

  def bind(data: Map[String, String]): Either[Seq[FormError], T] = {
    if (condition(data)) trueMapping.bind(data) else falseMapping.bind(data)
  }

  def unbind(value: T): Map[String, String] = {
    trueMapping.unbind(value)
  }

  def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    trueMapping.unbindAndValidate(value)
  }

  def withPrefix(prefix: String): IfElseMapping[T] = {
    copy(trueMapping = trueMapping.withPrefix(prefix), falseMapping = falseMapping.withPrefix(prefix))
  }

  val mappings: Seq[Mapping[_]] = trueMapping.mappings :+ this

  override val key: String = trueMapping.key
}

case class NonEmptyTextOrMapping(fieldName: String, wrapped: Mapping[String], keys: Set[String] = Set(),
                                 constraints: Seq[Constraint[String]] = Nil) extends Mapping[String] {

  override val format: Option[(String, Seq[Any])] = wrapped.format

  val key = wrapped.key

  def verifying(addConstraints: Constraint[String]*): Mapping[String] = {
    this.copy(constraints = constraints ++ addConstraints.toSeq)
  }

  def bind(data: Map[String, String]): Either[Seq[FormError], String] = {
    if (data.get(fieldName).map(_ != "").getOrElse(false)) {
      wrapped.bind(data)
    } else {
      Left(Seq(FormError(wrapped.key, Seq("error.required"))))
    }
  }

  def unbind(value: String): Map[String, String] = {
    wrapped.unbind(value)
  }

  def unbindAndValidate(value: String): (Map[String, String], Seq[FormError]) = {
    wrapped.unbindAndValidate(value)
  }

  def withPrefix(prefix: String): Mapping[String] = {
    copy(wrapped = wrapped.withPrefix(prefix))
  }

  val mappings: Seq[Mapping[_]] = wrapped.mappings :+ this

}
