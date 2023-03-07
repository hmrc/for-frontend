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

package controllers.dataCapturePages

import actions.{RefNumAction, RefNumRequest}
import connectors.Audit
import form.PageSevenForm.pageSevenForm
import form.PageSixForm.pageSixForm
import form.persistence.FormDocumentRepository
import models._
import models.pages.{PageSeven, PageSix, Summary}
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import playconfig.SessionId

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PageSixController @Inject()(audit: Audit,
                                  formDocumentRepository: FormDocumentRepository,
                                  refNumAction: RefNumAction,
                                  cc: MessagesControllerComponents,
                                  part6: views.html.part6)
  extends ForDataCapturePage[PageSix](audit, formDocumentRepository, refNumAction, cc) {
  val format = p6f
  val emptyForm = pageSixForm
  val pageNumber: Int = 6

  def template(form: Form[PageSix], summary: Summary)(implicit request: RefNumRequest[AnyContent]): Html = {
    val updatedForm: Form[PageSix] = Await.result(repository.findById(SessionId(hc), request.refNum).map { docOpt =>
      (for {
        doc <- docOpt
        page7 <- doc.page(7)
        pageSeven <- pageSevenForm.bindFromRequest(page7.fields).value
      } yield {
        form.copy(data = form.data ++ getReviewDatesFromPage7(pageSeven))
      }).getOrElse(form)
    }, 20 seconds)

    // Min 2 steps are required in stepped rent
    val finalForm: Form[PageSix] = updatedForm.copy(data =
      updatedForm.data ++ (0 to 1).map(idx => s"writtenAgreement.steppedDetails[$idx].indexSizeHolder" -> "x")
    )

    part6(finalForm, summary)
  }

  private def getReviewDatesFromPage7(pageSeven: PageSeven): Map[String, String] =
    Seq(
      pageSeven.pageSevenDetails.filter(_.rentResultOfRentReview).flatMap(_.reviewDetails.map(_.whenWasRentReview))
        .map("rentReviewDate" -> _.toLocalDate.toString),
      pageSeven.pageSevenDetails.flatMap(_.lastReviewDate)
        .map("lastReviewDate" -> _.toLocalDate.toString)
    ).flatten.toMap

}
