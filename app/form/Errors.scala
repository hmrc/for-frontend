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

object Errors {

  //address
  val addressRequired  = "address.required"
  val addressBuildingNameNumberRequired = "error.address.buildingName.required"
  val addressTownCityRequired = "error.address.townCity.required"
  val addressPostcodeRequired = "error.address.postcode.required"

  //Login
  val invalidRefNum = "error.invalid_refnum"
  val maxCurrencyAmountExceeded = "error.maxCurrencyAmountExceeded"
  val toDateIsAfterFromDate = "error.writtenAgreement.steppedDetails.stepTo.day"
  val fromDateIsAfterToDate = "error.writtenAgreement.steppedDetails.stepFrom.day"
  val overlappingDates = "error.steppedDetails.overlappingDates"
  val invalidPostcode = "error.invalid_postcode"
  val invalidPostcodeOnLetter = "error.invalid_postcode_as_on_letter"

  //page 0
  val isConnectedError = "error.isRelated"

  //page 2
  val userTypeRequired = "error.userType.required"
  val contactDetailsMissing = "error.contact.details.missing"
  val contactPhoneRequired = "error.contact.phone.required"
  val contactEmailRequired = "error.contact.email.required"
  val alternativeAddressMissing = "error.alternative.address.missing"
  val alternativeContactMissing  = "error.alternative.contact.missing"

  //page 3
  val propertyTypeRequired = "error.propertyType.required"
  val companyNameRequired = "error.companyName.required"
  val occupiersNameRequired = "error.occupiersName.required"
  val noRentDetailsRequired = "error.noRentDetails.required"


  val occupierTypeRequired = "error.occupierType.required"
  val propertyOwnedByYouRequired = "error.propertyOwnedByYou.required"
  val propertyRentedByYouRequired = "error.propertyRentedByYou.required"

  //page 4
  val propertyIsSublet = "error.propertyIsSublet.required"
  val tooManySublets = "error.too_many_sublets"
  val subletTypeRequired = "error.subletType.required"

  //page 5
  val LandlordConnectionTypeRequired = "error.LandlordConnectionType.required"

  //Page 6
  val tooManySteppedRents = "error.too_many_stepped_rents"
  val leaseAgreementTypeRequired = "error.leaseType.required"
  val leaseAgreementOpenEndedRequired = "error.leaseOpenEnded.required"
  val leaseAgreementBreakClauseRequired = "error.leaseHasBreakClause.required"
  val leaseAgreementIsSteppedRequired = "error.steppedRent.required"
  val toDateToFarFuture = "error.date.invalid.range.stepTo.day"

  //page 7
  val rentReviewDetailsRequired = "error.rentReviewDetails.required"
  val rentReviewFrequencyRequired = "error.rentReviewFrequency.required"
  val rentCanBeReducedOnReviewRequired = "error.canRentBeReducedOnReview.required"
  val isRentResultOfReviewRequired = "error.isRentResultOfReview.required"
  val rentWasAgreedBetweenRequired = "error.rentWasAgreedBetween.required"
  val rentFixedByRequired = "error.rentWasFixedBy.required"
  val rentBaseTypeRequired = "error.rentBaseOn.required"

  //page 8
  val wasTheRentFixedBetweenRequired = "error.wasTheRentFixedBetween.required"
  val whoWasTheRentFixedBetweenRequired = "error.whoWasTheRentFixedBetween.required"
  val isThisRentRequired = "error.isThisRent.required"

  //page 9
  val rentBasedOnRequired = "error.rentBaseOn.required"
  val negotiatingNewRentRequired ="error.negotiatingNewRent.required"

  //page 10
  val includesLivingAccommodationRequired = "error.includesLivingAccommodation.required"
  val isRentPaidForPartRequired = "error.isRentPaidForPart.required"
  val anyOtherBusinessPropertyRequired = "error.anyOtherBusinessProperty.required"
  val rentBasedOnLandOnlyRequired = "error.rentBasedOnLandOnly.required"
  val rentBasedOnEmptyBuildingRequired = "error.rentBasedOnEmptyBuilding.required"
  val includesParkingRequired = "error.rentIncludesParking.required"
  val parkingRequired = "error.required.parking"
  val tenantPaysForParkingRequired = "error.tenantPaysForParking.required"

  //Page 11
  val rentFreePeriodRequired = "error.rentFreePeriod.required"
  val paidCapitalSumRequired = "error.paidCapitalSum.required"
  val receivedCapitalSumRequired = "error.receivedCapitalSum.required"

  //page 12
  val tooManyServices = "error.too_many_services"
  val responsibleOutsideRepairsRequired = "error.responsibleOutsideRepairs.required"
  val responsibleInsideRepairsRequired = "error.responsibleInsideRepairs.required"
  val responsibleBuildingInsuranceRequired = "error.responsibleBuildingInsurance.required"
  val waterChargesIncludedRequired = "error.waterChargesIncluded.required"
  val businessRatesRequired = "error.businessRates.required"
  val serviceChargesIncludedRequired = "error.serviceChargesIncluded.required"

  //page 13
  val tooManyAlterations = "error.too_many_alterations"
  val hasTenantDonePropertyAlterationsRequired = "error.hasTenantDonePropertyAlterations.required"
  val tenantWasRequiredToMakeAlterationsRequired = "error.tenantWasRequiredToMakeAlterations.required"
  val whichWorksWereDoneRequired = "error.whichWorksWereDone.required"

  //page 14
  val anyOtherFactorsRequired="error.anyOtherFactors.required"

  //declaration page
  val declaration = "error.declaration"

  //generic errors
  val required = "error.required"
  val booleanMissing = "error.boolean_missing"
  val noValueSelected = "error.no_value_selected"
  val maxLength = "error.maxLength"
  val invalidPhone = "error.invalid_phone"
  val invalidCurrency = "error.invalid_currency"
  val password = "error.invalid_password"

  //numeric errors
  val invalidNumber = "error.invalid_number"
  val bigDecimalNegative = "error.BigDecimal_negative"
  val number = "error.number"

  //Dates
  val dateBefore1900 = "error.date_before_1900"
  val invalidDate = "error.invalid_date"
  val invalidDateMonth = "error.month.required"
  val invalidDateYear = "error.year.required"
  val dateMustBeInPast = "error.date_must_be_in_past"
  val invalidDurationMonths = "error.duration.months"
  val invalidDurationYears = "error.duration.years"
}