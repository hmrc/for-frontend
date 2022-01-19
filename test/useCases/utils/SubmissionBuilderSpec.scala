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

package useCases.utils

import connectors.{Document, Page}
import form.PageThreeForm
import models._
import models.serviceContracts.submissions._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.OptionValues._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import useCases.DefaultSubmissionBuilder

class SubmissionBuilderSpec extends AnyFlatSpec with should.Matchers {

  import TestData._

  behavior of "Submission builder"

  it should "build submissions from in-progress documents" in {
    assert(new DefaultSubmissionBuilder().build(doc1) === submission1)
  }

  it should "leave pages as none where there is no data for them" in {
    assert(new DefaultSubmissionBuilder().build(doc2) === submission2)
  }

  it should "assign the correct data to the correct page, no matter what order the pages are supplied in" in {
    assert(new DefaultSubmissionBuilder().build(doc3) === submission1)
  }

  it should "parse a verbal lease agreement when page six is a verbal agreement" in {
    assert(new DefaultSubmissionBuilder().build(docWithVerbalAgreement) === submissionWithVerbalAgreement)
  }

/*  it should "calculate an annual rent from a weekly rent when mapping rent" in {
    val sub = SubmissionBuilder.build(docWithWeeklyRent)
    val annualRent = sub.rent.flatMap(_.annualRentExcludingVat).value
    assert(annualRent === 5200)
  }

  it should "calculate an annual rent from a monthly rent when mapping rent" in {
    val sub = SubmissionBuilder.build(docWithMonthlyRent)
    val annualRent = sub.rent.flatMap(_.annualRentExcludingVat).value
    assert(annualRent === 60000)
  }

  it should "calculate an annual rent from a quarterly rent when mapping rent" in {
    val sub = SubmissionBuilder.build(docWithQuarterlyRent)
    val annualRent = sub.rent.flatMap(_.annualRentExcludingVat).value
    assert(annualRent === 1000)
  }*/

  it should "create ndr and water charges services if their details are supplied" in {
    val sub = new DefaultSubmissionBuilder().build(docWithNdrChargesAndWaterCharges)
    val services = sub.responsibilities.get.includedServicesDetails
    assert(services.exists(s => s.chargeDescription == "Non-domestic Rates" && s.chargeCost == 41.23))
    assert(services.exists(s => s.chargeDescription == "Water Charges" && s.chargeCost == 456.76))
  }

  it should "map overriden property address as tenants address when tenants address is main property address and main property address has been overriden" in {
    val sub = new DefaultSubmissionBuilder().build(docWithTenantsPropertyAddressAndOverridenMainAddress)
    assert(sub.sublet.map(_.sublets.head.tenantAddress).value === tenantsPropertyAddress)
  }

  it should "set occupier name as 'Nobody' if the property is not occupied" in {
    val ks = PageThreeForm.keys
    val p3 = page3FormData.updated(ks.occupierType, Seq(OccupierTypeNobody.name))
    val sub = new DefaultSubmissionBuilder().build(doc1.add(Page(3, p3)))
    assert(sub.theProperty.flatMap(_.occupierName).value === "Nobody")
  }

  it should "set occupier name as first 50 chars of (company name + contact name) if a company occupies the property" in {
    val ks = PageThreeForm.keys
    val p3 = page3FormData.updated(ks.occupierType, Seq(OccupierTypeCompany.name))
    .updated(ks.occupierCompanyName, Seq("Jimmy Choo Enterprise Integration Ventures"))
    .updated(ks.occupierCompanyContact, Seq("Kyle Kingsbury"))
    val sub = new DefaultSubmissionBuilder().build(doc1.add(Page(3, p3)))
    assert(sub.theProperty.flatMap(_.occupierName).value === "Jimmy Choo Enterprise Integration Ventures - Kyle ")
  }

