package amf.error

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.Range
import amf.core.remote.Async20YamlHint
import amf.facades.AMFCompiler

import scala.concurrent.Future

class AsyncParserErrorTest extends ParserErrorTest {

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/async"

  test("Parameter is not YMap") {
    validate(
      "/error/invalid-single-parameter-parse.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(Range((8, 14), (8, 27))))
      }
    )
  }

  test("Empty parameters") {
    validate(
      "/error/invalid-empty-parameters.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(Range((7, 15), (7, 15))))
      }
    )
  }
}
