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

import models._
import models.serviceContracts.submissions._
import play.api.data.FormError
import play.api.data.format.Formatter

object Formats {

  def namedEnumFormatter[T <: NamedEnum](named:NamedEnumSupport[T], missingCode:String): Formatter[T] = new Formatter[T] {

    override val format = Some((missingCode, Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      val resOpt = for {
        keyVal <- data.get(key)
        enumTypeValue <- named.fromName(keyVal)
      } yield {
          Right(enumTypeValue)
        }
      resOpt.getOrElse(Left(Seq(FormError(key, missingCode, Nil))))
    }

    def unbind(key: String, value: T): Map[String, String] = Map(key -> value.name)
  }

  def namedEnumFormatterWithKeys[T <: NamedEnum](named:NamedEnumSupport[T], missingCodes:Map[String, String]): Formatter[T] = new Formatter[T] {

    override val format = Some((Errors.required, Nil))

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      val resOpt = for {
        keyVal <- data.get(key)
        enumTypeValue <- named.fromName(keyVal)
      } yield {
        Right(enumTypeValue)
      }
      val maybeString = missingCodes.get(key)
      resOpt.getOrElse(Left(Seq(FormError(key, maybeString.getOrElse(Errors.required), Nil))))
    }

    def unbind(key: String, value: T): Map[String, String] = Map(key -> value.name)
  }

  implicit val userTypeFormat: Formatter[UserType] = namedEnumFormatter(UserTypes, Errors.userTypeRequired)
  implicit val contactAddressTypeFormat: Formatter[ContactAddressType] = namedEnumFormatter(ContactAddressTypes, Errors.required)
  implicit val propertyTypeFormat: Formatter[PropertyType] = namedEnumFormatter(PropertyTypes, Errors.noValueSelected)
  implicit val occupierTypeFormat: Formatter[OccupierType] = namedEnumFormatter(OccupierTypes, Errors.occupierTypeRequired)
  implicit val landlordConnTypeFormat: Formatter[LandlordConnectionType] = namedEnumFormatter(LandlordConnectionTypes, Errors.LandlordConnectionTypeRequired)
  implicit val leaseAgreementTypeFormat: Formatter[LeaseAgreementType] = namedEnumFormatter(LeaseAgreementTypes, Errors.leaseAgreementTypeRequired)
  implicit val reviewIntervalTypeFormat: Formatter[ReviewIntervalType] = namedEnumFormatter(ReviewIntervalTypes, Errors.rentReviewFrequencyRequired)
  implicit val rentFixedByTypeFormat: Formatter[RentFixedType] = namedEnumFormatter(RentFixedTypes, Errors.rentFixedByRequired)

  implicit val rentBaseTypeFormat: Formatter[RentBaseType] = namedEnumFormatter(RentBaseTypes, Errors.rentBasedOnRequired)
  implicit val notReviewRentFixedTypeFormat: Formatter[NotReviewRentFixedType] = namedEnumFormatter(NotReviewRentFixedTypes, Errors.whoWasTheRentFixedBetweenRequired)
  implicit val rentSetByTypesFormat: Formatter[RentSetByType] = namedEnumFormatter(RentSetByTypes, Errors.isThisRentRequired)

  implicit val responsibleTypesFormat: Formatter[ResponsibleType] = namedEnumFormatterWithKeys(ResponsibleTypes, Map(
    "responsibleOutsideRepairs" ->Errors.responsibleOutsideRepairsRequired,
    "responsibleInsideRepairs" ->Errors.responsibleInsideRepairsRequired,
    "responsibleBuildingInsurance" ->Errors.responsibleBuildingInsuranceRequired
  ))

  implicit val formatRentLengthType: Formatter[RentLengthType] = namedEnumFormatter(RentLengthTypes, Errors.noValueSelected)
  implicit val satisfactionFormatter: Formatter[Satisfaction] = namedEnumFormatter(SatisfactionTypes, Errors.noValueSelected)

  implicit val addressConnectionTypeFormatter: Formatter[AddressConnectionType] = namedEnumFormatter(AddressConnectionTypes, Errors.isConnectedError)
  implicit val alterationSetByTypeFormatter: Formatter[AlterationSetByType] = namedEnumFormatter(AlterationSetByType, Errors.whichWorksWereDoneRequired)
  implicit val journeyFormatter: Formatter[Journey] = namedEnumFormatter(JourneyTypes, Errors.noValueSelected)
  implicit val subletTypeFormatter: Formatter[SubletType] = namedEnumFormatter(SubletType, Errors.subletTypeRequired)
}