  it should "set occupier name as main occupier's name if 'one or more individuals' occupies the property" in {
    val ks = PageThreeForm.keys
    val p3 = page3FormData.updated(ks.occupierType, Seq(OccupierTypeIndividuals.name))
    .updated(ks.mainOccupierName, Seq("Jimmy Choo"))
    val sub = new DefaultSubmissionBuilder().build(doc1.add(Page(3, p3)))
    assert(sub.theProperty.flatMap(_.occupierName).value === "Jimmy Choo")
  }

  it should "remove all non-short path information if the user is on the short path (they may have previoysly been on a different path)" in {
    val fullSubmission = doc1
    val convertedToShortPath = fullSubmission.add(Page(3, page3ShortPath))
    val sub = new DefaultSubmissionBuilder().build(convertedToShortPath)
    assert(sub.landlord.isEmpty)
    assert(sub.lease.isEmpty)
    assert(sub.rentReviews.isEmpty)
    assert(sub.rentAgreement.isEmpty)
    assert(sub.rent.isEmpty)
    assert(sub.rentIncludes.isEmpty)
    assert(sub.incentives.isEmpty)
    assert(sub.responsibilities.isEmpty)
    assert(sub.alterations.isEmpty)
    assert(sub.otherFactors.isEmpty)
  }

  object TestData {
    lazy val tenantsPropertyAddress = Address("1", Some("The Street"), Some("worthing"), "AA11 1AA")
    lazy val doc1 = Document(
      "refNum1", DateTime.now, Seq(
        Page(1, page1FormData),
        Page(2, page2FormData),
        Page(3, page3FormData),
        Page(4, page4FormData),
        Page(5, page5FormData),
        Page(6, page6FormData),
        Page(7, page7FormData),
        Page(8, page8FormData),
        Page(9, page9FormData),
        Page(10, page10FormData),
        Page(11, page11FormData),
        Page(12, page12FormData),
        Page(13, page13FormData),
        Page(14, page14FormData)),
      address = Some(defaultAddress)
    )
    lazy val docWithVerbalAgreement = Document(
      "refNum1", DateTime.now, Seq(
        Page(1, page1FormData),
        Page(2, page2FormData),
        Page(3, page3FormData),
        Page(4, page4FormData),
        Page(5, page5FormData),
        Page(6, page6VerbalData),
        Page(7, page7FormData),
        Page(8, page8FormData),
        Page(9, page9FormData),
        Page(10, page10FormData),
        Page(11, page11FormData),
        Page(12, page12FormData),
        Page(13, page13FormData),
        Page(14, page14FormData)),
      address = Some(defaultAddress)
    )
    lazy val docWithWeeklyRent = doc1.add(Page(9, page9WeeklyRentData))
    lazy val docWithMonthlyRent = doc1.add(Page(9, page9MonthlyRentData))
    lazy val docWithQuarterlyRent = doc1.add(Page(9, page9QuarterlyRentData))

    lazy val docWithNdrChargesAndWaterCharges = doc1.add(Page(12, page12NdrAndWaterChargesData))
    lazy val docWithTenantsPropertyAddressAndOverridenMainAddress = doc1.add(Page(1, page1AlternativeAddressData))
    .add(Page(4, page4TenantsPropertyAddressData))
    lazy val docWithTenantsPropertyAddressAndNoOverridenMainAddress = doc1.add(Page(4, page4TenantsPropertyAddressData))
    lazy val submission1 = Submission(
      propertyAddress, Some(customerDetails), Some(theProperty), Some(sublet), Some(landlord),
      Some(leaseOrAgreement), Some(rentReviews), Some(rentAgreement), Some(rent), Some(whatRentIncludes),
      Some(incentivesAndPayments), Some(responsibilities), Some(propertyAlterations), Some(otherFactors),
      referenceNumber = Some("refNum1"))
    lazy val submissionWithVerbalAgreement = submission1.copy(lease = Some(verbalLease))
    lazy val doc2 = doc1.copy(pages = doc1.pages.take(10))
    lazy val submission2 = submission1.copy(
      incentives = None, responsibilities = None, alterations = None, otherFactors = None, referenceNumber = Some("refNum1")
    )
    lazy val doc3 = Document(
      "refNum1", DateTime.now, Seq(
        Page(1,  page1FormData),
        Page(11, page11FormData),
        Page(3,  page3FormData),
        Page(4,  page4FormData),
        Page(5,  page5FormData),
        Page(7,  page7FormData),
        Page(8,  page8FormData),
        Page(2,  page2FormData),
        Page(9,  page9FormData),
        Page(10, page10FormData),
        Page(12, page12FormData),
        Page(13, page13FormData),
        Page(6,  page6FormData),
        Page(14, page14FormData)),
      address = Some(defaultAddress)
    )
    lazy val page1FormData = Map("isAddressCorrect" -> Seq("true"))

