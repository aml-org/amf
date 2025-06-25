package amf.client.validation

import amf.cli.internal.convert.NativeOps
import amf.core.client.common.validation.StrictValidationMode
import amf.core.client.platform.model.DataTypes
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.platform.ShapesConfiguration
import amf.shapes.client.platform.model.domain.{AnyShape, ScalarShape}
import org.scalatest.matchers.should.Matchers

trait ClientPayloadValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with NativeOps with Matchers {

  test("Test object payload in string shape") {
    val test    = new ScalarShape().withDataType(DataTypes.String)
    val payload = """{"key1": 1, "text": "b"}""".trim
    val report  = payloadValidator(test, `application/json`).syncValidate(payload)
    report.conforms shouldBe false
  }

  test("Test unexpected type error") {
    val test   = new ScalarShape().withDataType(DataTypes.String)
    val report = payloadValidator(test, `application/json`).syncValidate("1234")
    report.conforms shouldBe false
  }

  test("Test unexpected type error async") {
    val test = new ScalarShape().withDataType(DataTypes.String)
    for {
      report <- payloadValidator(test, `application/json`).validate("1234").asFuture
    } yield {
      report.conforms shouldBe false
    }
  }

  test("Test valid scalar async") {
    val test = new ScalarShape().withDataType(DataTypes.String)
    for {
      report <- payloadValidator(test, `application/json`).validate("\"1234\"").asFuture
    } yield {
      report.conforms shouldBe true
    }
  }

  def payloadValidator(shape: AnyShape, mediaType: String): AMFShapePayloadValidator = {
    ShapesConfiguration.predefined().elementClient().payloadValidatorFor(shape, mediaType, StrictValidationMode)
  }
}
