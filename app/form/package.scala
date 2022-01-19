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

import play.api.data.validation.ValidationResult
import play.api.data.Mapping
import play.api.data.FormError
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import play.api.data.validation.Invalid
import play.api.data.Form

package object form {

  def getMappingErrors[T](dataOpt: Option[T], mapping: Mapping[T], prefix: String): ValidationResult = {
    val form = Form(mapping.withPrefix(prefix))
    val populatedForm = dataOpt match {
      case Some(data) => form.fill(data)
      case None       => form
    }
    val dataMap = populatedForm.data
    val result: Seq[FormError] = form.bind(dataMap).convertGlobalToFieldErrors().fold(formWithErrors => formWithErrors.errors, success => Seq())
    val valErrors: Seq[ValidationError] = result flatMap {
      case FormError(key, messages, args) => messages.map { message => createFieldValidationError(key, message, args: _*) }
    }
    if (valErrors.isEmpty) {
      Valid
    } else {
      Invalid(valErrors)
    }
  }
  implicit class ValidationResultHelper(validationResult: ValidationResult) {

    def and(constraint: => ValidationResult): ValidationResult = validationResult match {
      case Valid => constraint
      case invalid @ Invalid(errors1) => constraint match {
        case Valid                       => invalid
        case invalid2 @ Invalid(errors2) => Invalid(errors1 ++ errors2)
      }
    }

    def or(constraint: => ValidationResult): ValidationResult = validationResult match {
      case Valid => Valid
      case invalid @ Invalid(errors1) => constraint match {
        case Valid                       => Valid
        case invalid2 @ Invalid(errors2) => Invalid(errors1 ++ errors2)
      }
    }
  }

  def checkFieldConstraint(cond: => Boolean, field: String, code: String): ValidationResult = {
    if (cond) {
      Valid
    } else {
      Invalid(Seq(createFieldValidationError(field, code)))
    }
  }

  def createFieldValidationError(field: String, code: String, args: Any*): ValidationError = {
    ValidationError(s"fieldError|$field|$code", args: _*)
  }

  def createFieldConstraintFor(cond: Boolean, code: String, fields: Seq[String]): ValidationResult = {
    fields.map(field => checkFieldConstraint(cond, field, code)).reduce(_.and(_))
  }

  implicit class FormErrorSupport(formError: FormError) {
    def convert(): FormError =
      formError.message.split('|') match {
        case Array("fieldError", path, code) =>
          val newPath = if (formError.key.isEmpty) {
            path
          } else {
            formError.key + "." + path
          }
          FormError(newPath, newPath + "." + code, formError.args)
        case _ => formError
      }

  }

  implicit class MappingSupportHelper[T](mapping: Mapping[T]) {

    lazy val keys: Seq[String] = {
      val childKeys: Set[Seq[String]] = mapping.mappings.filter(_ != mapping).map(_.keys).toSet
      val res: Seq[String] = if (childKeys.isEmpty) {
        Seq(mapping.key)
      } else {
        childKeys.reduce(_ ++ _).distinct
      }
      res.sorted.sortBy(_.contains("."))
    }

  }

  implicit class FormHelper[T](form: Form[T]) {
    def convertGlobalToFieldErrors(): Form[T] = {
      form.copy(errors = form.errors map (_.convert()))
    }
    def getConstrainedFieldNamesStartingWith(prefix: String): Seq[String] = {
      val prefixDot = s"$prefix."
      val keys = form.mapping.keys
      keys.filter(_.startsWith(prefixDot))
    }
    def setFormData(formData: Map[String, String]): Form[T] = {
      new Form(mapping = form.mapping, data = formData, errors = form.errors, value = form.value)
    }
  }
}
