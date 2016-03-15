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

import models._
import models.serviceContracts.submissions._
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{FormError, Forms, Mapping}
import uk.gov.voa.play.form.ConditionalMappings._
import ConditionalMapping._

object MappingSupport {

  def checkEmail(prefix:String,contactDetails: ContactDetails) = checkFieldConstraint(contactDetails.email.isEmpty == false, prefix+"email1", Errors.contactEmailRequired);
  def checkPhone(prefix:String,contactDetails: ContactDetails) = checkFieldConstraint(contactDetails.phone.isEmpty == false, prefix+"phone", Errors.contactPhoneRequired);

  val emailsMatch: Constraint[ContactDetails] = Constraint("constraints.emails.match")({ contactDetails => {
    val cond = contactDetails.email == contactDetails.emailConfirmed
    createFieldConstraintFor(cond, "email.mismatch", Seq("email1", "email2"))
  }
  })

  val contactContactDetailsConstraint: Constraint[ContactDetails] = Constraint("constraints.alt.contact.contact.details")({
    contactDetails => {
      val cond = contactDetails.phone.isEmpty == false || contactDetails.email.isEmpty == false
      val fields = Seq("phone", "email1")
      createFieldConstraintFor(cond, Errors.contactDetailsMissing, fields)
    }
  })

  val positiveBigDecimal = bigDecimal
    .verifying("error.BigDecimal_negative", _ >= 0.0000)
    .transform({ s: BigDecimal => s.abs }, { v: BigDecimal => v })

  val decimalRegex = """^[0-9]{1,10}\.?[0-9]{0,2}$"""
  val cdbMaxCurrencyAmount = 9999999.99

  lazy val annualRent: Mapping[AnnualRent] = mapping(
    "rentLengthType" -> rentLengthType, 
    "annualRentExcludingVat" -> currency
  )(AnnualRent.apply)(AnnualRent.unapply).verifying(Errors.maxCurrencyAmountExceeded, _.annualRent <= cdbMaxCurrencyAmount)

  val currency: Mapping[BigDecimal] = text
    .verifying(Errors.invalidCurrency, x => (x.replace(",", "") matches decimalRegex) && BigDecimal(x.replace(",", "")) > 0.000)
    .transform({ s: String => BigDecimal(s.replace(",", "")) }, { v: BigDecimal => v.toString })
    .verifying(Errors.maxCurrencyAmountExceeded, _ <= cdbMaxCurrencyAmount)

  val nonNegativeCurrency: Mapping[BigDecimal] = text
    .verifying(Errors.invalidCurrency, x => (x.replace(",", "") matches decimalRegex) && BigDecimal(x.replace(",", "")) >= 0.000)
    .transform({ s: String => BigDecimal(s.replace(",", "")) }, { v: BigDecimal => v.toString })
    .verifying(Errors.maxCurrencyAmountExceeded, _ <= cdbMaxCurrencyAmount)

  val mandatoryBoolean = optional(boolean)
    .verifying(Errors.booleanMissing, _.isDefined)
    .transform({ s: Option[Boolean] => s.get }, { v: Boolean => Some(v) })

  import Formats._



  val postcodeRegex = """(GIR ?0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKPSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) ?[0-9][A-Z-[CIKMOV]]{2})"""
  val phoneRegex = """^^[0-9\s\+()-]+$"""
  val userType: Mapping[UserType] = Forms.of[UserType]
  val contactType: Mapping[ContactType] = Forms.of[ContactType]
  val contactAddressType: Mapping[ContactAddressType] = Forms.of[ContactAddressType]
  val propertyType: Mapping[PropertyType] = Forms.of[PropertyType]
  val occupierType: Mapping[OccupierType] = Forms.of[OccupierType]
  val landlordConnectionType: Mapping[LandlordConnectionType] = Forms.of[LandlordConnectionType]
  val leaseAgreementTypeMapping: Mapping[LeaseAgreementType] = Forms.of[LeaseAgreementType]
  val reviewIntervalTypeMapping: Mapping[ReviewIntervalType] = Forms.of[ReviewIntervalType]
  val rentFixedByTypeMapping: Mapping[RentFixedType] = Forms.of[RentFixedType]
  val rentBaseTypeMapping: Mapping[RentBaseType] = Forms.of[RentBaseType]
  val notReviewRentFixedTypeMapping: Mapping[NotReviewRentFixedType] = Forms.of[NotReviewRentFixedType]
  val rentSetByTypeMapping: Mapping[RentSetByType] = Forms.of[RentSetByType]
  val responsibleTypeMapping: Mapping[ResponsibleType] = Forms.of[ResponsibleType]
  val contactAddressTypeMapping: Mapping[ContactAddressType] = Forms.of[ContactAddressType]
  val rentLengthType: Mapping[RentLengthType] = Forms.of[RentLengthType]
  val postcode: Mapping[String] = text verifying (Errors.invalidPostcode, _.toUpperCase matches postcodeRegex)
  val phoneNumber: Mapping[String] = nonEmptyText(maxLength = 20) verifying (Errors.invalidPhone, _ matches phoneRegex)

  def addressMapping(prefix: String) = mapping(
    "buildingNameNumber" -> nonEmptyText(maxLength = 50),
    "street1" -> optional(text(maxLength = 50)),
    "street2" -> optional(text(maxLength = 50)),
    "postcode" -> nonEmptyTextOr(s"$prefix.postcode", postcode)
  )(Address.apply)(Address.unapply)