    lazy val page1AlternativeAddressData = Map(
      "isAddressCorrect" -> Seq("false"),
	 "address.buildingNameNumber" -> Seq("1"),
      "address.street1" -> Seq("The Street"),
	 "address.street2" -> Seq("worthing"),
	 "address.postcode" -> Seq("AA11 1AA")
    )

    lazy val page2FormData = Map(
	"fullName" -> Seq("fn"),
	 "userType" -> Seq("occupier"),
	 "contactType" -> Seq("email"),
      "contactDetails.email1" -> Seq("abc@mailinator.com"),
	 "contactDetails.email2" -> Seq("abc@mailinator.com"),
      "contactAddressType" -> Seq("mainAddress"))

    val p3ks = PageThreeForm.keys
    lazy val page3FormData = Map(
	p3ks.propertyType -> Seq("Stuff"),
      p3ks.occupierType -> Seq("individuals"),
      p3ks.mainOccupierName -> Seq("Mike Ington"),
      p3ks.occupierCompanyName -> Seq("company name"),
 p3ks.firstOccupationDate + ".month" -> Seq("7"),
      p3ks.firstOccupationDate + ".year" -> Seq("2013"),
	 p3ks.propertyOwnedByYou -> Seq("false"),
      p3ks.propertyRentedByYou -> Seq("true"),
	 p3ks.noRentDetails -> Seq("Coz I live with my rents!"))

    lazy val page3ShortPath = Map(
      p3ks.propertyType -> Seq("Stuff"),
      p3ks.occupierType -> Seq("individuals"),
      p3ks.mainOccupierName -> Seq("Mike Ington"),
      p3ks.occupierCompanyName -> Seq("company name"),
      p3ks.firstOccupationDate + ".month" -> Seq("7"),
      p3ks.firstOccupationDate + ".year" -> Seq("2013"),
      p3ks.propertyOwnedByYou -> Seq("false"),
      p3ks.propertyRentedByYou -> Seq("false"),
      p3ks.noRentDetails -> Seq("Coz I live with my rents!"))

    lazy val page4FormData = Map(
      "propertyIsSublet" -> Seq("true"),
      "sublet[0].tenantFullName" -> Seq("Jake Smythe"),
      "sublet[0].tenantAddress.buildingNameNumber" -> Seq("Some Company"),
      "sublet[0].tenantAddress.street1" -> Seq("Some Road"),
      "sublet[0].tenantAddress.street2" -> Seq(""),
      "sublet[0].tenantAddress.postcode" -> Seq("AA11 1AA"),
      "sublet[0].subletType" -> Seq("part"),
      "sublet[0].subletPropertyPartDescription" -> Seq("basement"),
      "sublet[0].subletPropertyReasonDescription" -> Seq("commercial"),
      "sublet[0].annualRent" -> Seq("200"),
      "sublet[0].rentFixedDate.month" -> Seq("2"),
      "sublet[0].rentFixedDate.year" -> Seq("2011")
    )

