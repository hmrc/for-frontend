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

package models.journeys

import models.*
import models.journeys.Journey.*
import models.journeys.Paths.*
import models.pages.*
import models.serviceContracts.submissions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import utils.SummaryBuilder as summaryBuilder

import java.time.LocalDate

class PathingLogicSpec extends AnyFlatSpec with should.Matchers:

  val pageZeroData: AddressConnectionType = AddressConnectionType.yes

  val pageOneData: Option[Address] = None

  val pageTwoData: CustomerDetails = CustomerDetails("name", UserType.owner, ContactDetails("01234567890", "abc@mailinator.com"))

  val propertyOwned: PageThree = PageThree(
    propertyType = "property type",
    occupierType = OccupierType.company,
    occupierCompanyName = Some("Some Company"),
    occupierCompanyContact = Some("Some Company Contact"),
    firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
    None,
    propertyOwnedByYou = true,
    propertyRentedByYou = None,
    noRentDetails = None
  )

  val propertyRented: PageThree = PageThree(
    propertyType = "property type",
    occupierType = OccupierType.company,
    occupierCompanyName = Some("Some Company"),
    occupierCompanyContact = Some("Some Company Contact"),
    firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
    None,
    propertyOwnedByYou = false,
    propertyRentedByYou = Some(true),
    noRentDetails = None
  )

  val propertyNotOwnedOrRented: PageThree = PageThree(
    propertyType = "property type",
    occupierType = OccupierType.company,
    occupierCompanyName = Some("Some Company"),
    occupierCompanyContact = Some("Some Company Contact"),
    firstOccupationDate = Some(RoughDate(Some(28), Some(2), 2015)),
    None,
    propertyOwnedByYou = false,
    propertyRentedByYou = Some(false),
    noRentDetails = None
  )

  val propertyNotSublet: PageFour = PageFour(
    false,
    List.empty
  )

  val propertyIsSublet: PageFour = PageFour(
    true,
    List(SubletDetails(
      "Something",
      Address("Street address", None, Some("City"), "Postcode"),
      SubletType.part,
      Option("Description"),
      "Reason",
      BigDecimal(1.33),
      RoughDate(None, Some(12), 1980)
    ))
  )

  val pageFiveData: PageFive = PageFive("name", Some(Address("line1", None, Some("city"), "postcode")), LandlordConnectionType.noConnected, None)

  val pageSixData: PageSix =
    PageSix(LeaseAgreementType.leaseTenancy, Some(WrittenAgreement(RoughDate(None, None, 1), false, None, false, None, false, Nil)), VerbalAgreement())

  val pageSixNoVerbal: PageSix =
    PageSix(LeaseAgreementType.leaseTenancy, Some(WrittenAgreement(RoughDate(None, None, 1), false, None, false, None, false, Nil)), VerbalAgreement())

  val leaseAgreementTenancy: PageSix =
    PageSix(LeaseAgreementType.leaseTenancy, Some(WrittenAgreement(RoughDate(None, None, 1), false, None, false, None, false, Nil)), VerbalAgreement())
  val pageSixVerbal: PageSix         = PageSix(LeaseAgreementType.verbal, None, VerbalAgreement(Some(RoughDate(None, None, 1)), Some(false)))
  val pageSevenData: PageSeven       = PageSeven(false, None)
  val pageEightData: RentAgreement   = RentAgreement(true, None, RentSetByType.newLease)
  val hasNoRentReviews: PageSeven    = PageSeven(false, None)
  val hasRentReviews: PageSeven      = PageSeven(true, None)

  val pageNineData: PageNine = PageNine(
    AnnualRent(8.99),
    rentBecomePayable = LocalDate.of(2010, 2, 27),
    rentActuallyAgreed = LocalDate.of(2005, 4, 2),
    negotiatingNewRent = true,
    rentBasis = RentBaseType.openMarket,
    None
  )

  "isShortPath" should "return true when the property is owned but not sublet" in {
    val sub = summaryBuilder(page3 = Some(propertyOwned), page4 = Some(propertyNotSublet))

    isShortPath(sub) should be(true)
  }

  it should "return true when the property is neither owned, rented, or sublet" in {
    val sub = summaryBuilder(page3 = Some(propertyNotOwnedOrRented), page4 = Some(propertyNotSublet))

    isShortPath(sub) should be(true)
  }

  it should "return false when the property not owned but is rented" in {
    val sub = summaryBuilder(page3 = Some(propertyRented), page4 = Some(propertyNotSublet))

    isShortPath(sub) should be(false)
  }

  it should "return false when the property is sublet" in {
    val sub = summaryBuilder(page4 = Some(propertyIsSublet))

    isShortPath(sub) should be(false)
  }

  "pageIsNotApplicable" should "return false for page 1" in {
    pageIsNotApplicable(1, summaryBuilder()) should be(false)
  }

  it should "return false for page 2" in {
    pageIsNotApplicable(2, summaryBuilder(page0 = Some(pageZeroData))) should be(false)
  }

  it should "return false for page 3" in {
    pageIsNotApplicable(3, summaryBuilder(page0 = Some(pageZeroData), page2 = Some(pageTwoData))) should be(false)
  }

  it should "return false for page 4" in {
    pageIsNotApplicable(4, summaryBuilder(page0 = Some(pageZeroData), page2 = Some(pageTwoData), page3 = Some(propertyNotOwnedOrRented))) should be(false)
  }

  it should "return false for page 5 when the property is sublet" in {
    val sub = summaryBuilder(page0 = Some(pageZeroData), page2 = Some(pageTwoData), page3 = Some(propertyRented), page4 = Some(propertyIsSublet))
    pageIsNotApplicable(5, sub) should be(false)
  }

  it should "return true for page 5 when property is owned and not sublet" in {
    val sub = summaryBuilder(page0 = Some(pageZeroData), page2 = Some(pageTwoData), page3 = Some(propertyOwned), page4 = Some(propertyNotSublet))
    pageIsNotApplicable(5, sub) should be(true)
  }

  it should "return false for page 7 when the lease agreement type is not verbal" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyRented),
      page4 = Some(propertyNotSublet),
      page5 = Some(pageFiveData),
      page6 = Some(leaseAgreementTenancy)
    )
    pageIsNotApplicable(7, sub) should be(false)
  }

  it should "return true for page 7 when the lease agreement type is verbal" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyRented),
      page4 = Some(propertyNotSublet),
      page5 = Some(pageFiveData),
      page6 = Some(pageSixVerbal)
    )
    pageIsNotApplicable(7, sub) should be(true)
  }

  it should "return true for page 7 when the property is owned and not sublet, even if the lease agreement type is not verbal" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyOwned),
      page4 = Some(propertyNotSublet),
      page5 = Some(pageFiveData),
      page6 = Some(leaseAgreementTenancy)
    )
    pageIsNotApplicable(7, sub) should be(true)
  }

  it should "return false for page 8 when there is no rent review" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyRented),
      page4 = Some(propertyNotSublet),
      page5 = Some(pageFiveData),
      page6 = Some(leaseAgreementTenancy),
      page7 = Some(hasNoRentReviews)
    )
    pageIsNotApplicable(8, sub) should be(false)
  }

  it should "return true for page 8 when there are rent reviews" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyOwned),
      page4 = Some(propertyNotSublet),
      page5 = Some(pageFiveData),
      page6 = Some(leaseAgreementTenancy),
      page7 = Some(hasRentReviews)
    )
    pageIsNotApplicable(8, sub) should be(true)
  }

  it should "return true for page 8 when the short path has been chosen" in {
    val sub = summaryBuilder(page0 = Some(pageZeroData), page2 = Some(pageTwoData), page3 = Some(propertyOwned), page4 = Some(propertyNotSublet))
    pageIsNotApplicable(8, sub) should be(true)
  }

  "lastPageForPath" should "return 15 for standard path" in {
    val sub = summaryBuilder()
    lastPageFor(sub) shouldBe 14
  }

  it should "return 4 for short path" in {
    val sub = summaryBuilder(page3 = Some(propertyOwned), page4 = Some(propertyNotSublet))
    lastPageFor(sub) shouldBe 4
  }

  it should "return 15 for rent reviews path" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyRented),
      page4 = Some(propertyIsSublet),
      page5 = Some(pageFiveData),
      page6 = Some(leaseAgreementTenancy),
      page7 = Some(hasRentReviews)
    )

    lastPageFor(sub) shouldBe 14
  }

  it should "return 15 for verbal agreement path" in {
    val sub = summaryBuilder(
      page0 = Some(pageZeroData),
      page2 = Some(pageTwoData),
      page3 = Some(propertyRented),
      page4 = Some(propertyIsSublet),
      page5 = Some(pageFiveData),
      page6 = Some(pageSixVerbal)
    )

    lastPageFor(sub) shouldBe 14
  }
