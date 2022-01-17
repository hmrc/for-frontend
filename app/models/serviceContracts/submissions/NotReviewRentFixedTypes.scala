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

sealed trait NotReviewRentFixedType extends NamedEnum{
  val key  = "notReviewRentFixedTypes"
}

object NotReviewRentFixedTypeFinalJudge extends NotReviewRentFixedType {
  val name = "finalJudge"
}

object NotReviewRentFixedTypeInterimRent extends NotReviewRentFixedType {
  val name = "interim"
}

object NotReviewRentFixedTypes extends NamedEnumSupport[NotReviewRentFixedType]{
  val all = List(NotReviewRentFixedTypeInterimRent,NotReviewRentFixedTypeFinalJudge)
  val key = all.head.key
}
