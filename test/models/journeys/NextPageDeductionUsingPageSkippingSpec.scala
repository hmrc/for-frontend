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

package models.journeys

import models._
import models.journeys.Journey._
import models.pages._
import models.serviceContracts.submissions._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class NextPageDeductionUsingPageSkippingSpec extends FlatSpec with Matchers with OptionValues {

  "nextPageAllowable for page four" should "return summary when you say you own property and do not sublet" in {
    val pageFourData = PageFour(false, List.empty)
    val doc = summaryBuilder(propertyAddress = Some(pageOneData), customerDetails = Some(pageTwoData), theProperty = Some(propertyOwned), sublet = Some(pageFourData))
    nextPageAllowable(5, doc, Some(4)) shouldBe SummaryPage
  }

  it should "return summary when you say you do not own the property, but do not rent or sublet" in {
    val pageFourData = PageFour(false, List.empty)

    val pageThreeData = PageThree(
      propertyType = "property type",
      occupierType = OccupierTypeCompany,
      occupierCompanyName = Some("Some Company"),
      occupierCompanyContact = Some("Some Company Contact"),
      firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
      None,
      propertyOwnedByYou = false,
      propertyRentedByYou = Some(false))

    val doc = summaryBuilder(propertyAddress = Some(pageOneData),customerDetails= Some(pageTwoData),theProperty = Some(pageThreeData), sublet = Some(pageFourData))

    nextPageAllowable(5, doc, Some(4)) shouldBe SummaryPage
  }

  it should "return page five when you say you do not own the property, but rent without subletting" in {
    val pageFourData = PageFour(false, List.empty)

    val pageThreeData = PageThree(
      propertyType = "property type",
      occupierType = OccupierTypeCompany,
      occupierCompanyName = Some("Some Company"),
      occupierCompanyContact = Some("Some Company Contact"),
      firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
      None,
      propertyOwnedByYou = false,
      propertyRentedByYou = Some(true))

    val doc = summaryBuilder(propertyAddress = Some(pageOneData),customerDetails= Some(pageTwoData),theProperty = Some(pageThreeData), sublet = Some(pageFourData))

    nextPageAllowable(5, doc, Some(4)) shouldBe PageToGoTo(5)
  }

  it should "return summary when you sublet while being the owner" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),customerDetails= Some(pageTwoData),theProperty = Some(propertyOwned), sublet = Some(pageFourData))

    nextPageAllowable(5, doc, Some(4)) shouldBe SummaryPage
  }

  "nextPageAllowable for page six" should "return 8 when the lease agreement is verbal" in {
    val p6 = PageSix(LeaseAgreementTypesVerbal, None, VerbalAgreement())
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(p6))

    nextPageAllowable(7, doc, Some(6)) shouldBe PageToGoTo(8)
  }

  it should "return 7 when the lease agreement is not verbal" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData),lease = Some(pageSixData))

    nextPageAllowable(7, doc, Some(6)) shouldBe PageToGoTo(7)
  }

  it should "display page eight when the lease agreement is verbal" in {
    val p6 = PageSix(LeaseAgreementTypesVerbal, None, VerbalAgreement())
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(p6))

    nextPageAllowable(8, doc) shouldBe PageToGoTo(8)
  }

  "nextPageAllowable for page seven" should "return eight when there is no rent reviews" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData),lease = Some(pageSixNoVerbal), rentReviews = Some(pageSevenData))
    nextPageAllowable(8, doc, Some(7)) shouldBe PageToGoTo(8)

  }
  it should "return nine when there are rent reviews" in {
     val p7 = PageSeven(true, None)
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(pageSixNoVerbal),rentReviews = Some(p7))

    nextPageAllowable(8, doc, Some(7)) shouldBe PageToGoTo(9)
  }

  it should "return page nine when pages nine and ten are already completed and there is a rent review" in {
    val p7 = PageSeven(true, None)
    val pageTenData = WhatRentIncludes(false, false, false, false, false, None, Parking(false, None, false, None, None, None))
    val doc = summaryBuilder(
      propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData),
      theProperty = Some(pageThreeData),
      sublet = Some(pageFourData),
      landlord = Some(pageFiveData),
      lease = Some(pageSixNoVerbal),
      rentReviews = Some(p7),
      rent = Some(pageNineData),
      includes = Some(pageTenData)
    )

    nextPageAllowable(8, doc, Some(7)) should be (PageToGoTo(9))
  }

  it should "not permit skipping ahead to page 13 when pages 10 through 12 are not completed" in {
    val doc = summaryBuilder(
      propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData),
      theProperty = Some(pageThreeData),
      sublet = Some(pageFourData),
      landlord = Some(pageFiveData),
      lease = Some(pageSixNoVerbal),
      rentReviews = Some(PageSeven(true, None)),
      rent = Some(pageNineData)
    )

    nextPageAllowable(13, doc, Some(9)) should be (PageToGoTo(10))
  }

  "nextPageAllowable for page eight" should "return six when page seven is requested when there is a verbal agreement" in {
    val p6 = PageSix(LeaseAgreementTypesVerbal, None, VerbalAgreement())
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(p6))

    nextPageAllowable(7, doc, Some(8)) shouldBe PageToGoTo(6)
  }

  it should "return four when eight is not an applicable page due to short path being chosen" in {
    val pageFourData = PageFour(false, List.empty)

    val doc = summaryBuilder(propertyAddress = Some(pageOneData), customerDetails = Some(pageTwoData), theProperty = Some(propertyOwned), sublet = Some(pageFourData))
    nextPageAllowable(8, doc) shouldBe SummaryPage
  }

  it should "return page seven when page seven is requested and there is not a verbal agreement" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(pageSixData))

    nextPageAllowable(7, doc, Some(8)) shouldBe PageToGoTo(7)
  }

  "nextPageAllowable for page nine" should "return seven when page eight is requested when there are rent reviews" in {
    val p7 = PageSeven(true, None)
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(pageSixNoVerbal),rentReviews = Some(p7))

    nextPageAllowable(8, doc, Some(9)) shouldBe PageToGoTo(7)
  }

  it should "return page eight when page eight is requested and there are no rent reviews" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(pageSixNoVerbal),rentReviews = Some(pageSevenData))

    nextPageAllowable(8, doc, Some(9)) shouldBe PageToGoTo(8)
  }

  it should "return page ten when a verbal agreement has been chosen on page six and fill in page nine" in {
    val doc = summaryBuilder(propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData), theProperty = Some(pageThreeData),
      sublet = Some(pageFourData), landlord = Some(pageFiveData), lease = Some(pageSixData), rentAgreement = Some(pageEightData), rent = Some(pageNineData))
    nextPageAllowable(10, doc, Some(9)) shouldBe PageToGoTo(10)
  }

  it should "return page ten when a verbal agreement has been chosen on page six and fill in page nine without current page" in {
    val doc = summaryBuilder(
      propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData),
      theProperty = Some(pageThreeData),
      sublet = Some(pageFourData),
      landlord = Some(pageFiveData),
      lease = Some(pageSixData),
      rentAgreement = Some(pageEightData),
      rent = Some(pageNineData))
    nextPageAllowable(10, doc) shouldBe PageToGoTo(10)
  }

  it should "return page ten when a non verbal agreement has been chosen on page six, on page seven you selected rent reviews and fill in page nine and press continue" in {
    val doc = summaryBuilder(
      propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData),
      theProperty = Some(pageThreeData),
      sublet = Some(pageFourData),
      landlord = Some(pageFiveData),
      lease = Some(pageSixData),
      rentReviews = Some(hasRentReviews),
      rent = Some(pageNineData))
    nextPageAllowable(10, doc) shouldBe PageToGoTo(10)
  }

  it should "return page ten when a non verbal agreement has been chosen on page six, on page seven you selected no rent reviews, fill in page eight and fill in page nine and press continue" in {
    val doc = summaryBuilder(
      propertyAddress = Some(pageOneData),
      customerDetails = Some(pageTwoData),
      theProperty = Some(pageThreeData),
      sublet = Some(pageFourData),
      landlord = Some(pageFiveData),
      lease = Some(pageSixData),
      rentReviews = Some(hasNoRentReviews),
      rentAgreement = Some(pageEightData),
      rent = Some(pageNineData))
    nextPageAllowable(10, doc) shouldBe PageToGoTo(10)
  }

  "nextPageAllowable for page one" should "return zero when trying to go back" in {
    val doc = summaryBuilder()
    nextPageAllowable(0, doc, Some(1)) shouldBe PageToGoTo(0)
  }

  lazy val pageOneData = PropertyAddress(true, None)

  lazy val pageTwoData = CustomerDetails("name", UserTypeOwner, ContactTypePhone, ContactDetails(None, None, None))

  lazy val pageThreeData = PageThree(
    propertyType = "property type",
    occupierType = OccupierTypeCompany,
    occupierCompanyName = Some("Some Company"),
    occupierCompanyContact = Some("Some Company Contact"),
    firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
    None,
    propertyOwnedByYou = false,
    propertyRentedByYou = Some(true)
  )

  lazy val propertyOwned = PageThree(
    propertyType = "property type",
    occupierType = OccupierTypeCompany,
    occupierCompanyName = Some("Some Company"),
    occupierCompanyContact = Some("Some Company Contact"),
    firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
    None,
    propertyOwnedByYou = true,
    propertyRentedByYou = None
  )

  lazy val pageFourData = PageFour(
    true, List(SubletDetails(
      "Something", Address("Street address", None, Some("City"), "Postcode"), "Description", "Reason", BigDecimal(1.0), RoughDate(None, Some(12), 1980))
    )
  )

  lazy val pageFiveData = PageFive(false, "name", Some(Address("line1", None, Some("city"), "postcode")), LandlordConnectionTypeNone, None)
  lazy val pageSixData = PageSix(LeaseAgreementTypesLeaseTenancy, Some(WrittenAgreement(RoughDate(None, None, 1), false, None, false, None, false, Nil)), VerbalAgreement())
  lazy val pageSixNoVerbal = PageSix(LeaseAgreementTypesLeaseTenancy, Some(WrittenAgreement(RoughDate(None, None, 1), false, None, false, None, false, Nil)), VerbalAgreement())
  lazy val pageSixVerbal = PageSix(LeaseAgreementTypesVerbal, None, VerbalAgreement(Some(RoughDate(None, None, 1)), Some(false)))
  lazy val pageSevenData = PageSeven(false, None)
  lazy val pageEightData = RentAgreement(true, None, RentSetByTypeNewLease)
  lazy val hasNoRentReviews = PageSeven(false, None)
  lazy val hasRentReviews = PageSeven(true, None)
  lazy val pageNineData = PageNine(AnnualRent(RentLengthTypeMonthly, 8.99), rentBecomePayable = new LocalDate(2010, 2, 27), rentActuallyAgreed = new LocalDate(2005, 4, 2), negotiatingNewRent = true, rentBasis = RentBaseTypeOpenMarket, None)


  private def summaryBuilder(propertyAddress: Option[PropertyAddress] = None,
                                customerDetails: Option[CustomerDetails] = None,
                                theProperty: Option[PageThree] = None,
                                sublet: Option[PageFour] = None,
                                landlord: Option[PageFive] = None,
                                lease: Option[PageSix] = None,
                                rentReviews: Option[PageSeven] = None,
                                rentAgreement: Option[RentAgreement] = None,
                                rent: Option[PageNine] = None,
                                includes: Option[WhatRentIncludes] = None,
                                incentives: Option[IncentivesAndPayments] = None,
                                responsibilities: Option[PageTwelve] = None,
                                alterations: Option[PropertyAlterations] = None,
                                otherFactors: Option[OtherFactors] = None) = {
    Summary("", DateTime.now, propertyAddress,
      customerDetails,
      theProperty,
      sublet,
      landlord,
      lease,
      rentReviews,
      rentAgreement,
      rent,
      includes,
      incentives,
      responsibilities,
      alterations,
      otherFactors)
  }
}
