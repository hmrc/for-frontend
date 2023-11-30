/*
 * Copyright 2023 HM Revenue & Customs
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

import models.RoughDate
import models.serviceContracts.submissions.MonthsYearDuration
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{FormError, Mapping}

import scala.util.Try
import ConditionalMapping._
import util.DateUtil.nowInUK

import java.time.{LocalDate, LocalDateTime}

object DateMappings {

  private val nineteenHundred = LocalDateTime.of(1900, 1, 1, 0, 0)

  private def dateIsInPastAndAfter1900(fieldErrorPart: String): Constraint[(String, String)] = Constraint("dateInPastAndAfter1900") { x =>
    val month = x._1.trim.toInt
    val year = x._2.trim.toInt
    val lastDayOfSpecifiedMonth = LocalDateTime.of(year, month, 1, 0, 0)
    if (!lastDayOfSpecifiedMonth.isBefore(nowInUK.toLocalDateTime))
      Invalid(Errors.dateMustBeInPast + fieldErrorPart)
    else if (lastDayOfSpecifiedMonth.isBefore(nineteenHundred))
      Invalid(Errors.dateBefore1900 + fieldErrorPart)
    else
      Valid
  }

  private def fullDateIsAfter1900(fieldErrorPart: String): Constraint[(String, String, String)] = Constraint("fullDateIsAfter1900") { x =>
    val day = x._1.trim.toInt
    val month = x._2.trim.toInt
    val year = x._3.trim.toInt

    if (Try(LocalDateTime.of(year, month, day, 23, 59)).isFailure)
      Invalid(Errors.invalidDate + fieldErrorPart)
    else if (LocalDateTime.of(year, month, day, 23, 59).isBefore(nineteenHundred))
      Invalid(Errors.dateBefore1900 + fieldErrorPart)
    else
      Valid
  }

  private def fullDateIsInPastAndAfter1900(fieldErrorPart: String): Constraint[(String, String, String)] = Constraint("fullDateIsInPastAndAfter1900") { x =>
    val day = x._1.trim.toInt
    val month = x._2.trim.toInt
    val year = x._3.trim.toInt

    if(Try(LocalDateTime.of(year, month, day, 23, 59)).isFailure)
      Invalid(Errors.invalidDate + fieldErrorPart)
    else {
      val date = LocalDateTime.of(year, month, day, 23, 59)
      if (date.isAfter(nowInUK.toLocalDateTime))
        Invalid(Errors.dateMustBeInPast + fieldErrorPart)
      else if (date.isBefore(nineteenHundred))
        Invalid(Errors.dateBefore1900 + fieldErrorPart)
      else
        Valid
    }
  }

  def monthYearRoughDateMapping(prefix: String, fieldErrorPart: String = ""): Mapping[RoughDate] = tuple(
    "month" -> nonEmptyTextOr(
      prefix + ".month", text.verifying(Errors.invalidDate, x => x.trim.forall(Character.isDigit) && x.trim.toInt >= 1 && x.trim.toInt <= 12),
      s"error$fieldErrorPart.month.required"
    ),
    "year" -> nonEmptyTextOr(
      prefix + ".year", text.verifying(Errors.invalidDate, x => x.trim.forall(Character.isDigit) && x.trim.length == 4),
      s"error$fieldErrorPart.year.required"
    )).verifying(
      dateIsInPastAndAfter1900(fieldErrorPart)
    ).transform({
      case (month, year) => new RoughDate(month.trim.toInt, year.trim.toInt)},
    (date: RoughDate) => (date.month.getOrElse(1).toString, date.year.toString)
  )

  //  for precise dates, where all fields must be present and accurate
  def dateFieldsMapping(prefix: String, allowFutureDates: Boolean = false, fieldErrorPart: String = ""): Mapping[LocalDate] = tuple(
    "day" -> nonEmptyTextOr(
      prefix + ".day", text.verifying(Errors.invalidDate, x => x.trim.forall(Character.isDigit) && x.trim.toInt >= 1 && x.trim.toInt <= 31),
      s"error$fieldErrorPart.day.required"
    ),
    "month" -> nonEmptyTextOr(
      prefix + ".month", text.verifying(Errors.invalidDate, x => x.trim.forall(Character.isDigit) && x.trim.toInt >= 1 && x.trim.toInt <= 12),
      s"error$fieldErrorPart.month.required"
    ),
    "year" -> nonEmptyTextOr(
      prefix + ".year", text.verifying(Errors.invalidDate, x => x.trim.forall(Character.isDigit) && x.trim.length == 4),
      s"error$fieldErrorPart.year.required"
    )).verifying(
      if (allowFutureDates) fullDateIsAfter1900(fieldErrorPart) else fullDateIsInPastAndAfter1900(fieldErrorPart)
    ).transform(
  { case (day, month, year) => LocalDate.of(year.trim.toInt, month.trim.toInt, day.trim.toInt) },
  (date: LocalDate) => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
  )

  def monthsYearDurationMapping(prefix: String, fieldErrorPart: String = ""): Mapping[MonthsYearDuration] = tuple(
    "years" -> nonEmptyTextOr(
      prefix + ".years", text.verifying(Errors.invalidDurationYears, x => x.trim.forall(Character.isDigit) && x.trim.toInt >= 0 && x.trim.toInt <= 999),
      s"error$fieldErrorPart.years.required"
    ),
    "months" -> nonEmptyTextOr(
      prefix + ".months", text.verifying(Errors.invalidDurationMonths, x => x.trim.forall(Character.isDigit) && x.trim.toInt >= 0 && x.trim.toInt <= 12),
      s"error$fieldErrorPart.months.required"
    )
  ).transform({
    case (years, months) => MonthsYearDuration(months.trim.toInt, years.trim.toInt)
  }, (my: MonthsYearDuration) => (my.months.toString, my.years.toString))

  def dateIsBeforeAnotherDate(roughDateMapping: Mapping[RoughDate], anotherDateKey: String, errorMsgKey: String): Mapping[RoughDate] =
    CompareWithAnotherDate(roughDateMapping, anotherDateKey, _.isBefore(_), errorMsgKey)

  def dateIsAfterAnotherDate(roughDateMapping: Mapping[RoughDate], anotherDateKey: String, errorMsgKey: String): Mapping[RoughDate] =
    CompareWithAnotherDate(roughDateMapping, anotherDateKey, _.isAfter(_), errorMsgKey)

  case class CompareWithAnotherDate(roughDateMapping: Mapping[RoughDate], anotherDateKey: String,
                                    compare: (java.time.LocalDate, java.time.LocalDate) => Boolean, errorMsgKey: String,
                                    additionalConstraints: Seq[Constraint[RoughDate]] = Nil) extends Mapping[RoughDate] {

    override val format: Option[(String, Seq[Any])] = roughDateMapping.format

    val key = roughDateMapping.key

    val constraints: Seq[Constraint[RoughDate]] = additionalConstraints

    override def verifying(constraints: Constraint[RoughDate]*): Mapping[RoughDate] =
      copy(additionalConstraints = additionalConstraints ++ constraints)

    def bind(data: Map[String, String]): Either[Seq[FormError], RoughDate] = {
      val anotherDateOpt = data.get(anotherDateKey).map(java.time.LocalDate.parse)
      roughDateMapping.bind(data).flatMap { date =>
        if (anotherDateOpt.exists(anotherDate => compare(date.toLocalDate, anotherDate))) {
          Left(Seq(FormError(key, errorMsgKey)))
        } else {
          Right(date)
        }
      }
    }

    def unbind(value: RoughDate): Map[String, String] =
      roughDateMapping.unbind(value)

    def unbindAndValidate(value: RoughDate): (Map[String, String], Seq[FormError]) =
      roughDateMapping.unbindAndValidate(value)

    def withPrefix(prefix: String): Mapping[RoughDate] = {
      copy(roughDateMapping = roughDateMapping.withPrefix(prefix))
    }

    val mappings: Seq[Mapping[_]] = roughDateMapping.mappings :+ this

  }

}
