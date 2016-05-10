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

package form

object Errors {
  val required = "error.required"
  val emailMismatch = "email.mismatch"
  val addressRequired  = "address.required"
  val contactDetailsMissing = "error.contact.details.missing"
  val contactPhoneRequired = "error.contact.phone.required"
  val contactEmailRequired = "error.contact.email.required"
  val alternativeAddressMissing = "error.alternative.address.missing"
  val alternativeContactMissing  = "error.alternative.contact.missing"
  val booleanMissing = "error.boolean_missing"
  val contactMissing = "error.contact.missing"
  val landlordConnectionTypeMissing = "error.landlord_connection_type_missing"
  val lastReviewalIntervalTypeMissing = "error.rent_review_interval_type_missing"
  val lastReviewalDateMissing = "error.last_review_date_missing"
  val addressBuildingNameNumberRequired = "error.address.buildingName.required"
  val addressTownCityRequired = "error.address.townCity.required"
  val addressPostcodeRequired = "error.address.postcode.required"
  val rentReviewDetailsMissing = "error.rent_review_details.required"
  val rentFixedByTypeMissing = "error.rent_fixed_by.required"
  val leaseAgreementTypeMissing = "error.lease.agreement.type.missing"
  val notReviewRentFixedTypeMissing = "error.not_review_rent_fixed_type.missing"
  val rentSetByTypesMissing = "error.rent_set_by.missing"
  val paymentDetailsMissing = "error.payment.details.required"
  val responsibleTypesMissing = "error.responsible.types.missing"
  val waterChargeResponsibleTypesMissing = "error.water.charges.responsible.types.missing"
  val bigDecimalNegative = "error.BigDecimal_negative"
  val rentBaseTypeMissing = "error.rent.base.type.missing"
  val declaration = "error.declaration"
  val maxLength = "error.maxLength"
  val invalidPostcode = "error.invalid_postcode"
  val invalidPhone = "error.invalid_phone"
  val invalidCurrency = "error.invalid_currency"
  val dateBefore1900 = "error.date_before_1900"
  val invalidDate = "error.invalid_date"
  val invalidNumber = "error.invalid_number"
  val dateMustBeInPast = "error.date_must_be_in_past"
  val number = "error.number"
  val invalidDurationMonths = "error.duration.months"
  val invalidDurationYears = "error.duration.years"
  val noValueSelected = "error.no_value_selected"
  val invalidRefNum = "error.invalid_refnum"
  val tooManySteppedRents = "error.too_many_stepped_rents"
  val tooManyServices = "error.too_many_services"
  val tooManyAlterations = "error.too_many_alterations"
  val tooManySublets = "error.too_many_sublets"
  val parkingRequired = "error.required.parking"
  val maxCurrencyAmountExceeded = "error.maxCurrencyAmountExceeded"
  val toDateIsAfterFromDate = "error.writtenAgreement.steppedDetails.stepTo.day"



}
