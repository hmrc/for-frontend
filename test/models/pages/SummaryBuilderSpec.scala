/*
 * Copyright 2023 HM Revenue & Customs
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

package models.pages

import connectors.Document
import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class SummaryBuilderSpec extends AnyFlatSpec with should.Matchers {

  behavior of "Summary builder"

  it should "map the reference number number, journey started date, and journey resumptions" in {
    val now = DateTime.now.minusDays(5)
    val resumptions = Seq(DateTime.now.minusDays(4), DateTime.now.minusDays(3), DateTime.now.minusDays(2))
    val d = Document("11122233344", now, Seq.empty, None, Some("secretPassword"), journeyResumptions = resumptions)
    val s = SummaryBuilder.build(d)
    assert(s.referenceNumber === "11122233344")
    assert(s.journeyStarted === now)
    assert(s.journeyResumptions === resumptions)
  }
}
