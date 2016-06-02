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

package models.journeys

import models.pages.Summary
import models.serviceContracts.submissions.{LeaseAgreementTypesVerbal, UserNoRelationVacated}

object Paths {
  val standardPath = new Path(1 to 14)
  val shortPath = new Path(1 to 4)
  val verbalAgreementPath = new Path((1 to 14).filterNot(_ == 7))
  val rentReviewPaths = new Path((1 to 14).filterNot(_ == 8))
  val vacatedPath = new Path((1 to 2))


  def pathFor(summary: Summary): Path = {
    if (isShortPath(summary)) shortPath
    else if (summary.lease.isDefined && summary.lease.get.leaseAgreementType == LeaseAgreementTypesVerbal) verbalAgreementPath
    else if (summary.rentReviews.isDefined && summary.rentReviews.get.leaseContainsRentReviews) rentReviewPaths
    else if(summary.customerDetails.isDefined && summary.customerDetails.get.userType == UserNoRelationVacated) vacatedPath
    else standardPath
  }

  def isShortPath(summary: Summary): Boolean = {
    val page4IsComplete = summary.sublet.isDefined
    val page3IsComplete = summary.theProperty.isDefined
    lazy val propertyIsNotRentedByYou = !summary.theProperty.get.propertyRentedByYou.getOrElse(false)
    lazy val propertyIsOwnedByYou = summary.theProperty.get.propertyOwnedByYou
    page3IsComplete && page4IsComplete && (propertyIsNotRentedByYou || propertyIsOwnedByYou)
  }
}

class Path(private val pages: Seq[Int]) {
  val lastPage = pages.last

  def firstIncompletePageFor(summary: Summary): Option[Int] = {
    val asList = summaryAsList(summary)
    pages.find(n => asList(n - 1).isEmpty)
  }

  def contains(page: Int): Boolean = pages.contains(page)

  def previousPage(page: Int): Int = pages.takeWhile(_ < page).lastOption.getOrElse(0)

  def nextPage(page: Int, summary: Summary): Option[Int] = {
    val summaryList = summaryAsList(summary)
    pages.dropWhile(p => summaryList(p - 1).isDefined && p < page).headOption
  }

  def previousPageIsComplete(page: Int, summary: Summary): Boolean = {
    summaryAsList(summary)(previousPage(page) - 1).isDefined
  }

  private def summaryAsList(summary: Summary): List[Option[_]] = List(
    summary.propertyAddress,
    summary.customerDetails,
    summary.theProperty,
    summary.sublet,
    summary.landlord,
    summary.lease,
    summary.rentReviews,
    summary.rentAgreement,
    summary.rent,
    summary.rentIncludes,
    summary.incentives,
    summary.responsibilities,
    summary.alterations,
    summary.otherFactors
  )
}
