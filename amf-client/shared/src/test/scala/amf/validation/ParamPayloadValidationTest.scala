package amf.validation

import amf.common.Diff
import amf.common.Diff.makeString
import amf.core.annotations.LexicalInformation
import amf.core.model.document.Document
import amf.core.parser
import amf.core.remote.OasJsonHint
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.Matchers

import scala.concurrent.Future

class ParamPayloadValidationTest extends FileAssertionTest with Matchers {

  val api = "file://amf-client/shared/src/test/resources/validations/param-payload/oas_data.json"

  private def validate(payload: String): Future[AMFValidationReport] = {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(api, platform, OasJsonHint, validation)
        .build()
      result <- {
        val shape = model
          .asInstanceOf[Document]
          .encodes
          .asInstanceOf[WebApi]
          .endPoints
          .head
          .operations
          .head
          .request
          .headers
          .head
          .schema
        PayloadValidator.validate(shape, payload, SeverityLevels.VIOLATION)
      }
    } yield {
      result
    }
  }

  sealed case class Fixture(name: String, payload: String, result: Option[AMFValidationResult])

  val fixtureList: Seq[Fixture] = Seq(
    Fixture("param validation", "2015-07-20T21:00:00", None),
    Fixture("param validation number in string", "\"2\"", None),
    Fixture("param validation boolean in string", "\"true\"", None),
    Fixture("param validation number error", "2", None)
  )

  fixtureList.foreach { f =>
    test(f.name) {
      validate(f.payload).map { report =>
        f.result match {
          case None if !report.conforms =>
            fail("report not conforms")
          case None if report.results.nonEmpty => fail("report has results (probably warnings because conforms)")
          case None                            => succeed
          case Some(expected) if report.results.isEmpty =>
            fail(s"report it's empty while should be ${expected.toString}")
          case Some(expected) if report.results.size > 1 =>
            fail(s"report has more than one results while should be ${expected.toString}")
          case Some(expected) =>
            val actual = report.results.head
            expected.message should be(actual.message)
            expected.targetNode should be(actual.targetNode)
            expected.level should be(actual.level)
            expected.position match {
              case Some(p) if actual.position.isDefined => p.toString should be(actual.position.get.toString)
              case Some(p)                              => fail("Actual position $p while no expected position its defined")
              case None if actual.position.isDefined =>
                fail(s"Actual position is not defined while no expected position its ${actual.position.get.toString}")
              case _ => succeed
            }
        }
      }
    }
  }

  private def runDeltas(expected: String, actual: String) = {
    val diffs = Diff.ignoreAllSpace.diff(actual, expected)
    if (diffs.nonEmpty)
      fail(s"\ndiff: \n${makeString(diffs)}")
    succeed
  }

}
