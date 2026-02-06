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

case class Address(buildingNameNumber: String, street1: Option[String], street2: Option[String], postcode: String) {

  def singleLine: String =
    List(Some(buildingNameNumber), street1, street2, Some(postcode.replaceAll("^(\\S+?)\\s*?(\\d\\w\\w)$", "$1 $2"))).flatten.mkString(", ")

  def multiLine: String =
    List(Some(buildingNameNumber), street1, street2, Some(postcode.replaceAll("^(\\S+?)\\s*?(\\d\\w\\w)$", "$1 $2"))).flatten.mkString("<br /> ")
}
