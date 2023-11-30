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

package util

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @author Yuriy Tumakha
 */
object DateUtil {

  val ukTimezone: ZoneId = ZoneId.of("Europe/London")

  private val dayMonthYearExampleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d M yyyy", Locale.UK)
  private val monthYearExampleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M yyyy", Locale.UK)

  val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
  val isoZonedDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.UK)

  def nowInUK: ZonedDateTime = ZonedDateTime.now(ukTimezone)

  def exampleDayMonthYear(minusYears: Int): String =
    nowInUK.minusYears(minusYears).format(dayMonthYearExampleFormatter)

  def exampleMonthYear(minusYears: Int): String =
    nowInUK.minusYears(minusYears).format(monthYearExampleFormatter)

}
