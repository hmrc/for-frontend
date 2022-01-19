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

import form.DateMappings._
import form.MappingSupport._
import models.pages.{PageSix, _}
import models.serviceContracts.submissions._
import play.api.data.Forms.{mapping, nonEmptyText, optional}
import play.api.data._
import play.api.data.validation.Constraint
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._

object PageSixForm {

  val keys = new {
    val leaseAgreementType = "leaseAgreementType"
    val writtenAgreement = "writtenAgreement"
    val verbalAgreement = "verbalAgreement"
    val leaseAgreementHasBreakClause = "leaseAgreementHasBreakClause"
    val agreementIsStepped = "agreementIsStepped"
    val breakClauseDetails = "breakClauseDetails"
    val steppedDetails = "steppedDetails"
    val startDate = "startDate"
    val leaseLength = "leaseLength"
    val from = "stepFrom"
    val to = "stepTo"
    val amount = "amount"
    val rentOpenEnded = "rentOpenEnded"
  }

  def toDateIsAfterFromDate(index: String): Constraint[SteppedDetails] = Constraint("constraints.steppedDetails.toAfterFrom") { steppedDetails => {
    val cond = steppedDetails.stepTo.isAfter(steppedDetails.stepFrom)
    createFieldConstraintFor(cond, Errors.toDateIsAfterFromDate, Seq(s"$index.${keys.to}.day"))
  }
  }

  def noOverlappingSteps: Constraint[WrittenAgreement] = Constraint("constraints.steppedDetails.overlappingSteps") { writtenAgreement => {
    val steppedDetails = writtenAgreement.steppedDetails
    lazy val s = steppedDetails.zipWithIndex.tail.filterNot { p =>
      p._1.stepFrom.isAfter(steppedDetails(p._2 - 1).stepTo) || p._1.stepFrom.isEqual(steppedDetails(p._2 - 1).stepTo)
    }

    lazy val f = s.map { p =>
      s"steppedDetails[${p._2}].${keys.from}.day"
    }
    createFieldConstraintFor(steppedDetails.isEmpty || s.isEmpty, Errors.overlappingDates,
      if(steppedDetails.nonEmpty && f.nonEmpty) f else Seq("needs one field to run"))
  }
  }

  val steppedDetailsMapping = (index: String) => mapping(
    (index + "." + keys.from) -> dateFieldsMapping(s"$index.stepFrom", allowFutureDates = true),
    (index + "." + keys.to) -> dateFieldsMapping(s"$index.stepTo", allowFutureDates = true),
    (index + "." + keys.amount) -> currency
  )(SteppedDetails.apply)(SteppedDetails.unapply).verifying(toDateIsAfterFromDate(index))

  val written = keys.writtenAgreement

  val steppedDetailsListMapping = IndexedMapping(s"$written.steppedDetails", steppedDetailsMapping).verifying(Errors.tooManySteppedRents, _.length <= 7)

  val writtenAgreementMapping = mapping(
    keys.startDate -> monthYearRoughDateMapping(s"$written.${keys.startDate}"),
    keys.rentOpenEnded -> mandatoryBooleanWithError(Errors.leaseAgreementOpenEndedRequired),
    keys.leaseLength -> mandatoryIfFalse(s"$written.${keys.rentOpenEnded}", monthsYearDurationMapping(s"$written.${keys.leaseLength}")),
    keys.leaseAgreementHasBreakClause -> mandatoryBooleanWithError(Errors.leaseAgreementBreakClauseRequired),
    keys.breakClauseDetails -> mandatoryIfTrue(s"$written.${keys.leaseAgreementHasBreakClause}", nonEmptyText(maxLength = 124)),
    keys.agreementIsStepped -> mandatoryBooleanWithError(Errors.leaseAgreementIsSteppedRequired),
    keys.steppedDetails -> onlyIfTrue(s"$written.${keys.agreementIsStepped}", steppedDetailsListMapping)
  )(WrittenAgreement.apply)(WrittenAgreement.unapply).verifying(noOverlappingSteps)

  val verbal = keys.verbalAgreement

  val verbalAgreementMapping = mapping(
    keys.startDate -> optional(monthYearRoughDateMapping(s"$verbal.${keys.startDate}")),
    keys.rentOpenEnded -> optional(mandatoryBooleanWithError(Errors.leaseAgreementOpenEndedRequired)),
    keys.leaseLength -> mandatoryIfFalse(s"$verbal.${keys.rentOpenEnded}", monthsYearDurationMapping(s"$verbal.${keys.leaseLength}"))
  )(VerbalAgreement.apply)(VerbalAgreement.unapply)

  val writtenAgreements = Seq(LeaseAgreementTypesLeaseTenancy.name, LeaseAgreementTypesLicenceOther.name)
  val pageSixMapping = mapping(
    keys.leaseAgreementType -> leaseAgreementTypeMapping,
    keys.writtenAgreement -> mandatoryIfAnyOf(keys.leaseAgreementType, writtenAgreements, writtenAgreementMapping),
    keys.verbalAgreement -> onlyIf(isEqual(keys.leaseAgreementType, LeaseAgreementTypesVerbal.name), verbalAgreementMapping)(VerbalAgreement())
  )(PageSix.apply)(PageSix.unapply)

  val pageSixForm = Form(pageSixMapping)
}
