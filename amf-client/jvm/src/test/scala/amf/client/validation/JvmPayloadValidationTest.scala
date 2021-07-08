package amf.client.validation

import amf.client.model.DataTypes
import amf.client.model.domain.ScalarShape
import amf.convert.NativeOpsFromJvm
import amf.core.AMF
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin

class JvmPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {
  test("Test unexpected type error") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String)

      val report = test
        .payloadValidator("application/json")
        .asOption
        .get
        .syncValidate("application/json", "1234")
      report.conforms shouldBe false
      report.results.asSeq.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
    }
  }

  // regex pending analysis (APIMF-3058) jvm cannot process regex, on the other hand it is valid for js
  test("Avoid exception for pattern regex that cannot be parsed") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val shape = new ScalarShape()
        .withDataType(DataTypes.String)
        .withPattern(
          "^(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
      val validator = shape.payloadValidator("application/json").asOption.get
      val report    = validator.syncValidate("application/json", """"irrelevant text"""")
      report.conforms shouldBe false
      report.results.asSeq.head.message shouldBe "Regex defined in schema could not be processed"
    }
  }

  test("Validation against a number with multipleOf 0 should throw violation") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val shape                = new ScalarShape().withDataType(DataTypes.Number).withMultipleOf(0)
      val validator            = shape.payloadValidator("application/json").asOption.get
      val positiveNumberReport = validator.syncValidate("application/json", "5")
      val zeroReport           = validator.syncValidate("application/json", "0")
      positiveNumberReport.results.asSeq.head.message shouldBe "Can't divide by 0"
      zeroReport.results.asSeq.head.message shouldBe "Can't divide by 0"
    }
  }
}
