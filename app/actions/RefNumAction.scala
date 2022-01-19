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

package actions

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class RefNumRequest[A](val refNum: String, request: Request[A], messagesApi: MessagesApi) extends MessagesRequest[A](request, messagesApi)


class RefNumAction @Inject() (bodyParser: BodyParsers.Default, messagesApi: MessagesApi)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RefNumRequest, AnyContent] with ActionRefiner[Request, RefNumRequest] with MessagesActionBuilder {

  override def refine[A](request: Request[A]): Future[Either[Result, RefNumRequest[A]]] = Future.successful {

    request.session.get("refNum") match {
      case Some(refNum) => Right(new RefNumRequest(refNum, request, messagesApi))
      case None => Left(Redirect(controllers.routes.LoginController.show))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser
}
