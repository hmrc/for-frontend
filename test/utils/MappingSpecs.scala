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

import form._
import models._
import play.api.data.Form
import utils.FormBindingTestAssertions._

object MappingSpecs extends DateMappingSpecs with DurationMappingSpecs with AddressMappingSpecs with CurrencySpecs with CommonSpecs {

  def validateFullName[T](form: Form[T], formData: Map[String, String], field: String) {
    validateLettersNumsSpecCharsUptoLength(field, 50, form, formData)
  }

  def validatePhone[T](form: Form[T], formData: Map[String, String], prefix: String) = {
    validateOptionalPhone(form, formData, prefix)
    val fieldName = prefix + ".phone"
    verifyIsMandatory(fieldName, form, formData)
  }

  def validateOptionalPhone[T](form: Form[T], formData: Map[String, String], prefix: String) = {
    val fieldName = prefix + ".phone"
    canOnlyContainNumbersAndSpecialCharacters(fieldName, form, formData)
    limitedToNChars(fieldName, 20, form, formData)
  }

  def validateUptoNDigits[T](field: String, digits: Int, form: Form[T], formData: Map[String, String]) {
    limitedToNDigits(field, digits, form, formData)
    canOnlyContainDigits(field, digits, form, formData)
  }

  private def canOnlyContainDigits[T](field: String, maxLength: Int, form: Form[T], formData: Map[String, String]) {
    val invalid = Seq( (1 to maxLength).map(_ => "a").mkString(""), "b", "C" )    
    validateError(field, invalid, Errors.number, form, formData)

    val valid = Seq( (1 to maxLength).map(_ => "1").mkString(""), "1", "9")
    validateNoError(field, valid, form, formData)
  }
}

trait CurrencySpecs { this: CommonSpecs =>
  val invalidCurrencyValues = Seq(
    "1.123", "33.305", "15000.999", "not a number", "aa.32", "-1.11", "11.22.22", "45,000.11,1,"
  )

  val excessiveCurrencyValues = Seq(
    "1111111111.11", "12345678.0", "12345678.00", "10000000.00"
  )

  val maxCurrencyAmount = 9999999

  val validCurrencyValues = Seq("1", "22.2", "24.35", "10000.88", "20000", "2222222.22", "22,000.55", "9999999.99")

  def validateAnnualRent[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateWeeklyRentalPeriod(field, form, formData)
    validateMonthlyRentalPeriod(field, form, formData)
    validateQuarterlyRentalPeriod(field, form, formData)
  }

  private def validateWeeklyRentalPeriod[T](field: String, form: Form[T], formData: Map[String, String]) {
    val data = formData.updated(s"$field.rentLengthType", RentLengthTypeWeekly.name)
    
    val valid = Seq("192307.69", "1", "250", "55421")
    validateNoError(s"$field.annualRentExcludingVat", valid, form, data)

    val invalid = Seq("192307.70", "9000000", "250000")
    validateError(s"$field.annualRentExcludingVat", invalid, Errors.maxCurrencyAmountExceeded, form, data, Some(field))
  }

  private def validateMonthlyRentalPeriod[T](field: String, form: Form[T], formData: Map[String, String]) {
    val data = formData.updated(s"$field.rentLengthType", RentLengthTypeMonthly.name)
    
    val valid = Seq("833333.33", "1", "250", "55421")
    validateNoError(s"$field.annualRentExcludingVat", valid, form, data)

    val invalid = Seq("833333.34", "9000000")
    validateError(s"$field.annualRentExcludingVat", invalid, Errors.maxCurrencyAmountExceeded, form, data, Some(field))
  }

  private def validateQuarterlyRentalPeriod[T](field: String, form: Form[T], formData: Map[String, String]) {
    val data = formData.updated(s"$field.rentLengthType", RentLengthTypeQuarterly.name)
    
    val valid = Seq("2499999.99", "1", "250", "55421")
    validateNoError(s"$field.annualRentExcludingVat", valid, form, data)

    val invalid = Seq("2500000.00", "9000000")
    validateError(s"$field.annualRentExcludingVat", invalid, Errors.maxCurrencyAmountExceeded, form, data, Some(field))
  }

