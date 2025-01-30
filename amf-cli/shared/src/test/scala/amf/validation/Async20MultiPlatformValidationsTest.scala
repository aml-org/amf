package amf.validation

import org.scalatest.matchers.should.Matchers

class Async20MultiPlatformValidationsTest extends MultiPlatformReportGenTest with Matchers {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/async20/validations/"
  val avroTCKPath: String          = "file://amf-cli/shared/src/test/resources/avro/tck/apis/valid/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/async20/"

  test("Validate avro payload in message") {
    validate("avro-map-values-union.yaml", Some("avro-map-values-union.report"), avroTCKPath)
  }

  test("Draft 7 - conditional sub schemas validations") {
    validate("draft-7-validations.yaml", Some("draft-7-validations.report"))
  }

  test("invalid examples defined in variables of server") {
    validate("invalid-server-variable-examples.yaml", Some("invalid-server-variable-examples.report"))
  }

  test("Validate message payload examples") {
    validate("message-payload-invalid-example.yaml", Some("invalid-message-examples.report"))
  }

  test("Validate example defined in message trait") {
    validate("applied-message-trait-invalid-example.yaml", Some("invalid-example-applied-trait.report"))
  }

  test("Validate avro payload against an invalid example defined in message examples") {
    validate(
      "async-avro-payload-validation/invalid-payload-example.yaml",
      Some("async-avro-payload-validation/invalid-payload-example.report")
    )
  }

  test("Validate avro payload against a valid example defined in message examples") {
    validate(
      "async-avro-payload-validation/valid-payload-example.yaml",
      Some("async-avro-payload-validation/valid-payload-example.report")
    )
  }

  test("Validate avro payload (as ref) against an invalid example defined in message examples") {
    validate(
      "async-avro-payload-validation/invalid-payload-example-refs.yaml",
      Some("async-avro-payload-validation/invalid-payload-example-refs.report")
    )
  }

  test("Validate avro payload (as ref) against a valid example defined in message examples") {
    validate(
      "async-avro-payload-validation/valid-payload-example-refs.yaml",
      Some("async-avro-payload-validation/valid-payload-example-refs.report")
    )
  }
}
