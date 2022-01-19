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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import utils.SummaryBuilder._

class ResumingAfterSavingSpec extends AnyFlatSpec with should.Matchers with TableDrivenPropertyChecks {
	import TestData._

	behavior of "Page to resume at"

	it should "return summary page when resuming complete but undeclared submissions" in {
		forAll(completeJourneys) { cj => 
			assert(Journey.pageToResumeAt(cj) === SummaryPage) 
		}
	}

	it should "return earliest incomplete page when resuming journeys for incomplete submissions" in {
		forAll(incompleteJourneys) { case (journey, page) =>
			assert(Journey.pageToResumeAt(journey) === PageToGoTo(page))
		}
	}

	it should "return to page one when it has been made invalid by editing when resuming complete but undeclared submissions" in {
		assert(Journey.pageToResumeAt(completeShortPathJourneyWithEditedPageOne) === PageToGoTo(1))
	}

	object TestData {
		val completeJourneys = Table("journey", completeShortPathJourney, completeFullPathJourney)
	
		val incompleteJourneys = Table(
			("journey", "earliest incomplete page"),
			(incompletePageOneJourney, 1),
			(incompletePageFourJourney, 4),
			(incompletePageFourteenJourney, 14)
		)
	}
}
