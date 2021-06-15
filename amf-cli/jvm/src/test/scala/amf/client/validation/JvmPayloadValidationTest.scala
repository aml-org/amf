package amf.client.validation

import amf.client.model.domain.ScalarShape
import amf.convert.NativeOpsFromJvm
import amf.core.client.platform.model.DataTypes

class JvmPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {

  test("Test unexpected type error") {

    val test = new ScalarShape().withDataType(DataTypes.String)

    val report = payloadValidator(test, APPLICATION_JSON).syncValidate("1234")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
  }

  // regex pending analysis (APIMF-3058) jvm cannot process regex, on the other hand it is valid for js
  test("Avoid exception for pattern regex that cannot be parsed") {
    val shape = new ScalarShape()
      .withDataType(DataTypes.String)
      .withPattern(
        "^(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
    val validator = payloadValidator(shape, APPLICATION_JSON)
    val report    = validator.syncValidate(""""irrelevant text"""")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "Regex defined in schema could not be processed"
  }
}
