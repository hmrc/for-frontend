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

package models.serviceContracts.submissions

import models.{NamedEnum, NamedEnumSupport}

sealed trait OccupierType extends NamedEnum {
  val key = "occupierType"
}

object OccupierTypeCompany extends OccupierType {
  val name = "company"
}

object OccupierTypeIndividuals extends OccupierType {
  val name = "individuals"
}

object OccupierTypeNobody extends OccupierType {
  val name = "nobody"
}
object NoRelationVacated extends OccupierType {
  val name = "vacated"
}

object OccupierTypes extends NamedEnumSupport[OccupierType] {
  val all: List[OccupierType] = List(OccupierTypeIndividuals, OccupierTypeCompany, OccupierTypeNobody, NoRelationVacated)
}
