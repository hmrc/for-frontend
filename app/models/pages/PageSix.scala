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

package models.pages

import models.RoughDate
import models.serviceContracts.submissions.{LeaseAgreementType, MonthsYearDuration, SteppedDetails}

import java.time.LocalDate

case class PageSix (
  leaseAgreementType: LeaseAgreementType,
  writtenAgreementDetails: Option[WrittenAgreement],
  verbalAgreementDetails: VerbalAgreement,
  lastReviewDate: Option[LocalDate] = None,
  rentReviewDate: Option[LocalDate] = None
)

case class WrittenAgreement(
  startDate: RoughDate, rentOpenEnded: Boolean, leaseLength: Option[MonthsYearDuration],
  leaseAgreementHasBreakClause: Boolean, breakClauseDetails: Option[String] = None,
  agreementIsStepped: Boolean, steppedDetails: List[SteppedDetails] = List())

case class VerbalAgreement (
  startDate: Option[RoughDate] = None,
  rentOpenEnded: Option[Boolean] = None,
  leaseLength: Option[MonthsYearDuration] = None
)
