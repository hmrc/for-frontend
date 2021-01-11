/*
 * Copyright 2021 HM Revenue & Customs
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

import models._
import models.pages._
import models.serviceContracts.submissions._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FlatSpec, Matchers}
import play.api.data.Form


class PageSixMappingSpec extends FlatSpec with Matchers {

  import PageSixForm._
  import TestData._
  import utils.FormBindingTestAssertions._
  import utils.MappingSpecs._

 "A page six form" should "bind to page six with a written agreement" in {
    val p6 = PageSix(
      LeaseAgreementTypesLicenceOther,
      Some(WrittenAgreement(
        leaseAgreementHasBreakClause = true,
        breakClauseDetails = Some("BREAK CLAUSE DETAILS"),
        agreementIsStepped = true,
        steppedDetails = List(SteppedDetails(stepFrom = new LocalDate(1900, 12, 2), stepTo = new LocalDate(2018, 2, 12), amount = 123.45)),
        startDate = new RoughDate(month = 3, year = 2013),
        rentOpenEnded = false,
        leaseLength = Some(MonthsYearDuration(months = 4, years = 3))
      )),
      VerbalAgreement()
    )

    mustBind(bind(fullData)) { x => assert(x === p6)}
  }

  it should "always require a lease agreement type" in {
    val data = Map.empty[String, String]
    val form = bind(data)

    mustOnlyContainError(keys.leaseAgreementType, Errors.leaseAgreementTypeRequired, form)
  }

  it should "allow all fields to be optional when the agreement is verbal or written" in {
    val data = Map(keys.leaseAgreementType -> LeaseAgreementTypesVerbal.name)

    mustBind(bind(data)) { x => assert(x === PageSix(LeaseAgreementTypesVerbal, None, VerbalAgreement())) }
  }

  it should "allow a start and open ended value if there is a verbal agreement or one in writing" in {
    val data = Map(
      keys.leaseAgreementType -> LeaseAgreementTypesVerbal.name,
      s"$verbalStartDate.month" -> "5",
      s"$verbalStartDate.year" -> "2011",
      verbalRentOpenEnded -> "false",
      s"$verbalLeaseLength.years" -> "3",
      s"$verbalLeaseLength.months" -> "4"
    )
    val verbal = VerbalAgreement(Some(RoughDate(None, Some(5), 2011)), Some(false), Some(MonthsYearDuration(months = 4, years = 3)))
    val lease = PageSix(LeaseAgreementTypesVerbal, None, verbal)

    mustBind(bind(data)) { x => assert(x === lease)}
  }

  it should "require a lease length if the agreement is not open ended for a written contract" in {
    val data = Map(
      keys.leaseAgreementType -> LeaseAgreementTypesLeaseTenancy.name,
      writtenRentOpenEnded -> "false"
    )
    val form = bind(data)

    mustContainRequiredErrorFor(writtenLeaseLength + ".months", form)
    mustContainRequiredErrorFor(writtenLeaseLength + ".years", form)
  }

  it should "require a lease length if the agreement is not open ended for a verbal contract" in {
    val data = Map(
      keys.leaseAgreementType -> LeaseAgreementTypesVerbal.name,
      verbalRentOpenEnded -> "false"
    )
    val form = bind(data)

    mustContainRequiredErrorFor(verbalLeaseLength + ".months", form)
    mustContainRequiredErrorFor(verbalLeaseLength + ".years", form)
  }

  it should "require all fields to be mandatory if the lease agreement type is a tenancy agreement" in {
    val data = Map("leaseAgreementType" -> LeaseAgreementTypesLeaseTenancy.name)
    val form = bind(data)

    mustContainError(writtenLeaseAgreementHasBreakClause, Errors.leaseAgreementBreakClauseRequired, form)
    mustContainError(writtenAgreementIsStepped, Errors.leaseAgreementIsSteppedRequired, form)
    mustContainRequiredErrorFor(writtenStartDate + ".year", form)
    mustContainRequiredErrorFor(writtenStartDate + ".month", form)
    mustContainError(writtenRentOpenEnded, Errors.leaseAgreementOpenEndedRequired, form)
  }

  it should "require all fields to be mandatory if the lease agreement type is other type of written agreement" in {
    val data = Map("leaseAgreementType" -> LeaseAgreementTypesLicenceOther.name)
    val form = bind(data)

    mustContainError(writtenLeaseAgreementHasBreakClause, Errors.leaseAgreementBreakClauseRequired, form)
    mustContainError(writtenAgreementIsStepped, Errors.leaseAgreementIsSteppedRequired, form)
    mustContainRequiredErrorFor(writtenStartDate + ".year", form)
    mustContainRequiredErrorFor(writtenStartDate + ".month", form)
    mustContainError(writtenRentOpenEnded, Errors.leaseAgreementOpenEndedRequired, form)
  }

  it should "return an break clause required error when 'has break clause' is true and there are no break clause details" in {
    val testData = fullData.updated(keys.leaseAgreementType, LeaseAgreementTypesLicenceOther.name) - writtenBreakClauseDetails
    val form = bind(testData)

    mustContainRequiredErrorFor(writtenBreakClauseDetails, form)
  }

  it should "return a stepped details error when 'has stepped agreement' is true but there are no stepped agreement details" in {
    val testData = fullData.updated(writtenAgreementIsStepped, "true") - s"${keys.writtenAgreement}.steppedDetails[0].stepTo.day" - s"${keys.writtenAgreement}.steppedDetails[0].stepTo.month" - s"${keys.writtenAgreement}.steppedDetails[0].stepTo.year"

    val res = bind(testData)
    res.errors.size should be(3)
  }

  it should "return a stepped details error when 'has stepped agreement' is true and stepped agreement details are missing" in {
    val testData = fullData.updated(writtenAgreementIsStepped, "true") - s"${keys.writtenAgreement}.steppedDetails[0].stepFrom.day" - s"${keys.writtenAgreement}.steppedDetails[0].stepFrom.month" - s"${keys.writtenAgreement}.steppedDetails[0].stepFrom.year"

    val res = bind(testData)
    res.errors.size should be(3)
  }

  it should "return a sepped price details error if 'has stepped agreement' is true but stepped agreement price details are missing" in {
    val testData = fullData.updated(writtenAgreementIsStepped, "true") - getKeyStepped(0).amount

    val form = bind(testData)
    mustOnlyContainRequiredErrorFor(s"${keys.writtenAgreement}.steppedDetails[0].amount", form)
  }

  it should "require a lease length when a rent agreed date is supplied" in {
    val d = fullData - writtenLeaseYears - writtenLeaseMonths

    val form = bind(d)
    mustContainRequiredErrorFor(writtenLeaseYears, form)
    mustContainRequiredErrorFor(writtenLeaseMonths, form)
    form.errors.size should be(2)
  }

  it should "not require a lease length when no rent agreed date is supplied" in {
    val d = fullData.updated(writtenRentOpenEnded, "true") - writtenLeaseYears - writtenLeaseMonths

    doesNotContainErrors(bind(d))
  }

  it should "not validate verbal contract fields if a written agreement is chosen" in {
    val d = fullData.updated(verbalRentOpenEnded, "false") - verbalLeaseYears - verbalLeaseMonths

    doesNotContainErrors(bind(d))
  }

  it should "validate the lease agreement start date as a date in the past" in {
    validatePastDate(writtenStartDate, pageSixForm, fullData)
  }

  it should "validate the lease duration" in {
    validatesDuration(writtenLeaseLength, pageSixForm, fullData)
  }

  it should "validate the break clause details as free text" in {
    validateLettersNumsSpecCharsUptoLength(writtenBreakClauseDetails, 124, pageSixForm, fullData)
  }

  it should "validate stepped rent amount as currency allowing non-negative amounts" in {
    validateCurrency(getKeyStepped(0).amount, pageSixForm, fullData)
  }

  it should "validate stepped rent from date as a date" in {
    val formData = fullData + (getKeyStepped(0).stepTo + ".year" -> DateTime.now().plusYears(1).getYear.toString)
    validateDate(getKeyStepped(0).stepFrom, pageSixForm, formData)
  }

  it should "validate the second stepped rent step amount as currency" in {
    validateCurrency(getKeyStepped(1).amount, pageSixForm, fullDataWithSecondRentStep)
  }

  it should "not allow more than 7 stepped rents" in {
    val with7SteppedRents = addSteppedRents(6, fullData)
    mustBind(bind(with7SteppedRents)) { _ => () }
    
    val with8SteppedRents = addSteppedRents(7, fullData)
    val form = bind(with8SteppedRents)
    mustOnlyContainError(s"${keys.writtenAgreement}.steppedDetails", Errors.tooManySteppedRents, form)
  }

  it should "validate the step to date is not before the step from date" in {
    val data = fullData.updated(getKeyStepped(0).stepFrom +".year", "2019")
    val f = bind(data)
    mustContainPrefixedError(s"${keys.writtenAgreement}.steppedDetails[0].stepTo.day",Errors.toDateIsAfterFromDate,f)
  }

  it should "validate the stepped rent dates do not overlap" in {
    val data = fullDataWithSecondRentStep.updated(
      getKeyStepped(0).stepTo + ".year", "2019"
    )
    val f = bind(data)
    mustContainPrefixedError(s"${keys.writtenAgreement}.steppedDetails[1].stepFrom.day", Errors.overlappingDates, f)
  }

  object TestData {
    val writtenLeaseYears = s"${keys.writtenAgreement}.${keys.leaseLength}.years"
    val writtenLeaseMonths = s"${keys.writtenAgreement}.${keys.leaseLength}.months"
    val writtenStartDate = s"${keys.writtenAgreement}.${keys.startDate}"
    val writtenLeaseAgreementHasBreakClause = s"${keys.writtenAgreement}.${keys.leaseAgreementHasBreakClause}"
    val writtenAgreementIsStepped = s"${keys.writtenAgreement}.${keys.agreementIsStepped}"
    val writtenLeaseLength = s"${keys.writtenAgreement}.${keys.leaseLength}"
    val writtenBreakClauseDetails = s"${keys.writtenAgreement}.${keys.breakClauseDetails}"
    val writtenRentOpenEnded = s"${keys.writtenAgreement}.${keys.rentOpenEnded}"
    val verbalRentOpenEnded = s"${keys.verbalAgreement}.${keys.rentOpenEnded}"
    val verbalStartDate = s"${keys.verbalAgreement}.${keys.startDate}"
    val verbalLeaseLength = s"${keys.verbalAgreement}.${keys.leaseLength}"
    val verbalLeaseYears = s"${keys.verbalAgreement}.${keys.leaseLength}.years"
    val verbalLeaseMonths = s"${keys.verbalAgreement}.${keys.leaseLength}.months"

    def bind(dataMap: Map[String, String]): Form[PageSix] = {
      val bound = pageSixForm.bind(dataMap)
      bound.convertGlobalToFieldErrors()
    }

    def getKeyStepped(idx: Int) = new {
      val stepFrom = s"${keys.writtenAgreement}.steppedDetails[$idx].stepFrom"
      val stepTo = s"${keys.writtenAgreement}.steppedDetails[$idx].stepTo"
      val amount = s"${keys.writtenAgreement}.steppedDetails[$idx].amount"
    }

    val fullData: Map[String, String] = Map(
      keys.leaseAgreementType -> LeaseAgreementTypesLicenceOther.name,
      writtenLeaseAgreementHasBreakClause -> "true",
      writtenBreakClauseDetails -> "BREAK CLAUSE DETAILS",
      writtenAgreementIsStepped -> "true",
      getKeyStepped(0).stepFrom + ".day" -> "2",
      getKeyStepped(0).stepFrom + ".month" -> "12",
      getKeyStepped(0).stepFrom + ".year" -> "1900",
      getKeyStepped(0).stepTo + ".day" -> "12",
      getKeyStepped(0).stepTo + ".month" -> "2",
      getKeyStepped(0).stepTo + ".year" -> "2018",
      getKeyStepped(0).amount -> "123.45",
      writtenStartDate + ".month" -> "3",
      writtenStartDate + ".year" -> "2013",
      writtenRentOpenEnded -> "false",
      writtenLeaseLength + ".years" -> "3",
      writtenLeaseLength + ".months" -> "4")

    val fullDataWithSecondRentStep = fullData.
      updated(getKeyStepped(1).amount, "456.78").
      updated(getKeyStepped(1).stepFrom + ".day", "1").
      updated(getKeyStepped(1).stepFrom + ".month", "1").
      updated(getKeyStepped(1).stepFrom + ".year", "2019").
      updated(getKeyStepped(1).stepTo + ".day", "1").
      updated(getKeyStepped(1).stepTo + ".month", "1").
      updated(getKeyStepped(1).stepTo + ".year", "2020")

    def addSteppedRents(n: Int, data: Map[String, String]): Map[String, String] = {
      (1 to n).foldLeft(data) { (s, v) =>
        s.updated(getKeyStepped(v).amount, "456.78")
         .updated(getKeyStepped(v).stepFrom + ".day", "1")
         .updated(getKeyStepped(v).stepFrom + ".month", "1")
         .updated(getKeyStepped(v).stepFrom + ".year", (v + 2021).toString)
         .updated(getKeyStepped(v).stepTo + ".day", "1")
         .updated(getKeyStepped(v).stepTo + ".month", "1")
         .updated(getKeyStepped(v).stepTo + ".year", (v + 2022).toString)
      }
    }
  }
}