    lazy val page4WeeklySubletRentData = Map(
      "propertyIsSublet" -> Seq("true"),
      "sublet[0].tenantFullName" -> Seq("Jake Smythe"),
      "sublet[0].tenantAddress.buildingNameNumber" -> Seq("Some Company"),
      "sublet[0].tenantAddress.street1" -> Seq("Some Road"),
      "sublet[0].tenantAddress.street2" -> Seq(""),
      "sublet[0].tenantAddress.postcode" -> Seq("AA11 1AA"),
      "sublet[0].subletType" -> Seq("part"),
      "sublet[0].subletPropertyPartDescription" -> Seq("basement"),
      "sublet[0].subletPropertyReasonDescription" -> Seq("commercial"),
      "sublet[0].annualRent" -> Seq("480"),
      "sublet[0].rentFixedDate.month" -> Seq("2"),
      "sublet[0].rentFixedDate.year" -> Seq("2011")
    )

    lazy val page4TenantsPropertyAddressData = Map(
      "propertyIsSublet" -> Seq("true"),
      "sublet[0].tenantFullName" -> Seq("Jake Smythe"),
      "sublet[0].tenantAddress.buildingNameNumber" ->  Seq("1"),
      "sublet[0].tenantAddress.street1" -> Seq("The Street"),
      "sublet[0].tenantAddress.street2"-> Seq("worthing"),
      "sublet[0].tenantAddress.postcode" -> Seq("AA11 1AA"),
      "sublet[0].subletType" -> Seq("part"),
      "sublet[0].subletPropertyPartDescription" -> Seq("basement"),
      "sublet[0].subletPropertyReasonDescription" -> Seq("commercial"),
      "sublet[0].annualRent" -> Seq("480"),
      "sublet[0].rentFixedDate.month" -> Seq("2"),
      "sublet[0].rentFixedDate.year" -> Seq("2011")
    )


    lazy val page5FormData = Map(
      "landlordFullName" -> Seq("Graham Goose"),
      "landlordAddress.buildingNameNumber" -> Seq("Some Company"),
      "landlordAddress.street1" -> Seq("Some Road"),
      "landlordAddress.street2" -> Seq(""),
      "landlordAddress.postcode" -> Seq("AA11 1AA"),
      "overseas" -> Seq("false"),
      "landlordConnectType" -> Seq("other"),
      "landlordConnectText" -> Seq("magic")
    )

    lazy val page6FormData = Map(
      "leaseAgreementType" -> Seq("leaseTenancy"),
      "writtenAgreement.leaseAgreementHasBreakClause" -> Seq("true"),
      "writtenAgreement.breakClauseDetails" -> Seq("adjf asdklfj a;sdljfa dsflk"),
      "writtenAgreement.agreementIsStepped" -> Seq("true"),
      "writtenAgreement.steppedDetails[0].stepTo.day" -> Seq("9"),
      "writtenAgreement.steppedDetails[0].stepTo.month" -> Seq("12"),
      "writtenAgreement.steppedDetails[0].stepTo.year" -> Seq("2011"),
      "writtenAgreement.steppedDetails[0].stepFrom.day" -> Seq("8"),
      "writtenAgreement.steppedDetails[0].stepFrom.month" -> Seq("11"),
      "writtenAgreement.steppedDetails[0].stepFrom.year" -> Seq("2010"),
      "writtenAgreement.steppedDetails[0].amount" -> Seq("500"),
      "writtenAgreement.startDate.month" -> Seq("3"),
      "writtenAgreement.startDate.year" -> Seq("2011"),
      "writtenAgreement.rentOpenEnded" -> Seq("false"),
      "writtenAgreement.leaseLength.years" -> Seq("10"),
      "writtenAgreement.leaseLength.months" -> Seq("2")
    )

    lazy val page6VerbalData = Map(
      "leaseAgreementType" -> Seq("verbal"),
      "verbalAgreement.startDate.month" -> Seq("5"),
      "verbalAgreement.startDate.year" -> Seq("2011"),
      "verbalAgreement.rentOpenEnded" -> Seq("false"),
      "verbalAgreement.leaseLength.years" -> Seq("10"),
      "verbalAgreement.leaseLength.months" -> Seq("2")
    )

