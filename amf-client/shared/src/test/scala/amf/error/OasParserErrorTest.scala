package amf.error
import amf.core.model.document.BaseUnit
import amf.core.remote.OasYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.core.parser.Range

import scala.concurrent.Future

class OasParserErrorTest extends ParserErrorTest {

  test("Empty type ref") {
    validate(
      "/error/empty-type-ref.yaml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Unexpected $ref with $ref: ")
        violation.position.map(_.range) should be(Some(Range((13, 9), (13, 9))))
      },
      refViolation => {
        refViolation.level should be("Violation")
        refViolation.message should be("Error parsing shape at NewSchema")
        refViolation.position.map(_.range) should be(Some(Range((12, 2), (13, 9))))
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/oas"

  override protected def build(validation: Validation, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, OasYamlHint, validation).build()
}
