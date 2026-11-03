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

package models.event

import models.{AnnualRent, RoughDate}
import models.pages.{PageFive, PageFour, PageNine, PageSeven, PageSevenDetails, PageSix, PageThree, PageTwelve, SubletDetails, Summary, VerbalAgreement, WrittenAgreement}
import models.serviceContracts.submissions.{Address, AddressConnectionType, AlterationSetByType, CapitalDetails, ChargeDetails, ContactDetails, CustomerDetails, FreePeriodDetails, IncentivesAndPayments, LandlordConnectionType, LeaseAgreementType, MonthsYearDuration, NotReviewRentFixedType, OccupierType, OtherFactors, Parking, ParkingDetails, PropertyAlterations, PropertyAlterationsDetails, RentAgreement, RentBaseType, RentFixedType, RentReviewResultDetails, RentSetByType, ResponsibleType, ReviewIntervalType, SteppedDetails, SubletType, UserType, WhatRentIncludes}

import java.time.Month.{AUGUST, DECEMBER, JANUARY}
import java.time.{Instant, LocalDate, ZoneOffset, ZonedDateTime}

/**
  * @author Yuriy Tumakha
  */
class SavedForLaterSpec extends AuditEventTest:

  private val journeyStartedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1798722525000L), ZoneOffset.UTC)

  "SavedForLater" should {
    "serialize to expected JSON" in {
      val event = SavedForLater(
        exitPath = "/sending-rental-information/page/6",
        summary = Summary(
          referenceNumber = "9999000777",
          journeyStarted = journeyStartedTime,
          addressConnection = Some(AddressConnectionType.yes),
          propertyAddress = Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA")),
          customerDetails = Some(CustomerDetails("Full name", UserType.owner, ContactDetails("1234567", "user@email.com"))),
          theProperty = Some(PageThree(
            "Property type",
            OccupierType.company,
            Some("Company name"),
            Some("Occupier company contact"),
            Some(RoughDate(Some(28), Some(12), 2026)),
            Some("Main occupier name"),
            true,
            Some(false),
            Some("no")
          )),
          sublet = Some(PageFour(
            propertyIsSublet = true,
            List(
              SubletDetails(
                "Tenant full name",
                Address("99B", Some("Tenant address line 1"), Some("City"), "ZZ1 1ZZ"),
                SubletType.part,
                Some("Sublet part description"),
                "Sublet reason",
                BigDecimal("1000.00"),
                RoughDate(Some(29), Some(12), 2026)
              )
            )
          )),
          landlord = Some(PageFive(
            "Landlord full name",
            Some(Address("33", Some("Landlord address line 1"), Some("City"), "NN1 9AB")),
            LandlordConnectionType.other,
            Some("Other connection")
          )),
          lease = Some(PageSix(
            LeaseAgreementType.leaseTenancy,
            Some(WrittenAgreement(
              RoughDate(Some(25), Some(12), 2026),
              true,
              Some(MonthsYearDuration(6, 0)),
              true,
              Some("Break clause details"),
              true,
              List(
                SteppedDetails(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2025, DECEMBER, 31), BigDecimal(3000.01)),
                SteppedDetails(LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31), BigDecimal(3500.99))
              )
            )),
            VerbalAgreement(
              Some(RoughDate(Some(25), Some(12), 2026)),
              Some(true),
              Some(MonthsYearDuration(6, 0))
            ),
            lastReviewDate = Some(LocalDate.of(2025, AUGUST, 25)),
            rentReviewDate = Some(LocalDate.of(2026, AUGUST, 26))
          )),
          rentReviews = Some(PageSeven(
            leaseContainsRentReviews = true,
            Some(PageSevenDetails(
              ReviewIntervalType.every3Years,
              Some(MonthsYearDuration(0, 3)),
              Some(RoughDate(Some(25), Some(12), 2026)),
              canRentReduced = true,
              rentResultOfRentReview = false,
              Some(RentReviewResultDetails(RoughDate(Some(25), Some(12), 2026), true, Some(RentFixedType.independent)))
            )),
            agreementStartDate = Some(LocalDate.of(2025, AUGUST, 11))
          )),
          rentAgreement = Some(RentAgreement(
            wasRentFixedBetween = true,
            Some(NotReviewRentFixedType.interim),
            RentSetByType.renewedLease
          )),
          rent = Some(PageNine(
            totalRent = AnnualRent(BigDecimal("2500.50")),
            rentBecomePayable = LocalDate.of(2025, AUGUST, 10),
            rentActuallyAgreed = LocalDate.of(2025, AUGUST, 20),
            negotiatingNewRent = true,
            RentBaseType.percentageOpenMarket,
            Some("RentBasis other details")
          )),
          rentIncludes = Some(WhatRentIncludes(
            partRent = true,
            otherProperty = true,
            livingAccommodation = true,
            landOnly = false,
            shellUnit = false,
            Some("Rent details"),
            Parking(
              rentIncludesParking = true,
              Some(ParkingDetails(openSpaces = 100, coveredSpaces = 200, garages = 12)),
              rentHasSeparateParking = true,
              Some(ParkingDetails(openSpaces = 300, coveredSpaces = 20, garages = 100)),
              annualSeparateParkingAmount = Some(BigDecimal(999.99)),
              annualSeparateParkingFixedDate = Some(RoughDate(Some(26), Some(12), 2026))
            )
          )),
          incentives = Some(IncentivesAndPayments(
            rentFreePeriod = true,
            Some(FreePeriodDetails(lengthInMonths = 2, description = "Free 2 months period")),
            payCapitalSum = true,
            Some(CapitalDetails(capitalSum = BigDecimal(1500.22), paymentDate = RoughDate(Some(31), Some(1), 2026))),
            receiveCapitalSum = true,
            Some(CapitalDetails(capitalSum = BigDecimal(2500.33), paymentDate = RoughDate(Some(22), Some(2), 2026)))
          )),
          responsibilities = Some(PageTwelve(
            responsibleOutsideRepairs = ResponsibleType.landlord,
            responsibleInsideRepairs = ResponsibleType.tenant,
            responsibleBuildingInsurance = ResponsibleType.both,
            ndrCharges = true,
            ndrDetails = Some(BigDecimal(100.11)),
            waterCharges = true,
            waterChargesCost = Some(BigDecimal(200.22)),
            includedServices = true,
            includedServicesDetails = List(ChargeDetails("Extra charge description", chargeCost = BigDecimal(100.33)))
          )),
          alterations = Some(PropertyAlterations(
            propertyAlterations = true,
            List(PropertyAlterationsDetails(RoughDate(Some(9), Some(1), 2026), AlterationSetByType.addLift, BigDecimal(999.44))),
            alterationsRequired = Some(true)
          )),
          otherFactors = Some(OtherFactors(
            anyOtherFactors = true,
            Some("Other factors details")
          )),
          address = Some(Address("13", Some("Street"), Some("City"), "AA11 1AA")),
          journeyResumptions = Seq(journeyStartedTime.minusDays(1), journeyStartedTime)
        ),
        address = Address("13", Some("Street"), Some("City"), "AA11 1AA"),
        updatedAddress = Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA"))
      )

      verifyEventJson(event, "SavedForLater", "SavedForLater.json")
    }
  }

  "SavedForLater minimal" should {
    "serialize to expected JSON" in {
      val event = SavedForLater(
        exitPath = "/sending-rental-information/page/6",
        summary = Summary("9999000777", journeyStartedTime),
        address = Address("13", None, None, "AA11 1AA"),
        updatedAddress = None
      )

      verifyEventJson(event, "SavedForLater", "SavedForLater_minimal.json")
    }
  }