    lazy val page7FormData = Map(
      "leaseContainsRentReviews" -> Seq("true"),
      "rentReviewDetails.reviewIntervalType" -> Seq("every3Years"),
      "rentReviewDetails.reviewIntervalTypeSpecify.years" -> Seq(""),
      "rentReviewDetails.reviewIntervalTypeSpecify.months" -> Seq(""),
      "rentReviewDetails.lastReviewDate.month" -> Seq("4"),
      "rentReviewDetails.lastReviewDate.year" -> Seq("2013"),
      "rentReviewDetails.canRentReduced" -> Seq("true"),
      "rentReviewDetails.rentResultOfRentReview" -> Seq("true"),
      "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.month" -> Seq("7"),
      "rentReviewDetails.rentReviewResultsDetails.whenWasRentReview.year" -> Seq("2012"),
      "rentReviewDetails.rentReviewResultsDetails.rentAgreedBetween" -> Seq("false"),
"rentReviewDetails.rentReviewResultsDetails.rentFixedBy" -> Seq("independent")
    )

    lazy val page8FormData = Map(
	"wasRentFixedBetween" -> Seq("true"),
	 "rentSetByType" -> Seq("renewedLease"))

    lazy val page9FormData = Map(
      "totalRent.rentLengthType" -> Seq("monthly"),
      "totalRent.annualRentExcludingVat" -> Seq("15588"),
      "rent-paid" -> Seq(""),
      "rentBecomePayable.day" -> Seq("1"),
      "rentBecomePayable.month" -> Seq("11"),
      "rentBecomePayable.year" -> Seq("2013"),
      "rentActuallyAgreed.day" -> Seq("1"),
      "rentActuallyAgreed.month" -> Seq("11"),
      "rentActuallyAgreed.year" -> Seq("2013"),
      "negotiatingNewRent" -> Seq("true"),
      "rentBasedOn" -> Seq("other"),
      "rentBasedOnDetails" -> Seq("here are some details about what the rent is based on")
    )

    lazy val page9WeeklyRentData = Map(
      "totalRent.rentLengthType" -> Seq("weekly"),
      "totalRent.annualRentExcludingVat" -> Seq("100"),
      "rent-paid" -> Seq(""),
      "rentBecomePayable.day" -> Seq("1"),
      "rentBecomePayable.month" -> Seq("11"),
      "rentBecomePayable.year" -> Seq("2013"),
      "rentActuallyAgreed.day" -> Seq("1"),
      "rentActuallyAgreed.month" -> Seq("11"),
      "rentActuallyAgreed.year" -> Seq("2013"),
      "negotiatingNewRent" -> Seq("true"),
      "rentBasedOn" -> Seq("other"),
      "rentBasedOnDetails" -> Seq("here are some details about what the rent is based on")
    )

    lazy val page9MonthlyRentData = Map(
      "totalRent.rentLengthType" -> Seq("monthly"),
      "totalRent.annualRentExcludingVat" -> Seq("5000"),
      "rent-paid" -> Seq(""),
      "rentBecomePayable.day" -> Seq("1"),
      "rentBecomePayable.month" -> Seq("11"),
      "rentBecomePayable.year" -> Seq("2013"),
      "rentActuallyAgreed.day" -> Seq("1"),
      "rentActuallyAgreed.month" -> Seq("11"),
      "rentActuallyAgreed.year" -> Seq("2013"),
      "negotiatingNewRent" -> Seq("true"),
      "rentBasedOn" -> Seq("other"),
      "rentBasedOnDetails" -> Seq("here are some details about what the rent is based on")
    )

    lazy val page9QuarterlyRentData = Map(
      "totalRent.rentLengthType" -> Seq("quarterly"),
      "totalRent.annualRentExcludingVat" -> Seq("250"),
      "rent-paid" -> Seq(""),
      "rentBecomePayable.day" -> Seq("1"),
      "rentBecomePayable.month" -> Seq("11"),
      "rentBecomePayable.year" -> Seq("2013"),
      "rentActuallyAgreed.day" -> Seq("1"),
      "rentActuallyAgreed.month" -> Seq("11"),
      "rentActuallyAgreed.year" -> Seq("2013"),
      "negotiatingNewRent" -> Seq("true"),
      "rentBasedOn" -> Seq("other"),
      "rentBasedOnDetails" -> Seq("here are some details about what the rent is based on")
    )

