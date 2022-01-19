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

package models.journeys

import models.journeys.Paths._
import models.pages.Summary

sealed trait TargetPage
case object SummaryPage extends TargetPage
case class PageToGoTo(number: Int) extends TargetPage

object Journey {

  def pageToResumeAt(summary: Summary): TargetPage = {
    val path = Paths.pathFor(summary)
    path.firstIncompletePageFor(summary) match {
      case None =>
        SummaryPage
      case Some(page) =>
        nextPageAllowable(page, summary)
    }
  }

  def nextPageAllowable(targetPage: Int, summary: Summary, currentPage: Option[Int] = None): TargetPage = {
    pageToShow(targetPage, summary, currentPage) match {
      case 16 => SummaryPage
      case n => PageToGoTo(n)
    }
  }

  private def pageToShow(target: Int, summary: Summary, current: Option[Int]): Int = {
    val path = Paths.pathFor(summary)
    if (target == 0) {
      target
    } else if (path.contains(target) && path.previousPageIsComplete(target, summary)) {
      target
    } else if (movingBackwards(target, current)) {
      path.previousPage(target)
    } else {
      path.nextPage(target, summary) getOrElse 16
    }
  }

  private def movingBackwards(target: Int, current: Option[Int]) = current.map(_ > target) getOrElse false

  def pageIsNotApplicable(target: Int, summary: Summary): Boolean = {
    !Paths.pathFor(summary).contains(target)
  }

  def lastPageFor(summary: Summary): Int = pathFor(summary).lastPage
}
