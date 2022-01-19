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

package form


import models.serviceContracts.submissions.PreviouslyConnected
import play.api.data.Form
import play.api.data.Forms._

object PreviouslyConnectedForm {

  private val previouslyConnectedMandatoryBoolean = optional(boolean)
    .verifying("previously-connected.mandatory", _.isDefined)
    .transform({ s: Option[Boolean] => s.get }, { v: Boolean => Some(v) })

  private val fieldMapping = mapping (
    "haveYouBeenConnected" -> previouslyConnectedMandatoryBoolean
  )(PreviouslyConnected.apply)(PreviouslyConnected.unapply)

  val formMapping = Form(fieldMapping)

}
