package aat.agentApi

import aat.AcceptanceTest
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

class PUTtingSubmissionJson extends AcceptanceTest {
  override val http: WSGet with WSPut with WSPost with WSDelete = _
}

object TestData {
  val validCredentials = ("9999000", "123", "AA11+1AA")
  val invalidCredentials = ("1234567", "890", "AA11+1AA")
}
