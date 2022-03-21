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

package models

import models.pages.Summary
import models.serviceContracts.submissions.Address
import play.api.libs.json.{JsObject, Json}

object Addresses {

  def getAddress(summary: Summary): Address = {
    summary.propertyAddress orElse summary.address match {
      case Some(a) => a
      case None => throw new AddressMissing(summary.referenceNumber)
    }
  }

  def addressJson(summary: Summary): JsObject =
    summary.address.fold(Json.obj())(address => Json.obj("address" -> address)) ++
      summary.propertyAddress
        .filter(newAddress => !summary.address.contains(newAddress))
        .fold(Json.obj())(newAddress => Json.obj("updatedAddress" -> newAddress))

}

class AddressMissing(referenceNumber: String) extends Exception(s"Reference number: $referenceNumber")
