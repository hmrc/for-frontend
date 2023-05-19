/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.data.validation._
import play.api.data.{FormError, Forms, Mapping}
import ConditionalMapping._
import play.api.data.validation.Constraints.{maxLength, minLength, nonEmpty, pattern}

import javax.mail.internet.InternetAddress
import scala.util.Try

object MappingSupport {

  private val strictEmail = email.verifying("error.email", value => Try(new InternetAddress(value, true)).isSuccess)

  val positiveBigDecimal = bigDecimal
    .verifying("error.BigDecimal_negative", _ >= 0.0000)
    .transform({ s: BigDecimal => s.abs }, { v: BigDecimal => v })

  val decimalRegex = """^[0-9]{1,10}\.?[0-9]{0,2}$"""
  val cdbMaxCurrencyAmount = 9999999.99
  val spacesIntRegex = """^\-?\d{1,10}$""".r
  val intRegex = """^\d{1,3}$""".r

  lazy val annualRent: Mapping[AnnualRent] = mapping(
    "annualRentExcludingVat" -> currencyMapping(".annualRentExcludingVat")
  )(AnnualRent.apply)(AnnualRent.unapply).verifying(Errors.maxCurrencyAmountExceeded, _.amount <= cdbMaxCurrencyAmount)

  val currency: Mapping[BigDecimal] = currencyMapping()

  def currencyMapping(fieldErrorPart: String = ""): Mapping[BigDecimal] = default(text, "")
    .verifying(nonEmpty(errorMessage = Errors.required + fieldErrorPart))
    .verifying(Errors.invalidCurrency + fieldErrorPart, x => x == "" || ((x.replace(",", "") matches decimalRegex) && BigDecimal(x.replace(",", "")) >= 0.000))
    .transform({ s: String => BigDecimal(s.replace(",", "")) }, { v: BigDecimal => v.toString })
    .verifying(Errors.maxCurrencyAmountExceeded + fieldErrorPart, _ <= cdbMaxCurrencyAmount)

  val nonNegativeCurrency: Mapping[BigDecimal] = text
    .verifying(Errors.invalidCurrency, x => (x.replace(",", "") matches decimalRegex) && BigDecimal(x.replace(",", "")) >= 0.000)
    .transform({ s: String => BigDecimal(s.replace(",", "")) }, { v: BigDecimal => v.toString })
    .verifying(Errors.maxCurrencyAmountExceeded, _ <= cdbMaxCurrencyAmount)

  val mandatoryBoolean = optional(boolean)
    .verifying(Errors.booleanMissing, _.isDefined)
    .transform({ s: Option[Boolean] => s.get }, { v: Boolean => Some(v) })


  def mandatoryBooleanWithError(message: String) = {
    optional(boolean)
      .verifying(message, _.isDefined)
      .transform({ s: Option[Boolean] => s.get }, { v: Boolean => Some(v) })
  }

  import Formats._ // scalastyle:ignore

  val postcodeRegex = """(GIR ?0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKPSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) ?[0-9][A-Z-[CIKMOV]]{2})""" //scalastyle:ignore
  val phoneRegex = """^^[0-9\s\+()-]+$"""
  val userType: Mapping[UserType] = Forms.of[UserType]
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
  val postcode: Mapping[String] = PostcodeMapping.postcode()
  val loginPostcode: Mapping[String] = PostcodeMapping.postcode(Errors.invalidPostcodeOnLetter, Errors.invalidPostcodeOnLetter)
  val phoneNumber: Mapping[String] = nonEmptyText(maxLength = 20).verifying(Errors.invalidPhone, _ matches phoneRegex)
  val addressConnectionType: Mapping[AddressConnectionType] = Forms.of[AddressConnectionType]
  val alterationSetByTypeMapping: Mapping[AlterationSetByType] = Forms.of[AlterationSetByType]
  val subletTypeMapping: Mapping[SubletType] = Forms.of[SubletType]

  def addressMapping: Mapping[Address] = mapping(
    "buildingNameNumber" -> default(text, "").verifying(
      nonEmpty(errorMessage = "error.buildingNameNumber.required"),
      maxLength(50, "error.buildingNameNumber.maxLength")
    ),
    "street1" -> optional(text(maxLength = 50)),
    "street2" -> optional(text(maxLength = 50)),
    "postcode" ->  nonEmptyTextOr("postcode", postcode, "error.postcode.required")
  )(Address.apply)(Address.unapply)