  def validateCurrency[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateError(field, invalidCurrencyValues, Errors.invalidCurrency, form, formData)
    validateNoError(field, validCurrencyValues, form, formData)
    validateDoesNotExceedMaxCurrencyAmount(field, form, formData)
  }

  def validateNonNegativeCurrency[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateError(field, invalidCurrencyValues, Errors.invalidCurrency, form, formData)
    val valid = validCurrencyValues ++ Seq("0", "0.05", "0.99", "0.01")
    validateNoError(field, valid, form, formData)
    validateDoesNotExceedMaxCurrencyAmount(field, form, formData)
  }

  def validateDoesNotExceedMaxCurrencyAmount[T](field: String, form: Form[T], formData: Map[String, String]): Unit = {
    validateError(field, excessiveCurrencyValues, Errors.maxCurrencyAmountExceeded, form, formData)
  }
}

trait DateMappingSpecs { this: CommonSpecs =>
  import org.joda.time.DateTime

  def validateFullDateInPast[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateAnyDate(field, form, formData)
    validateDay(field, form, formData)
    fullDateMustBeInPast(field, form, formData)
  }

  def validatePastDate[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateAnyDate(field, form, formData)
    mustBeInPast(field, form, formData)
  }

  def validateDate[T](field: String, form: Form[T], formData: Map[String, String]) {
    validateAnyDate(field, form, formData)
    validateDay(field, form, formData)
    dateMayBeInFuture(field, form, formData)
  }

  private def validateAnyDate[T](field: String, form: Form[T], formData: Map[String, String]) {
    monthCanOnlyBe1to12(field, form, formData)
    cannotBeEmptyString(field + ".year", form, formData)
    cannotBeEmptyString(field + ".month", form, formData)
    yearCanOnlyBe4Digits(field, form, formData)
    yearMustBe1900OrLater(field, form, formData)
    ignoresLeadingAndTraillingWhitespace(field, form, formData)
  }
  
  private def validateDay[T](field: String, form: Form[T], formData: Map[String, String]) {
    dayCanOnlyBe1to31(field, form, formData)
    cannotBeEmptyString(field + ".day", form, formData)
    mustBeValidDayInMonth(field, form, formData)
  }

  private def dayCanOnlyBe1to31[T](field: String, form: Form[T], formData: Map[String, String]) {
    val key = field + ".day"
    val data = formData.
      updated(field + ".month", "1").
      updated(field + ".year", "2014")

    val invalid = Seq("-1", "0", "32", "999", "day")
    validateError(key, invalid, Errors.invalidDate, form, data)

    val valid = (1 to 31).map(_.toString)
    validateNoError(key, valid, form, data)
  }

  private def monthCanOnlyBe1to12[T](field: String, form: Form[T], formData: Map[String, String]) {
    val key = field + ".month"
    val data = formData.updated(field + ".year", "2014")

    val invalid = Seq("-1", "0", "13", "25", "200", "2000000", "erkjl")
    validateError(key, invalid, Errors.invalidDate, form, data) 

    val valid = (1 to 12).map(_.toString) 
    validateNoError(field, valid, form, data)
  }

  private def yearCanOnlyBe4Digits[T](field: String, form: Form[T], formData: Map[String, String]) {
    val key = field + ".year"
    val invalid = Seq("abc", "12345", "-1000")
    validateError(key, invalid, Errors.invalidDate, form, formData)
  }

  private def yearMustBe1900OrLater[T](field: String, form: Form[T], formData: Map[String, String]) {
    val key = field + ".year"

    val invalid = Seq("1899", "1000", "0001")
    invalid foreach { i => validateError(key, invalid, Errors.dateBefore1900, form, formData, Some(field)) }

    val valid = Seq("1900", "1901", "1955", "2014")
    valid foreach { v => validateNoError(key, valid, form, formData) }
  }

  private def ignoresLeadingAndTraillingWhitespace[T](field: String, form: Form[T], formData: Map[String, String]) {
    val yearKey = field + ".year"
    val monthKey = field + ".month"

    val data = formData.updated(monthKey, " 1 ").updated(yearKey, " 2011 ")
    val f = form.bind(data)
    doesNotContainErrors(f)
  }

