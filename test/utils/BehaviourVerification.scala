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

package utils

import org.scalatest.matchers.should
import security.ArgumentsDidNotMatch
import testutils._

import scala.concurrent.Future

trait BehaviourVerification extends should.Matchers {
  def respondWith[A, B](a: A)(b: B): A => Future[B] = aa => {
    if (a == aa) Future.successful(b) else throw ArgumentsDidNotMatch(Seq(a), Seq(aa))
  }

  def respondWith[A,B,C](a: A, b: B)(c: C): (A,B) => Future[C] = (aa, bb) => {
    if (aa == a && bb == b) Future.successful(c) else throw ArgumentsDidNotMatch(Seq(a, b), Seq(aa, bb))
  }

  def respondWith[A,B,C,D](a: A, b: B, c: C)(d: D): (A,B,C) => Future[D] = (aa, bb, cc) => {
    if (aa == a && bb == b && cc == c) Future.successful(d) else throw ArgumentsDidNotMatch(Seq(a, b, c), Seq(aa, bb, cc))
  }

  def expect[A](a: A): A => Future[Unit] =
    x => Future.successful(assert(x === a))

  def set[A,R](x: A => R): A => Future[R] =
    a => Future.successful(x(a))

  def set[A,B,R](x: ((A,B)) => R): (A,B) => Future[R] =
    (a, b) => Future.successful(x((a, b)))

  def set[A,B,C,R](x: ((A,B,C)) => R): (A,B,C) => Future[R] =
    (a, b, c) => Future.successful(x((a, b, c)))

  def none[A,B,C]: (A,B) => Future[Option[C]] =
    (_, _) => None
}
