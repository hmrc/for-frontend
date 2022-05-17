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
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.data.Forms.{default, mapping, optional, text, tuple}
import play.api.data._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.validation.Constraints.{maxLength, nonEmpty}
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

  def toDateIsAfterFromDate: Constraint[(LocalDate, LocalDate)] = Constraint("constraints.steppedDetails.toAfterFrom") {
    case (stepFrom, stepTo) =>
      val condTo = stepTo.isAfter(stepFrom)
      createFieldConstraintFor(condTo, Errors.toDateIsAfterFromDate, Seq(s"${keys.to}.day"))

  }

  def toDateIsAfterTenYears: Constraint[(LocalDate, LocalDate)] =  Constraint("constraints.steppedDetails.invalidRange") {
    case (stepFrom, stepTo) =>
      val dateFormatPattern = DateTimeFormat.forPattern("d MMMM YYYY")
      val maxFutureDate = stepFrom.plusYears(10)

      if (stepTo.isBefore(maxFutureDate.plusDays(1)))
        Valid
      else
        Invalid(Seq(createFieldValidationError(s"${keys.to}.day", Errors.toDateToFarFuture,
          dateFormatPattern.print(stepFrom.plusDays(1)), dateFormatPattern.print(maxFutureDate))))
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
    index -> tuple(
      keys.from -> dateFieldsMapping(s"$index.stepFrom", allowFutureDates = true, fieldErrorPart = ".writtenAgreement.steppedDetails.stepFrom"),
      keys.to -> dateFieldsMapping(s"$index.stepTo", allowFutureDates = true, fieldErrorPart = ".writtenAgreement.steppedDetails.stepTo")
    ).verifying(toDateIsAfterFromDate, toDateIsAfterTenYears),
    (index + "." + keys.amount) -> currencyMapping(".writtenAgreement.steppedDetails.amount")
  )((dates, amount) => SteppedDetails(dates._1, dates._2, amount))(stepped => Some(((stepped.stepFrom, stepped.stepTo), stepped.amount)))

  val written = keys.writtenAgreement

  val steppedDetailsListMapping = IndexedMapping(s"$written.steppedDetails", steppedDetailsMapping, alwaysValidateFirstIndex = true)
    .verifying(Errors.tooManySteppedRents, _.length <= 7)

  val writtenAgreementMapping = mapping(
    keys.startDate -> monthYearRoughDateMapping(s"$written.${keys.startDate}", ".writtenAgreement.startDate"),
    keys.rentOpenEnded -> mandatoryBooleanWithError(Errors.leaseAgreementOpenEndedRequired),
    keys.leaseLength -> mandatoryIfFalse(s"$written.${keys.rentOpenEnded}",
      monthsYearDurationMapping(s"$written.${keys.leaseLength}", ".writtenAgreement.leaseLength")),
    keys.leaseAgreementHasBreakClause -> mandatoryBooleanWithError(Errors.leaseAgreementBreakClauseRequired),
    keys.breakClauseDetails -> mandatoryIfTrue(s"$written.${keys.leaseAgreementHasBreakClause}",
      default(text, "").verifying(
        nonEmpty(errorMessage = "error.writtenAgreement.breakClauseDetails.required"),
        maxLength(124, "error.writtenAgreement.breakClauseDetails.maxLength")
      )
    ),
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
