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

package models.serviceContracts.submissions

import models.{NamedEnum, NamedEnumSupport}

sealed trait AlterationSetByType extends NamedEnum {
  val key = "alterationSetByType"
}

object AlterationSetByTypeExtension extends AlterationSetByType {
  val name ="extension"
}

object AlterationSetByTypeDemolition extends AlterationSetByType {
  val name ="demolition"
}

object AlterationSetByTypeAddedMezzanineFloor extends AlterationSetByType {
  val name ="addedMezzanineFloor"
}

object AlterationSetByTypeRemovedWall extends AlterationSetByType {
  val name ="removedWall"
}

object AlterationSetByTypeAddLift extends AlterationSetByType {
  val name ="addLift"
}

object AlterationSetByTypeAddedParking extends AlterationSetByType {
  val name ="addedParking"
}

object AlterationSetByTypeAddedAirCondition extends AlterationSetByType {
  val name ="addedAirCondition"
}



object AlterationSetByType extends NamedEnumSupport[AlterationSetByType] {
  val all = List(AlterationSetByTypeExtension, AlterationSetByTypeDemolition,
    AlterationSetByTypeAddedMezzanineFloor, AlterationSetByTypeRemovedWall,
    AlterationSetByTypeAddLift, AlterationSetByTypeAddedParking, AlterationSetByTypeAddedAirCondition
  )
}
