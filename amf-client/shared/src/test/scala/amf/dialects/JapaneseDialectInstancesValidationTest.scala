package amf.dialects

import scala.concurrent.ExecutionContext

class JapaneseDialectInstancesValidationTest extends DialectInstanceValidation {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-client/shared/src/test/resources/vocabularies2/japanese/instances/"

  test("validate mixed instance 1") {
    validate("mixed-dialect-1.raml", "mixed-example-1.raml", 0)
  }

  test("validate mixed instance 2") {
    validate("mixed-dialect-2.raml", "mixed-example-2.raml", 0)
  }

  test("valid pattern") {
    validate("pattern-dialect.raml", "pattern-valid.raml", 0)
  }

  test("invalid pattern") {
    validate("pattern-dialect.raml", "pattern-invalid.raml", 1)
  }

  ignore("valid in") {
    validate("in-dialect.raml", "in-valid.raml", 0)
  }

  ignore("invalid in") {
    validate("in-dialect.raml", "in-invalid.raml", 1)
  }
}
