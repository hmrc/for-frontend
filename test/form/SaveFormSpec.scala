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

package form

import connectors.{Document, Page}
import form.persistence.SaveFormInRepository
import models.RoughDate
import models.pages._
import models.serviceContracts.submissions._
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import util.DateUtil.nowInUK
import utils.stubs.StubFormDocumentRepo

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits._

class SaveFormSpec extends AnyWordSpec with should.Matchers {
  import TestData._

  "SaveForm" when {

    "no document exists" should {
      val sessionId = "asdkjfa078380898fdsa"
      val r         = StubFormDocumentRepo()
      val s         = new SaveFormInRepository(r, StubSummaryBuilder())
      val x         = Await.result(s(Some(testData), sessionId, refNumWithNoDoc, 1), 5 seconds)

      "not save anything" in
        assert(r.storedPages.isEmpty)

      "return none" in
        assert(x === None)
    }

    "a document exists" should {
      val sessionId = "sdlkjfaweiroiwe"
      val r         = StubFormDocumentRepo((sessionId, refNumWithDoc, testDoc))
      val b         = StubSummaryBuilder((testDocWithTestDataAddedToPage1, summaryForTestDocWithTestData))
      val s         = new SaveFormInRepository(r, b)
      val x         = Await.result(s(Some(testData), sessionId, refNumWithDoc, 1), 5 seconds)

      "add a new page to the document with the supplied fields as a flat Json object" in
        assert(r.storedPages.last._3 === page1WithTestData)

      "return the trimmed fields that were saved and the updated summary" in {
        assert(x.get._1 === testDataTrimmed - "csrfToken")
        assert(x.get._2 === summaryForTestDocWithTestData)
      }
    }

    "the form data contains empty entries supplied because JavaScript is not enabled" should {
      val sessionId = "asfd937r2839ra3e7sd"
      val r         = StubFormDocumentRepo((sessionId, emptyFieldsRefNum, emptyFieldsTestDoc))
      val b         = StubSummaryBuilder((emptyFieldsTestDocWithNonEmptyFieldsAddedToPage6, summaryWithNonEmptyFieldsAddedToPage6))
      val s         = new SaveFormInRepository(r, b)
      val x         = Await.result(s(Some(page6TestData), sessionId, emptyFieldsRefNum, 6), 5 seconds)

      "add the new page to the document as usual, but will throw away the empty fields" in {
        assert(r.storedPages.last._1 === sessionId)
        assert(r.storedPages.last._2 === emptyFieldsRefNum)
        assert(r.storedPages.last._3 === page6WithNonEmptyTestDataFields)
      }

      "return only the fields that were saved and the empty summary" in {
        assert(x.get._1 === page6NonEmptyTestData)
        assert(x.get._2 === summaryWithNonEmptyFieldsAddedToPage6)
      }
    }
  }

  object TestData {
    val refNumWithNoDoc   = "4545456744"
    val refNumWithDoc     = "1111111222"
    val testDoc: Document = Document(refNumWithDoc, nowInUK, Seq.empty)

    val testData: Map[String, Seq[String]] = Map(
      "isAddressCorrect"           -> Seq(" false "),
      "address.buildingNameNumber" -> Seq(" 123 "),
      "address.postcode"           -> Seq(" AA11 1AA "),
      "csrfToken"                  -> Seq(" adfjalskdfjs ")
    )

    val testDataTrimmed: Map[String, Seq[String]] = Map(
      "isAddressCorrect"           -> Seq("false"),
      "address.buildingNameNumber" -> Seq("123"),
      "address.postcode"           -> Seq("AA11 1AA"),
      "csrfToken"                  -> Seq("adfjalskdfjs")
    )
    val page1WithTestData: Page                   = Page(1, testDataTrimmed - "csrfToken")
    val testDocWithTestDataAddedToPage1: Document = testDoc.add(page1WithTestData)

    val summaryForTestDocWithTestData: Summary = Summary(
      refNumWithDoc,
      nowInUK,
      Some(AddressConnectionTypeYesChangeAddress),
      Some(Address("123", None, None, "AA11 1AA")),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None
    )

