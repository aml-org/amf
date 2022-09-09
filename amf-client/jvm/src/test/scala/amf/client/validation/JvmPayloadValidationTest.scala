package amf.client.validation

import amf.client.environment.Environment
import amf.client.model.DataTypes
import amf.client.model.domain.{NodeShape, PropertyShape, ScalarShape}
import amf.convert.NativeOpsFromJvm
import amf.core.AMF
import amf.core.remote.Mimes
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

  test("Payload with max depth that exceeds the one in environment should not conform") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val dummyShape     = new NodeShape()
      val maxDepth = 50
      val env = Environment.empty().setMaxYamlDepth(maxDepth)
      val validator = dummyShape.payloadValidator(Mimes.`APPLICATION/JSON`, env).asOption.get
      val payload =
        """{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{
          |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
          |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
          |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
          |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
          |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
          |{{{{{[{{{{{{""".stripMargin

      payload.count(char => char == '{' || char == '[') shouldBe > (50)

      val report = validator.syncValidate(Mimes.`APPLICATION/JSON`, payload)
      report._internal.results.head.message should include(s"Reached maximum nesting value of $maxDepth in JSON")
      report.conforms shouldBe false
    }
  }

  test("Big payload with nested depth accumulation of more than 7 but max accumulation of less than 7 should be valid") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val dummyShape     = new NodeShape()
      val maxDepth = 7
      val env = Environment.empty().setMaxYamlDepth(maxDepth)
      val validator = dummyShape.payloadValidator(Mimes.`APPLICATION/JSON`, env).asOption.get
      val payload =
        """{
          | "items": [
          |   {
          |     "a": {
          |       "b": {
          |         "c": {
          |         }
          |       }
          |     }
          |   },
          |   {
          |     "a": {
          |       "b": {
          |         "c": {
          |         }
          |       }
          |     }
          |   }
          | ]
          |}""".stripMargin
      val report = validator.syncValidate(Mimes.`APPLICATION/JSON`, payload)
      println(report)
      report.conforms shouldBe true
    }
  }
}