  private def mustBeInPast[T](field: String, form: Form[T], formData: Map[String, String]) {
    val monthKey = field + ".month"
    val yearKey = field + ".year"

    val nextMonth = DateTime.now.plusMonths(1)

    val invalid = Seq(("12", "3030"), ("1", "3025"), (nextMonth.getMonthOfYear.toString, nextMonth.getYear.toString))
    invalid foreach { iv =>
      val d = formData.updated(monthKey, iv._1).updated(yearKey, iv._2)
      val f = form.bind(d).convertGlobalToFieldErrors()
      mustOnlyContainError(field, Errors.dateMustBeInPast, f)
    }

    val lastMonth = DateTime.now.plusMonths(-1)
    val now = DateTime.now

    val valid = Seq(
      (lastMonth.getMonthOfYear.toString, lastMonth.getYear.toString),
      (now.getMonthOfYear.toString, now.getYear.toString),
      ("9", "2015"), ("1", "1900"))
    valid foreach { v =>
      val d = formData.updated(monthKey, v._1).updated(yearKey, v._2)
      val f = form.bind(d).convertGlobalToFieldErrors()
      doesNotContainErrors(f)
    }
  }

  private def dateMayBeInFuture[T](field: String, form: Form[T], formData: Map[String, String]) {
    val monthKey = field + ".month"
    val yearKey = field + ".year"

    val nextMonth = DateTime.now.plusMonths(1)
    val data = formData.
      updated(monthKey, nextMonth.getMonthOfYear.toString).
      updated(yearKey, nextMonth.getYear.toString)

    val res = form.bind(data).convertGlobalToFieldErrors()

    doesNotContainErrors(res)
  }

  private def fullDateMustBeInPast[T](field: String, form: Form[T], formData: Map[String, String]) {
    val dayKey = field + ".day"
    val monthKey = field + ".month"
    val yearKey = field + ".year"

    val tomorrow = DateTime.now.plusDays(1)

    val invalid = Seq(
      ("28", "2", "2225"),
      ("23", "5", "2115"),
      (tomorrow.getDayOfMonth.toString, tomorrow.getMonthOfYear.toString, tomorrow.getYear.toString))
    invalid foreach { iv =>
      val f = updateFullDateAndBind(field, iv, form, formData)
      mustOnlyContainError(field, Errors.dateMustBeInPast, f)
    }

    val yesterday = DateTime.now.minusDays(1)

    val valid = Seq(
      ("1", "1", "2015"),
      ("31", "10", "2014"),
      (yesterday.getDayOfMonth.toString, yesterday.getMonthOfYear.toString, yesterday.getYear.toString)
    )
    valid foreach {v =>
      val f = updateFullDateAndBind(field, v, form, formData)
      doesNotContainErrors(f)
    }
  }
  
  def mustBeValidDayInMonth[T](field: String, form: Form[T], formData: Map[String, String]) {
    val dayKey = field + ".day"
    val monthKey = field + ".month"
    val yearKey = field + ".year"
    
    val invalid = Seq(
      ("29", "2", "2015"),
      ("31", "9", "2015")
    )
    invalid foreach { iv =>
      val f = updateFullDateAndBind(field, iv, form, formData)
      mustOnlyContainError(field, Errors.invalidDate, f)
    }
    
    val valid = Seq(
      ("29", "2", "2012"),
      ("31", "8", "2015"),
      ("30", "9", "2015")
    )
    valid foreach {v =>
      val f = updateFullDateAndBind(field, v, form, formData)
      doesNotContainErrors(f)
    }
  }

  private def updateFullDateAndBind[T](field: String, date: (String, String, String), form: Form[T], formData: Map[String, String]): Form[T] = {
    val data = formData.
      updated(field + ".day", date._1).
      updated(field + ".month", date._2).
      updated(field + ".year", date._3)
    form.bind(data).convertGlobalToFieldErrors()
  }
}

trait DurationMappingSpecs { this: CommonSpecs =>

  def validatesDuration[T](prefix: String, form: Form[T], formData: Map[String, String]) = {
    cannotBeEmptyString(prefix + ".years", form, formData)
    cannotBeEmptyString(prefix + ".months", form, formData)
    monthDurationCanOnlyBeUpTo12(prefix, form, formData)
    yearDurationCanOnlyBe3Digits(prefix, 3, form, formData)
  }