    lazy val page10FormData = Map(
      "parking.annualSeparateParkingDate.month" -> Seq("6"),
      "parking.annualSeparateParkingDate.year" -> Seq("2012"),
      "parking.rentIncludeParkingDetails.openSpaces" -> Seq("2"),
      "parking.rentIncludeParking" -> Seq("true"),
      "parking.rentSeparateParkingDetails.garages" -> Seq("9"),
      "parking.annualSeparateParking" -> Seq("599.84"),
      "parking.rentSeparateParking" -> Seq("true"),
      "landOnly" -> Seq("true"),
      "shellUnit" -> Seq("true"),
      "parking.rentIncludeParkingDetails" -> Seq("2"),
      "rentDetails" -> Seq("RENT DETAILS"),
      "annualSeparateParking" -> Seq("599.84"),
      "partRent" -> Seq("true"),
      "livingAccommodation" -> Seq("true"),
      "partRentDetails" -> Seq("PART RENT DETAILS"),
      "otherProperty" -> Seq("true")
    )

    lazy val page11FormData = Map(
      "rentFreePeriod" -> Seq("true"),
      "rentFreePeriodDetails.rentFreePeriodLength" -> Seq("36"),
      "rentFreePeriodDetails.rentFreePeriodDetails" -> Seq("REnt free period alsjfd lasdjf lasjdf la;sdjf lasdjf lasjd flasd jflsa df"),
      "payCapitalSum" -> Seq("true"),
      "capitalPaidDetails.capitalSum" -> Seq("4000"),
      "capitalPaidDetails.paymentDate.month" -> Seq("4"),
      "capitalPaidDetails.paymentDate.year" -> Seq("2010"),
      "receiveCapitalSum" -> Seq("true"),
      "capitalReceivedDetails.capitalSum" -> Seq("200"),
      "capitalReceivedDetails.paymentDate.month" -> Seq("3"),
      "capitalReceivedDetails.paymentDate.year" -> Seq("2010")
    )

    lazy val page12FormData = Map(
      "responsibleOutsideRepairs" -> Seq("both"),
      "responsibleInsideRepairs" -> Seq("both"),
      "responsibleBuildingInsurance" -> Seq("both"),
      "ndrCharges" -> Seq("false"),
      "waterCharges" -> Seq("false"),
      "includedServices" -> Seq("true"),
      "includedServicesDetails[0].chargeDescription" -> Seq("lkjfsdlfj"),
      "includedServicesDetails[0].chargeCost" -> Seq("78")
    )

    lazy val page12NdrAndWaterChargesData = Map(
      "responsibleOutsideRepairs" -> Seq("both"),
      "responsibleInsideRepairs" -> Seq("both"),
      "responsibleBuildingInsurance" -> Seq("both"),
      "ndrCharges" -> Seq("true"),
      "ndrDetails" -> Seq("41.23"),
      "waterCharges" -> Seq("true"),
      "waterChargesCost" -> Seq("456.76"),
      "includedServices" -> Seq("true"),
      "includedServicesDetails[0].chargeDescription" -> Seq("lkjfsdlfj"),
      "includedServicesDetails[0].chargeCost" -> Seq("78")
    )

    lazy val page13FormData = Map(
      "propertyAlterations" -> Seq("false"),
      "propertyAlterationsDetails[0].description" -> Seq("sdfasadsf"),
      "propertyAlterationsDetails[0].cost" -> Seq(""),
      "propertyAlterationsDetails[0].date.month" -> Seq(""),
      "propertyAlterationsDetails[0].date.year" -> Seq("")
    )

    lazy val page14FormData = Map(
      "anyOtherFactors" -> Seq("false"),
      "anyOtherFactorsDetails" -> Seq("")
    )