  def addressAbroadMapping(prefix: String) = mapping(
    "buildingNameNumber" -> default(text(maxLength = 50), ""),
    "street1" -> optional(text(maxLength = 50)),
    "street2" -> optional(text(maxLength = 50)),
    "postcode" -> default(text(maxLength = 10), "")
  )(Address.apply)(Address.unapply)

  def contactDetailsMappingFor(contactTypeField: String): Mapping[ContactDetails] = {
    mapping(
      "phone" -> mandatoryIfAnyOf(contactTypeField, Seq(ContactTypePhone.name, ContactTypeBoth.name),
        nonEmptyTextOr("contactDetails.phone", phoneNumber)),
      "email1" -> mandatoryIfAnyOf(contactTypeField, Seq(ContactTypeEmail.name, ContactTypeBoth.name), email),
      "email2" -> mandatoryIfAnyOf(contactTypeField, Seq(ContactTypeEmail.name, ContactTypeBoth.name), email)
    )(ContactDetails.apply)(ContactDetails.unapply) verifying emailsMatch
  }

  val alternativeContactDetailsMapping = mapping(
    "phone" -> nonEmptyTextOr("alternativeContact.contactDetails.phone", phoneNumber),
    "email1" -> email,
    "email2" -> email
  )((p, e1, e2) =>
    ContactDetails(Some(p), Some(e1), Some(e2))
  )(details =>
    Some((details.phone.get, details.email.get, details.emailConfirmed.get))
    ) verifying emailsMatch

  val alternativeContactContactDetailsMapping = alternativeContactDetailsMapping verifying contactContactDetailsConstraint

  def alternativeContactMapping(prefix: String) = mapping(
    "fullName" -> text(minLength = 1, maxLength = 50),
    "contactDetails" -> alternativeContactContactDetailsMapping,
    "address" -> addressMapping(s"$prefix.address"))(Contact.apply)(Contact.unapply)

  def parkingDetailsMapping(key: String) = mapping(
    "openSpaces" -> default(number(min = 0), 0).verifying(Errors.maxLength, _ <= 9999),
    "coveredSpaces" -> default(number(min = 0), 0).verifying(Errors.maxLength, _ <= 9999),
    "garages" -> default(number(min = 0), 0).verifying(Errors.maxLength, _ <= 9999)
  )(ParkingDetails.apply)(ParkingDetails.unapply) verifying atLeastOneParkingDetailRequired(key)

  def atLeastOneParkingDetailRequired(key: String) = Constraint[ParkingDetails]("constraints.parkingDetails") { pd =>
    if(pd.openSpaces > 0 || pd.coveredSpaces > 0 || pd.garages > 0) {
      Valid
    } else {
      Invalid(ValidationError(Errors.parkingRequired))
    }
  }

  case class IndexedMapping[T](key: String, wrapped: (String => Mapping[T]), constraints: Seq[Constraint[List[T]]] = Nil,
                               allowEmpty: Boolean = false, alwaysValidateFirstIndex: Boolean = false) extends Mapping[List[T]] {

    def indexes(data: Map[String, String]): List[Int] = {
      val keyPattern = ("""^""" + key + """\[(\d+)\].*$""").r
      val indicesInData = data.toList.collect { case (keyPattern(index), _) => index.toInt }
      val all = if (alwaysValidateFirstIndex) indicesInData :+ 0 else indicesInData
      all.distinct.sorted
    }

    override val mappings: Seq[Mapping[_]] = wrapped(s"$key[0]").mappings

    override def verifying(addConstraints: Constraint[List[T]]*): Mapping[List[T]] = {
      this.copy[T](constraints = constraints ++ addConstraints.toSeq)
    }

    override def unbind(value: List[T]): Map[String, String] = {
      val datas = value.zipWithIndex.map { case (t, i) => wrapped(key + "[" + i + "]").unbind(t) }
      datas.foldLeft(Map.empty[String, String])(_ ++ _)
    }

    override def withPrefix(prefix: String): Mapping[List[T]] = this

    override def unbindAndValidate(value: List[T]): (Map[String, String], Seq[FormError]) = {
      val (datas, errors) = value.zipWithIndex.map { case (t, i) => wrapped(key + "[" + i + "]").unbindAndValidate(t) }.unzip
      (datas.foldLeft(Map.empty[String, String])(_ ++ _), errors.flatten ++ collectErrors(value))
    }

    private def indexedKey(index: Int) = s"$key[$index]"

    override def bind(data: Map[String, String]): Either[Seq[FormError], List[T]] = {
      val bound = for (index <- indexes(data)) yield wrapped(indexedKey(index)).bind(data)
      if(bound.forall(_.isRight)) {
        if (bound.isEmpty && !allowEmpty)
          bind(allEmptyFieldsForIndexZero)
        else
          Right(bound.map(_.right.get)).right.flatMap(applyConstraints)
      } else {
        Left(bound.collect { case Left(errors) => errors }.flatten)
      }
    }

    private def allEmptyFieldsForIndexZero = wrapped(s"$key[0]").mappings.flatMap(_.keys).map((_, "")).toMap
  }
}
