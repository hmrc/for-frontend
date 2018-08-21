/*
 * Copyright 2018 HM Revenue & Customs
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

package models.serviceContracts.submissions

import models.RoughDate
import org.joda.time.LocalDate

case class LeaseOrAgreement (
  leaseAgreementType: LeaseAgreementType,
  leaseAgreementHasBreakClause: Option[Boolean],
  breakClauseDetails: Option[String],
  agreementIsStepped: Option[Boolean],
  steppedDetails: List[SteppedDetails] = List(),
  startDate: Option[RoughDate],
  rentOpenEnded: Option[Boolean],
  leaseLength: Option[MonthsYearDuration])

case class SteppedDetails(stepFrom: LocalDate, stepTo: LocalDate, amount: BigDecimal)