    val propertyAddress = Option.empty[Address]
    val alternatePropertyAddress = Some(tenantsPropertyAddress)
    val customerDetails = CustomerDetails("fn", UserTypeOccupier, ContactTypeEmail, ContactDetails(None, Some("abc@mailinator.com"), Some("abc@mailinator.com")))
    val theProperty = TheProperty("Stuff", OccupierTypeIndividuals, Some("Mike Ington"), Some(RoughDate(None, Some(7), 2013)), false, Some(true), None)
    val sublet = Sublet(true, List(SubletData("Jake Smythe", Address("Some Company", Some("Some Road"), None, "AA11 1AA"), SubletPart, Option("basement"), "commercial", Some(200), RoughDate(None, Some(2), 2011))))
    val landlord = Landlord(Some("Graham Goose"), Some(Address("Some Company", Some("Some Road"), None, "AA11 1AA")), LandlordConnectionTypeOther, Some("magic"))
    val leaseOrAgreement = LeaseOrAgreement(
      LeaseAgreementTypesLeaseTenancy, Some(true), Some("adjf asdklfj a;sdljfa dsflk"), Some(true), List(SteppedDetails(new LocalDate(2010, 11, 8), new LocalDate(2011, 12, 9),500)), Some(RoughDate(None, Some(3), 2011)), Some(false), Some(MonthsYearDuration(2, 10))
    )
    val rentReviews = RentReviews(
      true, Some(RentReviewDetails(Some(MonthsYearDuration(0, 3)), Some(RoughDate(None, Some(4), 2013)), true, true,
        Some(RentReviewResultDetails(RoughDate(None, Some(7), 2012), false, Some(RentFixedTypeIndependent))))))
    val rentAgreement = RentAgreement(true, None, RentSetByTypeRenewedLease)
    val rent = Rent(Some(15588), new LocalDate(2013, 11, 1), new LocalDate(2013, 11, 1), true, RentBaseTypeOther, Some("here are some details about what the rent is based on"))
    val whatRentIncludes = WhatRentIncludes(true, true, true, true, true, Some("RENT DETAILS"), Parking(true, Some(ParkingDetails(2, 0, 0)), true, Some(ParkingDetails(0, 0, 9)), Some(599.84), Some(RoughDate(None, Some(6), 2012))))
    val incentivesAndPayments = IncentivesAndPayments(
      true, Some(FreePeriodDetails(36, "REnt free period alsjfd lasdjf lasjdf la;sdjf lasdjf lasjd flasd jflsa df")), true,
      Some(CapitalDetails(4000, RoughDate(None, Some(4), 2010))), true, Some(CapitalDetails(200, RoughDate(None, Some(3), 2010))))
    val responsibilities = Responsibilities(
      ResponsibleBoth, ResponsibleBoth, ResponsibleBoth, false, false, true,
      List(ChargeDetails("lkjfsdlfj", 78)))
    val propertyAlterations = PropertyAlterations(false, List(), None)
    val otherFactors = OtherFactors(false, None)
    val verbalLease = LeaseOrAgreement(
      LeaseAgreementTypesVerbal, None, None, None, List.empty, Some(RoughDate(None, Some(5), 2011)), Some(false), Some(MonthsYearDuration(2, 10))
    )
    val defaultAddress = Address("45", Some("Default Street"), Some("Goring-by-sea"), "AA11 1AA")
    val docsAndSubmissions = Table(
      ("doc", "submission"),
      (doc1, submission1),
      (doc2, submission2)
    )

    def assertEqual(x: Submission, y: Submission) {
      assert(x.propertyAddress === y.propertyAddress)
      assert(x.customerDetails === y.customerDetails)
      assert(x.theProperty === y.theProperty)
      assert(x.sublet === y.sublet)
      assert(x.landlord === y.landlord)
      assert(x.lease === y.lease)
      assert(x.rentReviews === y.rentReviews)
      assert(x.rentAgreement === y.rentAgreement)
      assert(x.rent === y.rent)
      assert(x.rentIncludes === y.rentIncludes)
      assert(x.incentives === y.incentives)
      assert(x.responsibilities === y.responsibilities)
      assert(x.alterations === y.alterations)
      assert(x.otherFactors === y.otherFactors)
      assert(x === y)
    }
  }

}
