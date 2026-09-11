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

package models.journeys

import models.pages.Summary
import org.scalatest.prop.{TableFor1, TableFor2}
import uk.gov.hmrc.vo.unit.test.BaseSpec
import utils.SummaryBuilder.*

class ResumingAfterSavingSpec extends BaseSpec:

  import TestData.*

  "pageToResumeAt" should {
    "return summary page when resuming complete but undeclared submissions" in
      forAll(completeJourneys) { cj =>
        Journey.pageToResumeAt(cj) shouldBe SummaryPage
      }

    "return earliest incomplete page when resuming journeys for incomplete submissions" in
      forAll(incompleteJourneys) { case (journey, page) =>
        Journey.pageToResumeAt(journey) shouldBe PageToGoTo(page)
      }

    "return to page one when it has been made invalid by editing when resuming complete but undeclared submissions" in {
      Journey.pageToResumeAt(completeShortPathJourneyWithEditedPageOne) shouldBe PageToGoTo(1)
    }
  }

  object TestData:
    val completeJourneys: TableFor1[Summary] = Table("journey", completeShortPathJourney, completeFullPathJourney)

    val incompleteJourneys: TableFor2[Summary, Int] = Table(
      ("journey", "earliest incomplete page"),
      (incompletePageOneJourney, 1),
      (incompletePageFourJourney, 4),
      (incompletePageFourteenJourney, 14)
    )
