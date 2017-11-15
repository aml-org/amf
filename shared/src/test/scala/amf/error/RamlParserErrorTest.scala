package amf.error

import amf.compiler.AMFCompiler
import amf.parser.Range
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.{AMFValidationResult, Validation}
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSuite, Succeeded}

import scala.concurrent.ExecutionContext

class RamlParserErrorTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://shared/src/test/resources/error/"

  test("Test unexpected node types") {
    validate(
      "unexpected-nodes.raml",
      title => {
        title.level should be("Violation")
        title.message should be("Expecting !!str and !!map provided")
        title.position.map(_.range) should be(Some(Range((2, 6), (4, 0))))
      },
      description => {
        description.level should be("Violation")
        description.message should be("Expecting !!str and !!seq provided")
        description.position.map(_.range) should be(Some(Range((4, 13), (4, 24))))
      },
      protocols => {
        protocols.level should be("Violation")
        protocols.message should be("WebAPI 'protocols' property must be a scalar or sequence value")
        protocols.position.map(_.range) should be(Some(Range((5, 10), (7, 0))))
      },
      securedBy => {
        securedBy.level should be("Violation")
        securedBy.message should be("Not a YSequence")
        securedBy.position.map(_.range) should be(Some(Range((7, 11), (7, 16))))
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    val validation = Validation(platform)
    AMFCompiler(basePath + file, platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        val report = validation.aggregatedReport
        report.size should be(fixture.size)
        fixture.zip(report).foreach {
          case (fn, result) => fn(result)
        }
        Succeeded
      }
  }
}
