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

package models.pages

import connectors.Document
import uk.gov.hmrc.vo.unit.test.BaseSpec
import util.DateUtil.nowInUK

class SummaryBuilderSpec extends BaseSpec:

  "SummaryBuilder" should {
    "map the reference number number, journey started date, and journey resumptions" in {
      val now         = nowInUK.minusDays(5)
      val resumptions = Seq(nowInUK.minusDays(4), nowInUK.minusDays(3), nowInUK.minusDays(2))
      val d           = Document("11122233344", now, Seq.empty, None, Some("secretPassword"), journeyResumptions = resumptions)
      val s           = SummaryBuilder.build(d)

      s.referenceNumber    shouldBe "11122233344"
      s.journeyStarted     shouldBe now
      s.journeyResumptions shouldBe resumptions
    }
  }