    val page6NonEmptyTestData: Map[String, Seq[String]] = Map(
      "leaseAgreementType"                                -> Seq("leaseTenancy"),
      "writtenAgreement.leaseAgreementHasBreakClause"     -> Seq("true"),
      "writtenAgreement.breakClauseDetails"               -> Seq("adjf asdklfj a;sdljfa dsflk"),
      "writtenAgreement.agreementIsStepped"               -> Seq("true"),
      "writtenAgreement.steppedDetails[0].stepTo.day"     -> Seq("9"),
      "writtenAgreement.steppedDetails[0].stepTo.month"   -> Seq("12"),
      "writtenAgreement.steppedDetails[0].stepTo.year"    -> Seq("2011"),
      "writtenAgreement.steppedDetails[0].stepFrom.day"   -> Seq("8"),
      "writtenAgreement.steppedDetails[0].stepFrom.month" -> Seq("11"),
      "writtenAgreement.steppedDetails[0].stepFrom.year"  -> Seq("2010"),
      "writtenAgreement.steppedDetails[0].amount"         -> Seq("500"),
      "writtenAgreement.startDate.month"                  -> Seq("3"),
      "writtenAgreement.startDate.year"                   -> Seq("2011"),
      "writtenAgreement.rentOpenEnded"                    -> Seq("false"),
      "writtenAgreement.leaseLength.years"                -> Seq("10"),
      "writtenAgreement.leaseLength.months"               -> Seq("2")
    )

    val page6EmptyTestData: Map[String, Seq[String]]               = Map(
      "writtenAgreement.steppedDetails[1].stepTo.day"     -> Seq(""),
      "writtenAgreement.steppedDetails[1].stepTo.month"   -> Seq(""),
      "writtenAgreement.steppedDetails[1].stepTo.year"    -> Seq(""),
      "writtenAgreement.steppedDetails[1].stepFrom.day"   -> Seq(""),
      "writtenAgreement.steppedDetails[1].stepFrom.month" -> Seq(""),
      "writtenAgreement.steppedDetails[1].stepFrom.year"  -> Seq(""),
      "writtenAgreement.steppedDetails[1].amount"         -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepTo.day"     -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepTo.month"   -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepTo.year"    -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepFrom.day"   -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepFrom.month" -> Seq(""),
      "writtenAgreement.steppedDetails[2].stepFrom.year"  -> Seq(""),
      "writtenAgreement.steppedDetails[2].amount"         -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepTo.day"     -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepTo.month"   -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepTo.year"    -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepFrom.day"   -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepFrom.month" -> Seq(""),
      "writtenAgreement.steppedDetails[3].stepFrom.year"  -> Seq(""),
      "writtenAgreement.steppedDetails[3].amount"         -> Seq("")
    )
    val page6TestData: Map[String, Seq[String]]                    = page6NonEmptyTestData ++ page6EmptyTestData
    val emptyFieldsRefNum                                          = "4455667898"
    val emptyFieldsTestDoc: Document                               = Document(emptyFieldsRefNum, nowInUK, Seq(page1WithTestData))
    val page6WithNonEmptyTestDataFields: Page                      = Page(6, page6NonEmptyTestData)
    val emptyFieldsTestDocWithNonEmptyFieldsAddedToPage6: Document = emptyFieldsTestDoc.add(page6WithNonEmptyTestDataFields)

    val mappedPage6WithNonEmptyFields: PageSix = PageSix(
      LeaseAgreementTypesLeaseTenancy,
      Some(WrittenAgreement(
        RoughDate(None, Some(3), 2011),
        false,
        Some(MonthsYearDuration(2, 10)),
        true,
        Some("adjf asdklfj a;sdljfa dsflk"),
        true,
        List(SteppedDetails(
          LocalDate.of(2011, 12, 9),
          LocalDate.of(2010, 11, 8),
          500
        ))
      )),
      VerbalAgreement()
    )

    val summaryWithNonEmptyFieldsAddedToPage6: Summary = Summary(
      "",
      nowInUK,
      Some(AddressConnectionTypeYesChangeAddress),
      Some(Address("123", None, None, "AA11 1AA")),
      None,
      None,
      None,
      None,
      Some(mappedPage6WithNonEmptyFields),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None
    )
  }
}

case class StubSummaryBuilder(summaries: (Document, Summary)*) extends SummaryBuilder {
  val emptySummary: Summary = Summary("", nowInUK, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)

  override def build(doc: Document): Summary = summaries.find(_._1 == doc).map(_._2).getOrElse(emptySummary)
}
