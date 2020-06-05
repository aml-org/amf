package amf.error

import amf.core.model.document.BaseUnit
import amf.core.parser.Range
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.remote.AsyncYamlHint
import amf.facades.AMFCompiler

import scala.concurrent.Future

class AsyncParserErrorTest extends ParserErrorTest {

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/async"

  override protected def build(eh: ParserErrorHandler, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, AsyncYamlHint, eh = eh).build()

  test("Parameter is not YMap") {
    validate(
      "/error/invalid-single-parameter-parse.yaml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(Range((8, 14), (8, 27))))
      }
    )
  }

  test("Empty parameters") {
    validate(
      "/error/invalid-empty-parameters.yaml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(Range((7, 15), (7, 15))))
      }
    )
  }
}
