package amf.error

class AsyncParserErrorTest extends ParserErrorTest {

  override protected val basePath: String = "file://amf-cli/shared/src/test/resources/parser-results/async"

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
