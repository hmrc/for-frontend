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

package controllers.dataCapturePages

import controllers._
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class PageController @Inject() (cc: MessagesControllerComponents,
                                application: ApplicationController,
                                pageZeroController: PageZeroController,
                                pageOneController: PageOneController,
                                pageTwoController: PageTwoController,
                                pageThreeController: PageThreeController,
                                pageFourController: PageFourController,
                                pageFiveController: PageFiveController,
                                pageSixController: PageSixController,
                                pageSevenController: PageSevenController,
                                pageEightController: PageEightController,
                                pageNineController: PageNineController,
                                pageTenController: PageTenController,
                                pageElevenController: PageElevenController,
                                pageTwelveController: PageTwelveController,
                                pageThirteenController: PageThirteenController,
                                pageFourteenController: PageFourteenController

                               )

  extends FrontendController(cc) {

  def showPage(pageNumber: Int): Action[AnyContent] =
    pageNumber match {
      case 0 => pageZeroController.show
      case 1 => pageOneController.show
      case 2 => pageTwoController.show
      case 3 => pageThreeController.show
      case 4 => pageFourController.show
      case 5 => pageFiveController.show
      case 6 => pageSixController.show
      case 7 => pageSevenController.show
      case 8 => pageEightController.show
      case 9 => pageNineController.show
      case 10 => pageTenController.show
      case 11 => pageElevenController.show
      case 12 => pageTwelveController.show
      case 13 => pageThirteenController.show
      case 14 => pageFourteenController.show
      case _ => application.index
    }


  def savePage(pageNumber: Int): Action[AnyContent] =
    pageNumber match {
      case 0 => pageZeroController.save
      case 1 => pageOneController.save
      case 2 => pageTwoController.save
      case 3 => pageThreeController.save
      case 4 => pageFourController.save
      case 5 => pageFiveController.save
      case 6 => pageSixController.save
      case 7 => pageSevenController.save
      case 8 => pageEightController.save
      case 9 => pageNineController.save
      case 10 => pageTenController.save
      case 11 => pageElevenController.save
      case 12 => pageTwelveController.save
      case 13 => pageThirteenController.save
      case 14 => pageFourteenController.save
      case _ => application.index
    }
}
