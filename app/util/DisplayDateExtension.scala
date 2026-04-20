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

package util

import models.RoughDate
import models.serviceContracts.submissions.MonthsYearDuration

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
  * @author Yuriy Tumakha
  */
val displayFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")

extension (localDate: LocalDate)

  def display: String =
    localDate.format(displayFormatter)

extension (monthsYears: MonthsYearDuration)

  def display: String =
    s"${monthsYears.years} years, ${monthsYears.months} months"

extension (roughDate: RoughDate)

  def display: String =
    Seq(
      roughDate.day,
      roughDate.month,
      Some(roughDate.year)
    ).flatten.mkString("/")
