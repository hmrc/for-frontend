/*
 * Copyright 2020 HM Revenue & Customs
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

import org.scalatest.{ FlatSpec, Matchers }
import utils.FormBindingTestAssertions._
import org.joda.time.DateTime

class LoginMappingSpec extends FlatSpec with Matchers {
	val loginForm = controllers.LoginController.loginForm

	behavior of "Login Mapping"

	it should "bind to ISO date time strings for the start-time" in {
		val data = Map(
			"referenceNumber" -> "12345678 /*BLAH 000",
			"postcode" -> "AA11 1AA",
			"start-time" -> "2016-01-04T08:58:42.113Z"
		)

		mustBind(loginForm.bind(data)) { x =>
			assert(x.startTime === new DateTime(2016, 1, 4, 8, 58, 42, 113))
		}
	}

	it should "after trimming non numeric chars and making postcode UPPERCASE should be 10 or 11 digits in reference numbers" in {
		val data = Map(
			"referenceNumber" -> "12345678  */ 000",
			"postcode" -> "aa11 1aa",
			"start-time" -> "2016-01-04T08:58:42.113Z"
		)

    mustBind(loginForm.bind(data)) { x => assert(x.referenceNumber === "12345678  */ 000") }

		val d1 = data.updated("referenceNumber", "123456789")
		mustContainError("referenceNumber", Errors.invalidRefNum, loginForm.bind(d1))

		val d2 = data.updated("referenceNumber", "123456789012")
		mustContainError("referenceNumber", Errors.invalidRefNum, loginForm.bind(d2))

		val d3 = data.updated("referenceNumber", "12345678901")
		mustBind(loginForm.bind(d3)){x => assert(x.referenceNumber === "12345678901")}

		val d4 = data.updated("referenceNumber", "1234567890")
		mustNotContainErrorFor("referenceNumber", loginForm.bind(d4))
	}

	it should "postcode works in upper or lowercase" in {
		val data = Map(
			"referenceNumber" -> "12345678 000",
			"postcode" -> "AA11 1AA",
			"start-time" -> "2016-01-04T08:58:42.113Z"
		)

		mustBind(loginForm.bind(data)) { x => assert(x.postcode === "AA11 1AA") }

		val d4 = data.updated("postcode", "aa11 1aa")
		mustNotContainErrorFor("postcode", loginForm.bind(d4))
	}

}
