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

import models.RoughDate
import models.serviceContracts.submissions.*

import java.time.LocalDate
import java.time.Month.{AUGUST, DECEMBER, JANUARY}

/**
  * @author Yuriy Tumakha
  */
class FormSubmissionFailedSpec extends AuditEventTest:

  "FormSubmissionFailed" should {
    "serialize to expected JSON" in {
      val event = FormSubmissionFailed(
        error = "Error message",
        language = "en",
        submission = Some(Submission(
          Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA")),
          Some(CustomerDetails("Full name", UserType.owner, ContactDetails("1234567", "user@email.com"))),
          Some(TheProperty(
            "Property type",
            OccupierType.company,
            Some("Company name"),
            Some(RoughDate(Some(28), Some(12), 2026)),
            propertyOwnedByYou = true,
            propertyRentedByYou = Some(false),
            Some("No rent details")
          )),
          Some(Sublet(
            propertyIsSublet = true,
            List(
              SubletData(
                "Tenant full name",
                Address("99B", Some("Tenant address line 1"), Some("City"), "ZZ1 1ZZ"),
                SubletType.part,
                Some("Sublet part description"),
                "Sublet reason",
                Some(BigDecimal(999.99)),
                RoughDate(Some(29), Some(12), 2026)
              )
            )
          )),
          Some(Landlord(
            "Landlord full name",
            Some(Address("33", Some("Landlord address line 1"), Some("City"), "NN1 9AB")),
            LandlordConnectionType.other,
            Some("Other connection")
          )),
          Some(LeaseOrAgreement(
            LeaseAgreementType.leaseTenancy,
            leaseAgreementHasBreakClause = Some(true),
            Some("Break clause details"),
            agreementIsStepped = Some(true),
            List(
              SteppedDetails(LocalDate.of(2025, JANUARY, 1), LocalDate.of(2025, DECEMBER, 31), BigDecimal(3000.01)),
              SteppedDetails(LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31), BigDecimal(3500.99))
            ),
            startDate = Some(RoughDate(Some(25), Some(12), 2026)),
            rentOpenEnded = Some(true),
            leaseLength = Some(MonthsYearDuration(6, 0))
          )),
          Some(RentReviews(
            leaseContainsRentReviews = true,
            Some(RentReviewDetails(
              reviewIntervalTypeSpecify = Some(MonthsYearDuration(0, 3)),
              lastReviewDate = Some(RoughDate(Some(25), Some(12), 2026)),
              canRentReduced = true,
              rentResultOfRentReview = false,
              Some(RentReviewResultDetails(RoughDate(Some(25), Some(12), 2026), true, Some(RentFixedType.independent)))
            ))
          )),
          Some(RentAgreement(
            wasRentFixedBetween = true,
            Some(NotReviewRentFixedType.finalJudge),
            RentSetByType.newLease
          )),
          Some(Rent(
            annualRentExcludingVat = Some(BigDecimal("2500.50")),
            rentBecomePayable = LocalDate.of(2025, AUGUST, 10),
            rentActuallyAgreed = LocalDate.of(2025, AUGUST, 20),
            negotiatingNewRent = true,
            RentBaseType.openMarket,
            Some("RentBasis other details")
          )),
          Some(WhatRentIncludes(
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
          Some(IncentivesAndPayments(
            rentFreePeriod = true,
            Some(FreePeriodDetails(lengthInMonths = 2, description = "Free 2 months period")),
            payCapitalSum = true,
            Some(CapitalDetails(capitalSum = BigDecimal(1500.22), paymentDate = RoughDate(Some(31), Some(1), 2026))),
            receiveCapitalSum = true,
            Some(CapitalDetails(capitalSum = BigDecimal(2500.33), paymentDate = RoughDate(Some(22), Some(2), 2026)))
          )),
          Some(Responsibilities(
            responsibleOutsideRepairs = ResponsibleType.landlord,
            responsibleInsideRepairs = ResponsibleType.tenant,
            responsibleBuildingInsurance = ResponsibleType.both,
            ndrCharges = true,
            waterCharges = true,
            includedServices = true,
            includedServicesDetails = List(ChargeDetails("Extra charge description", chargeCost = BigDecimal(100.33)))
          )),
          Some(PropertyAlterations(
            propertyAlterations = true,
            List(PropertyAlterationsDetails(RoughDate(Some(9), Some(1), 2026), AlterationSetByType.removedWall, BigDecimal(999.44))),
            alterationsRequired = Some(true)
          )),
          Some(OtherFactors(
            anyOtherFactors = true,
            Some("Other factors details")
          )),
          referenceNumber = Some("9999000123")
        )),
        address = Some(Address("13", Some("Street"), Some("City"), "AA11 1AA")),
        updatedAddress = Some(Address("13A", Some("New street"), Some("City"), "AA11 1AA"))
      )

      verifyEventJson(event, "FormSubmissionFailed", "FormSubmissionFailed.json")
    }
  }

  "FormSubmissionFailed minimal" should {
    "serialize to expected JSON" in {
      val event = FormSubmissionFailed("Error message", "cy")

      verifyEventJson(event, "FormSubmissionFailed", "FormSubmissionFailed_minimal.json")
    }
  }
