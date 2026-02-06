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
import form.PageZeroForm.pageZeroForm
import form.PageEightForm.pageEightForm
import form.PageElevenForm.pageElevenForm
import form.PageFiveForm.pageFiveForm
import form.PageFourForm.pageFourForm
import form.PageFourteenForm.pageFourteenForm
import form.PageNineForm.pageNineForm
import form.PageOneForm.pageOneForm
import form.PageSevenForm.pageSevenForm
import form.PageSixForm.pageSixForm
import form.PageTenForm.pageTenForm
import form.PageThirteenForm.pageThirteenForm
import form.PageThreeForm.pageThreeForm
import form.PageTwelveForm.pageTwelveForm
import form.PageTwoForm.pageTwoForm
import play.api.data.Form

trait SummaryBuilder {
  def build(doc: Document): Summary
}

object SummaryBuilder extends SummaryBuilder {

  def build(doc: Document): Summary = {
    val p0  = findPage(doc, 0, pageZeroForm)
    val p1  = findPage(doc, 1, pageOneForm)
    val p2  = findPage(doc, 2, pageTwoForm)
    val p3  = findPage(doc, 3, pageThreeForm)
    val p4  = findPage(doc, 4, pageFourForm)
    val p5  = findPage(doc, 5, pageFiveForm)
    val p6  = findPage(doc, 6, pageSixForm)
    val p7  = findPage(doc, 7, pageSevenForm)
    val p8  = findPage(doc, 8, pageEightForm)
    val p9  = findPage(doc, 9, pageNineForm)
    val p10 = findPage(doc, 10, pageTenForm)
    val p11 = findPage(doc, 11, pageElevenForm)
    val p12 = findPage(doc, 12, pageTwelveForm)
    val p13 = findPage(doc, 13, pageThirteenForm)
    val p14 = findPage(doc, 14, pageFourteenForm)
    Summary(
      doc.referenceNumber,
      doc.journeyStarted,
      p0,
      p1,
      p2,
      p3,
      p4,
      p5,
      p6,
      p7,
      p8,
      p9,
      p10,
      p11,
      p12,
      p13,
      p14,
      address = doc.address,
      journeyResumptions = doc.journeyResumptions
    )
  }

  private def findPage[T](doc: Document, pageNumber: Int, form: Form[T]) = doc.page(pageNumber) flatMap { p =>
    form.bindFromRequest(p.fields).value
  }
}
