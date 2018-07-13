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

  sealed case class Fixture(name: String, payload: String, conforms: Boolean)

  val fixtureList: Seq[Fixture] = Seq(
    Fixture("param validation", "2015-07-20T21:00:00", conforms = true),
    Fixture("param validation number in string", "\"2\"", conforms = true),
    Fixture("param validation boolean in string", "\"true\"", conforms = true),
    Fixture("param validation number error", "2", conforms = false)
  )

  fixtureList.foreach { f =>
    test(f.name) {
      validate(f.payload).map { report =>
        assert(report.conforms == f.conforms)
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
