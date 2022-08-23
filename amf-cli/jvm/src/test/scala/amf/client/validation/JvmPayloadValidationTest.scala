package amf.client.validation

import amf.cli.internal.convert.NativeOpsFromJvm
import amf.core.client.platform.model.DataTypes
import amf.core.internal.remote.Mimes._
import amf.shapes.client.platform.model.domain.{ArrayShape, NodeShape, ScalarShape}

class JvmPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {

  test("Test unexpected type error") {
    val test   = new ScalarShape().withDataType(DataTypes.String)
    val report = payloadValidator(test, `application/json`).syncValidate("1234")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
  }

  // regex pending analysis (APIMF-3058) jvm cannot process regex, on the other hand it is valid for js
  test("Avoid exception for pattern regex that cannot be parsed") {
    val shape = new ScalarShape()
      .withDataType(DataTypes.String)
      .withPattern(
        "^(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
      )
    val validator = payloadValidator(shape, `application/json`)
    val report    = validator.syncValidate(""""irrelevant text"""")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "Regex defined in schema could not be processed"
  }

  test("Validation against a number with multipleOf 0 should throw violation") {
    val shape                = new ScalarShape().withDataType(DataTypes.Number).withMultipleOf(0)
    val validator            = payloadValidator(shape, `application/json`)
    val positiveNumberReport = validator.syncValidate("5")
    val zeroReport           = validator.syncValidate("0")
    positiveNumberReport.results.asSeq.head.message shouldBe "Can't divide by 0"
    zeroReport.results.asSeq.head.message shouldBe "Can't divide by 0"
  }

  test("Validation order shouldn't affect conformance of each payload") {
    val itemsShape = new NodeShape()
    val shape      = new ArrayShape().withItems(itemsShape)
    val validator  = payloadValidator(shape, `application/yaml`)

    val validPayload = "- {}"
    // an error in multiple lines should not break the next validation
    val invalidPayload = "- {}\n - {}"

    val validReport1  = validator.syncValidate(validPayload)
    val invalidReport = validator.syncValidate(invalidPayload)
    val validReport2  = validator.syncValidate(validPayload)

    validReport1.results.asSeq.size shouldBe 0
    invalidReport.results.asSeq.size shouldBe 1
    validReport2.results.asSeq.size shouldBe 0 // this line should not fail
  }
}
