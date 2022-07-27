package amf.error

import org.mulesoft.common.client.lexical.PositionRange

class AsyncParserErrorTest extends ParserErrorTest {

  override protected val basePath: String = "file://amf-cli/shared/src/test/resources/parser-results/async"

  test("Parameter is not YMap") {
    validate(
      "/error/invalid-single-parameter-parse.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(PositionRange((8, 14), (8, 27))))
      }
    )
  }

  test("Empty parameters") {
    validate(
      "/error/invalid-empty-parameters.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("YAML map expected")
        violation.position.map(_.range) should be(Some(PositionRange((7, 15), (7, 15))))
      }
    )
  }
}
