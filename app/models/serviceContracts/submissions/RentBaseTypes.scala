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

package models.serviceContracts.submissions

import models.{NamedEnum, NamedEnumSupport}

sealed trait RentBaseType extends NamedEnum {
  val key = "rentBaseTypes"
}

object RentBaseTypeOpenMarket extends RentBaseType {
  val name = "openMarket"
}

object RentBaseTypePercentageOpenMarket extends RentBaseType {
  val name = "percentageOpenMarket"
}

object RentBaseTypePercentageTurnover extends RentBaseType {
  val name = "percentageTurnover"
}

object RentBaseTypeIndexation extends RentBaseType {
  val name = "indexation"
}

object RentBaseTypeOther extends RentBaseType {
  val name = "other"
}

object RentBaseTypes extends NamedEnumSupport[RentBaseType] {

  val all: List[RentBaseType] =
    List(RentBaseTypeOpenMarket, RentBaseTypePercentageOpenMarket, RentBaseTypePercentageTurnover, RentBaseTypeIndexation, RentBaseTypeOther)
  val key: String             = all.head.key

}
