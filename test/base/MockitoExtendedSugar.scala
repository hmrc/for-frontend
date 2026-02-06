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

package base

import org.mockito.stubbing.{OngoingStubbing, Stubber}
import org.mockito.verification.VerificationMode
import org.mockito.{ArgumentMatcher, ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar

/**
  * @author Yuriy Tumakha
  */
trait MockitoExtendedSugar extends MockitoSugar {

  // ArgumentMatchers

  def eqTo[T](value: T): T = ArgumentMatchers.eq(value)

  def any[T]: T = ArgumentMatchers.any

  def anyString: String = ArgumentMatchers.anyString

  def anyInt: Int = ArgumentMatchers.anyInt

  def anyBoolean: Boolean = ArgumentMatchers.anyBoolean

  def anyList[T]: List[T] = ArgumentMatchers.any[List[T]]()

  def anySeq[T]: Seq[T] = ArgumentMatchers.any[Seq[T]]()

  def anyMap[K, V]: Map[K, V] = ArgumentMatchers.any[Map[K, V]]()

  def startsWith(prefix: String): String = ArgumentMatchers.startsWith(prefix)

  def argThat[T](matcher: ArgumentMatcher[T]): T = ArgumentMatchers.argThat(matcher)

  // Mockito API

  def when[T](methodCall: T): OngoingStubbing[T] = Mockito.when(methodCall)

  def doNothing(): Stubber = Mockito.doNothing()

  def reset(mocks: AnyRef*): Unit = Mockito.reset(mocks*)

  def verify[T](mock: T): T = Mockito.verify(mock)

  def verify[T](mock: T, mode: VerificationMode): T = Mockito.verify(mock, mode)

  def verifyNoInteractions(mocks: AnyRef*): Unit = Mockito.verifyNoInteractions(mocks*)

  def verifyNoMoreInteractions(mocks: AnyRef*): Unit = Mockito.verifyNoMoreInteractions(mocks*)

  def atLeastOnce: VerificationMode = Mockito.atLeastOnce()

  def atMost(maxNumberOfInvocations: Int): VerificationMode = Mockito.atMost(maxNumberOfInvocations)

  def atLeast(minNumberOfInvocations: Int): VerificationMode = Mockito.atLeast(minNumberOfInvocations)

  def never: VerificationMode = Mockito.never()

  def only: VerificationMode = Mockito.only()

  def times(wantedNumberOfInvocations: Int): VerificationMode = Mockito.times(wantedNumberOfInvocations)

  def calls(wantedNumberOfInvocations: Int): VerificationMode = Mockito.calls(wantedNumberOfInvocations)

}
