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

package useCases

import uk.gov.hmrc.vo.unit.test.BaseSpec

import scala.collection.StringOps

class Generate7LengthLowercaseAlphaNumPasswordSpec extends BaseSpec:

  private def isLowercaseLetter(c: Char) = c.toString.matches("[a-z]")

  private def isNonAmbiguousDigit(c: Char) = !Seq('0', '1').contains(c)

  private def isNonAmbiguousLowercaseLetter(c: Char) = !Seq('i', 'l', 'o').contains(c)

  private def isAllowed(c: Char) = (c.isDigit || isLowercaseLetter(c)) && (isNonAmbiguousDigit(c) || isNonAmbiguousLowercaseLetter(c))

  "Generate7LengthLowercaseAlphaNumPassword" should {
    "generate a password consisting of unambiguous lowercase chars and numbers with a length of 7" in {
      (1 to 100) foreach { _ =>
        val pw = Generate7LengthLowercaseAlphaNumPassword()
        pw.length shouldBe 7

        for (c <- StringOps(pw))
          assert(isAllowed(c) === true, s"$c is not a valid character for passwords")
      }
    }
  }
