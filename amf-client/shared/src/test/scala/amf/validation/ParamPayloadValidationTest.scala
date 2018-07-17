package amf.validation

import amf.common.Diff
import amf.common.Diff.makeString
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Shape
import amf.core.remote.{Hint, OasJsonHint}
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.Matchers

import scala.concurrent.Future

class ParamPayloadValidationTest extends ApiShapePayloadValidationTest {

  val api = "file://amf-client/shared/src/test/resources/validations/param-payload/oas_data.json"

  val fixtureList: Seq[Fixture] = Seq(
    Fixture("param validation", "2015-07-20T21:00:00", conforms = true),
    Fixture("param validation quoted number in string", "\"2\"", conforms = true),
    Fixture("param validation quoted boolean in string", "\"true\"", conforms = true),
    Fixture("param validation boolean in string", "true", conforms = true),
    Fixture("param validation number against string", "2", conforms = true)
  )

  fixtureList.foreach { f =>
    forApi(api).map { u =>
      test(f.name) {
        validate(u, f.payload).map { report =>
          assert(report.conforms == f.conforms)
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

  override protected def findShape(d: Document): Shape =
    d.encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head
      .request
      .headers
      .head
      .schema

  override protected val hint: Hint = OasJsonHint
}

trait ApiShapePayloadValidationTest extends FileAssertionTest with Matchers {

  protected case class Fixture(name: String, payload: String, conforms: Boolean)

  protected val hint: Hint
  protected def forApi(api: String): Future[BaseUnit] = {
    Validation(platform).flatMap(
      AMFCompiler(api, platform, hint, _)
        .build())
  }
  protected def validate(u: BaseUnit, payload: String): Future[AMFValidationReport] =
    PayloadValidator.validate(findShape(u.asInstanceOf[Document]), payload, SeverityLevels.VIOLATION)

  protected def findShape(d: Document): Shape
}