  def validatesDurationMonths[T](prefix: String, form: Form[T], formData: Map[String, String]) = {
    cannotBeEmptyString(prefix, form, formData)
    yearDurationCanOnlyBe3Digits(prefix, 3, form, formData)
  }

  private def monthDurationCanOnlyBeUpTo12[T](field: String, form: Form[T], formData: Map[String, String]) {
    val key = field + ".months"
    val data = formData.updated(field + ".year", "2014")

    val invalid = Seq("-1", "13", "25", "200", "2000000", "erkjl")
    validateError(key, invalid, Errors.invalidDurationMonths, form, data)

    val valid = (0 to 12).map(_.toString)
    validateNoError(field, valid, form, data)
  }

  private def yearDurationCanOnlyBe3Digits[T](prefix: String, n: Int, form: Form[T], formData: Map[String, String]) = {
    val key = prefix + ".years"
    val invalid = Seq("abc", "1234", "-100")
    validateError(key, invalid, Errors.invalidDurationYears, form, formData)

    val valid = Seq("1", "111", "111", "999")
    validateNoError(key, valid, form, formData)
  }
}

trait AddressMappingSpecs extends PostcodeMappingSpecs { this: CommonSpecs =>
  private val addressKeys = new {
    val buildingNameOrNumber = "buildingNameNumber"
    val street1 = "street1"
    val street2 = "street2"
    val postcode = "postcode"
  }

  def validateAddress[T](form: Form[T], formData: Map[String, String], fieldPrefix: String) {
    val prefix = fieldPrefix + "."
    val dwa = withAddressValues(formData, prefix)
    validateAddressLine(prefix + addressKeys.buildingNameOrNumber, form, dwa, isMandatory = true)
    validateAddressLine(prefix + addressKeys.street1, form, dwa)
    validateAddressLine(prefix + addressKeys.street2, form, dwa)
    validatesPostcode(prefix + addressKeys.postcode, form, dwa)
  }

  def validateOverseasAddress[T](form: Form[T], formData: Map[String, String], fieldPrefix: String) {
    val prefix = fieldPrefix + "."
    val dwa = withAddressValues(formData, prefix)
    validateAddressLine(prefix + addressKeys.buildingNameOrNumber, form, dwa, isMandatory = false)
    validateAddressLine(prefix + addressKeys.street1, form, dwa)
    validateAddressLine(prefix + addressKeys.street2, form, dwa)
    validateLettersNumsSpecCharsUptoLength(prefix + addressKeys.postcode, 10, form, dwa)
  }

  private def withAddressValues(m: Map[String, String], prefix: String): Map[String, String] = {
    m.updated(prefix + addressKeys.buildingNameOrNumber, "123 Nice House")
     .updated(prefix + addressKeys.street1, "A long street")
     .updated(prefix + addressKeys.street2, "A small grove")
     .updated(prefix + addressKeys.postcode, "RU45 9LS")
  }

  private def validateAddressLine[T](field: String, form: Form[T], formData: Map[String, String], isMandatory: Boolean = false) {
    if (isMandatory) 
      verifyIsMandatory(field, form, formData)
    else 
      verifyIsNotMandatory(field, form, formData)

    canContainsLettersNumbersSpacesSpecialAccentCharacters(field, form, formData)
    limitedToNChars(field, 50, form, formData)
  }
}

trait PostcodeMappingSpecs { this: CommonSpecs =>

  protected def validatesPostcode[T](field: String, form: Form[T], formData: Map[String, String]) {
    allowsValidPostcodes(field, form, formData)
    rejectsInvalidPostcodes(field, form, formData)
    cannotBeEmptyString(field, form, formData)
  }

  protected def allowsValidPostcodes[T](field: String, form: Form[T], formData: Map[String, String]) {
    val validPostcodes = Seq("AA11 1BW", "AA1A 1AA", "AA11 1HS", "AA1 5UT", "AA1A 2BX", "AA2N 4JS")
    validateNoError(field, validPostcodes, form, formData)
  }

  protected def rejectsInvalidPostcodes[T](field: String, form: Form[T], formData: Map[String, String]) {
    val invalidPostcodes = Seq("Steve", "AAAA11", "01111111111", "POST CODE", "Generic Headphones")
    invalidPostcodes foreach { pc =>
      val data = formData.updated(field, pc)
      val f = form.bind(data).convertGlobalToFieldErrors()
      mustOnlyContainError(field, Errors.invalidPostcode, f)
    }
  }
}

