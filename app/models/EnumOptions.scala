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

package models

import models.serviceContracts.submissions._
import play.api.i18n.Messages

object EnumOptions {

  def options[E <: NamedEnum](e: NamedEnumSupport[E])(implicit messages: Messages): Seq[(String, String)] =
    e.all.map { ut =>
      (ut.name, Messages(ut.msgKey))
    }

  def contactAddressTypeOptions(address: Address)(implicit messages: Messages): Seq[(String, String)] =
    ContactAddressTypes.all.map { ut =>
      if ut == ContactAddressTypeMain then
        (ut.name, address.singleLine)
      else
        (ut.name, Messages(ut.msgKey))
    }

  def originalContactAddressTypeOptions(address: String)(implicit messages: Messages): List[(String, String)] =
    ContactAddressTypes.all.map { ut =>
      if ut == ContactAddressTypeMain then
        (ut.name, address)
      else
        (ut.name, Messages(ut.msgKey))
    }

  def isTenantsAddressTypeOptions(address: Address)(implicit messages: Messages): Seq[(String, String)] =
    TenantsAddressTypes.all.map { ut =>
      if ut == TenantsAddressTypeMain then
        (ut.name, address.singleLine)
      else
        (ut.name, Messages(ut.msgKey))
    }

  def isTenantsAddressTypeOptionsOriginal(address: String)(implicit messages: Messages): Seq[(String, String)] =
    TenantsAddressTypes.all.map { ut =>
      if ut == TenantsAddressTypeMain then
        (ut.name, address)
      else
        (ut.name, Messages(ut.msgKey))
    }
}
