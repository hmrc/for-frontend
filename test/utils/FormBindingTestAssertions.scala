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

package utils

import form.Errors
import org.scalatest.Matchers
import play.api.data.Form

object FormBindingTestAssertions extends Matchers {

  def doesNotContainErrors[T](f: Form[T]): Unit = {
    if (f.hasErrors) {
      fail(s"Form incorrectly has errors: ${f.errors}. ${boundObject(f)}")
    }

    if (!f.value.isDefined) {
      fail(s"Form did not bind")
    }

  }

  def mustContainRequiredErrorFor[T](field: String, f: Form[T]) { mustContainError(field, Errors.required, f) }

  def mustContainInvalidCurrencyErrorFor[T](field: String, f: Form[T]) { mustContainError(field, Errors.invalidCurrency, f) }

  def mustOnlyContainRequiredErrorFor[T](field: String, f: Form[T]) {
    mustContainError(field, Errors.required, f)
    if(f.errors.length > 1) {
      fail(s"Did not contain only required error for $field. Errors: ${f.errors}")
    }
  }

  def mustOnlyContainRequiredErrorsFor[T](fields: Seq[String], f: Form[T]) {
    fields.map(mustContainRequiredErrorFor(_, f))
    val otherErrors = f.errors.filterNot(e => fields.exists(_ == e.key))
    if(otherErrors.length > 1) {
      fail(s"Form contained unexpected errors: $otherErrors. Expected only errors for $fields")
    }
  }

  def mustContainPrefixedRequiredErrorFor[T](field: String, f: Form[T]) { mustContainError(field, s"$field.${Errors.required}", f) }

  def mustContainBooleanRequiredErrorFor[T](field: String, f: Form[T]) { mustContainError(field, Errors.booleanMissing, f) }

  def mustOnlyContainBooleanRequiredErrorFor[T](field: String, f: Form[T]) {
    mustContainError(field, Errors.booleanMissing, f)
    if(f.errors.length > 1) {
      fail(s"Did not contain only boolean required error for $field. Errors: ${f.errors}")
    }
  }

  def mustContainNegativeDecimalErrorFor[T](field: String, f: Form[T]) { mustContainError(field, Errors.bigDecimalNegative, f) }

  def mustContainPrefixedError[T](field: String, error: String, f: Form[T]) = mustContainError(field, s"$field.$error", f)

  def mustContainMaxLengthErrorFor[T](field: String, f: Form[T]) = mustContainError(field, Errors.maxLength, f)

  def mustOnlyContainError[T](field: String, error: String, f: Form[T]) {
    mustContainError(field, error, f)
    if (f.errors.length > 1) {
      fail(s"Form contained too many errors. Expected only: $field - $error. ${f.errors}")
    }
  }

  def mustContainError[T](field: String, error: String, f: Form[T]) {
    val errorsForField = f.errors(field)
    if (errorsForField.isEmpty || !errorsForField.exists(_.message == error)){
      fail(s"Form does not contain the $error for $field. Errors: ${f.errors}. \n${boundObject(f)}. \n${f.data}")
    }
  }

  def mustNotContainErrorFor[T](field: String, f: Form[T]) {
    if (f.errors.exists(_.key == field)) {
      fail(s"Form should not contain error for: $field. \nErrors: ${f.errors}")
    }
  }

  private def boundObject[T](f: Form[T]): String = f.value.map(x => s"Bound to object: ${x.toString}").getOrElse("Form did not bind to any object")

  def mustBind[T](form: Form[T])(checks: T => Unit) {
    form.value.map(v => checks(v)) getOrElse fail(s"Form did not bind. \nErrors: ${form.errors} \nData: ${form.data}")
    assert(form.errors.isEmpty, s"Form unexpectedly contained errors. \nErrors: ${form.errors} \nData: ${form.data}")
  }
}