trait CommonSpecs {

  protected def validateError[T](field: String, values: Seq[String], error: String, form: Form[T], formData: Map[String, String], errorKey: Option[String] = None) {
    values foreach { v =>
      val d = formData.updated(field, v)
      val f = form.bind(d).convertGlobalToFieldErrors()
      mustOnlyContainError(errorKey.getOrElse(field), error, f)
    }
  }

  protected def validateNoError[T](field: String, values: Seq[String], form: Form[T], formData: Map[String, String]) {
    values foreach { v =>
      val d = formData.updated(field, v)
      val f = form.bind(d).convertGlobalToFieldErrors()
      doesNotContainErrors(f)
    } 
  }

  protected def cannotBeEmptyString[T](field: String, form: Form[T], formData: Map[String, String]) {
    val data = formData.updated(field, "")
    val f = form.bind(data).convertGlobalToFieldErrors()
    mustOnlyContainRequiredErrorFor(field, f)
  }

  def limitedToNChars[T](field: String, n: Int, form: Form[T], formData: Map[String, String]) {
    val fifty1Chars = (1 to (n + 1)).map(_ => "1").mkString("")
    val data = formData.updated(field, fifty1Chars)
    val f = form.bind(data).convertGlobalToFieldErrors()
    mustContainMaxLengthErrorFor(field, f)

    val fiftyChars = (1 to n).map(_ => "1").mkString("")
    val data2 = formData.updated(field, fiftyChars)
    val f2 = form.bind(data2).convertGlobalToFieldErrors()
    doesNotContainErrors(f2)
  }

  def limitedToNDigits[T](field: String, n: Int, form: Form[T], formData: Map[String, String]) {
    val fifty1Chars = (1 to (n + 1)).map(_ => 1).mkString("")
    val data = formData.updated(field, fifty1Chars)
    val f = form.bind(data).convertGlobalToFieldErrors()
    mustContainMaxLengthErrorFor(field, f)

    val fiftyChars = (1 to n).map(_ => 1).mkString("")
    val data2 = formData.updated(field, fiftyChars)
    val f2 = form.bind(data2).convertGlobalToFieldErrors()
    doesNotContainErrors(f2)
  }

  def canOnlyContainNumbersAndSpecialCharacters[T](field: String, form: Form[T], formData: Map[String, String]) = {
    val valid = Seq("012345678901", "+4412345678901", "012345 678 901", "012345-678-901", "(012345) 678 901")
    validateNoError(field, valid, form, formData)

    val invalid = Seq("012345.678.910", "oh-one-two-etc", "phone", "£9.99", "[012345] 678 901")
    validateError(field, invalid, Errors.invalidPhone, form, formData)
  }

  def validateLettersNumsSpecCharsUptoLength[T](field: String, maxLength: Int, form: Form[T], data: Map[String, String]) {
    canContainsLettersNumbersSpacesSpecialAccentCharacters(field, form, data, maxLength)
    limitedToNChars(field, maxLength, form, data) 
  }

  def verifyIsMandatory[T](field: String, form: Form[T], formData: Map[String, String]) {
    val f = form.bind(formData - field).convertGlobalToFieldErrors()
    mustContainRequiredErrorFor(field, f)
    cannotBeEmptyString(field, form, formData)
  }

  def verifyIsNotMandatory[T](field: String, form: Form[T], formData: Map[String, String]) {
    val f = form.bind(formData - field).convertGlobalToFieldErrors()
    doesNotContainErrors(f)
  }

  def canContainsLettersNumbersSpacesSpecialAccentCharacters[T](field: String, form: Form[T], formData: Map[String, String], maxLength: Int = 100) {
    val chars = "111  ABCdef &;*/.() Â ê Ê î Î ô Ô û Û"
    chars.sliding(maxLength).toSeq.foreach { x =>
      val data = formData.updated(field, x)
      val f = form.bind(data).convertGlobalToFieldErrors()
      doesNotContainErrors(f)
      assert(f.data.get(field).get === x)
    }
  }

}
