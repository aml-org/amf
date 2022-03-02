package amf.error

import org.mulesoft.common.client.lexical.PositionRange

class OasParserErrorTest extends ParserErrorTest {

  test("Empty type ref") {
    validate(
      "/error/empty-type-ref.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Unexpected $ref with $ref: ")
        violation.position.map(_.range) should be(Some(PositionRange((14, 9), (14, 9))))
      }
    )
  }

  test("Invalid parameter binding") {
    validate(
      "/error/invalid-parameter-binding.json",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Invalid parameter binding 'bo'")
        violation.position.map(_.range) should be(Some(PositionRange((13, 18), (13, 22))))
      },
      refViolation => {
        refViolation.severityLevel should be("Violation")
        refViolation.message should be("Invalid parameter binding 'qu'")
        refViolation.position.map(_.range) should be(Some(PositionRange((25, 18), (25, 22))))
      }
    )
  }

  test("Invalid body parameter count") {
    validate(
      "/error/invalid-body-parameter.json",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Cannot declare more than one 'body' parameter for a request or a resource")
        violation.position.map(_.range) should be(Some(PositionRange((22, 13), (33, 11))))
      }
    )
  }

  test("Ignored pattern property") {
    validate(
      "/warning/ignored-pattern-property.json",
      warning => {
        warning.severityLevel should be("Warning")
        warning.message should be(
          "Pattern property may be ignored if format 'byte' already defines a standard pattern")
        warning.position.map(_.range) should be(Some(PositionRange((12, 6), (12, 30))))
      }
    )
  }

  override protected val basePath: String = "file://amf-cli/shared/src/test/resources/parser-results/oas"
}