  def addressMapping(prefix: String): Mapping[Address] = mapping(
    "buildingNameNumber" -> default(text, "").verifying(
      nonEmpty(errorMessage = "error.buildingNameNumber.required"),
      maxLength(50, "error.buildingNameNumber.maxLength")
    ),
    "street1" -> optional(text(maxLength = 50)),
    "street2" -> optional(text(maxLength = 50)),
    "postcode" -> nonEmptyTextOr(s"$prefix.postcode", postcode, "error.postcode.required")
  )(Address.apply)(Address.unapply)

  def optionalAddressMapping(prefix: String): Mapping[Address] = mapping(
    "buildingNameNumber" -> default(
      text.verifying(maxLength(50, "error.buildingNameNumber.maxLength")), ""
    ),
    "street1" -> optional(
      text.verifying(maxLength(50, "error.line2.maxLength"))
    ),
    "street2" -> optional(
      text.verifying(maxLength(50, "error.line3.maxLength"))
    ),
    "postcode" -> default(
      postcode.verifying(maxLength(10, "error.postcode.maxLength")), ""
    )
  )(Address.apply)(Address.unapply)

  val contactDetailsMapping: Mapping[ContactDetails] =
    mapping(
      "phone" -> default(text, "").verifying(
        nonEmpty(errorMessage = Errors.contactPhoneRequired),
        pattern(phoneRegex.r, error = Errors.invalidPhone),
        minLength(11, "error.contact.phone.minLength"),
        maxLength(20, "error.contact.phone.maxLength")
      ),
      "email1" -> default(strictEmail, "").verifying(
        nonEmpty(errorMessage = Errors.contactEmailRequired),
        maxLength(50, "contactDetails.email1.email.tooLong")
      )
    )(ContactDetails.apply)(ContactDetails.unapply)

  def parkingDetailsMapping(key: String): Mapping[ParkingDetails] = mapping(
    "openSpaces" -> spacesOrGaragesMapping(key, "openSpaces"),
    "coveredSpaces" -> spacesOrGaragesMapping(key, "coveredSpaces"),
    "garages" -> spacesOrGaragesMapping(key, "garages")
  )(ParkingDetails.apply)(ParkingDetails.unapply) verifying atLeastOneParkingDetailRequired(key)

  private def spacesOrGaragesMapping(key: String, field: String): Mapping[Int] = default(text, "0")
    .verifying(s"${Errors.invalidNumber}.$key.$field", x => x == "0" || spacesIntRegex.findFirstIn(x).isDefined )
    .transform[Int](_.replace(",", "").toInt, _.toString)
    .verifying(s"error.minValue.$key.$field", _ >= 0)
    .verifying(s"error.maxValue.$key.$field", _ <= 9999)

  def intMapping(): Mapping[Int] = default(text, "0")
    .verifying("error.maxValueRentFreeIsBlank.required", x => x == "0" || intRegex.findFirstIn(x).isDefined)
    .transform[Int](_.replace(",", "").toInt, _.toString)
    .verifying(s"error.empty.required", _ >= 1)

  def atLeastOneParkingDetailRequired(key: String): Constraint[ParkingDetails] =
    Constraint[ParkingDetails]("constraints.parkingDetails") { pd =>
      if (pd.openSpaces > 0 || pd.coveredSpaces > 0 || pd.garages > 0) {
        Valid
      } else {
        Invalid(ValidationError(s"${Errors.required}.$key"))
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
      val bound = for { index <- indexes(data) } yield wrapped(indexedKey(index)).bind(data)
      if (bound.forall(_.isRight)) {
        if (bound.isEmpty && !allowEmpty)
          bind(allEmptyFieldsForIndexZero)
        else
          Right(bound.map(_.toOption.get)).flatMap(applyConstraints)
      } else {
        Left(bound.collect { case Left(errors) => errors }.flatten)
      }
    }

    private def allEmptyFieldsForIndexZero = wrapped(s"$key[0]").mappings.flatMap(_.keys).map((_, "")).toMap
  }

}
