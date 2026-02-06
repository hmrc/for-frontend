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

sealed trait SubletType extends NamedEnum {
  override val key = "subletType"
}

object SubletAll extends SubletType {
  override val name: String = "all"
}

object SubletPart extends SubletType {
  override val name: String = "part"
}

object SubletType extends NamedEnumSupport[SubletType] {
  override def all: List[SubletType] = List(SubletAll, SubletPart)
}
