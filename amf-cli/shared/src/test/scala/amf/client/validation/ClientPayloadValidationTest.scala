package amf.client.validation

import amf.cli.internal.convert.NativeOps
import amf.core.client.common.validation.StrictValidationMode
import amf.core.client.platform.model.DataTypes
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.platform.ShapesConfiguration
import amf.shapes.client.platform.model.domain.ScalarShape
import org.scalatest.matchers.should.Matchers
import amf.shapes.client.platform.model.domain.{AnyShape, NodeShape}
import amf.core.client.platform.model.domain.PropertyShape

trait ClientPayloadValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with NativeOps with Matchers {

  test("Test object payload in string shape") {
    val test    = new ScalarShape().withDataType(DataTypes.String)
    val payload = """{"key1": 1, "text": "b"}""".trim
    val report  = payloadValidator(test, `application/json`).syncValidate(payload)
    report.conforms shouldBe false
  }

  test("Test duplicated key payload in object shape") {
    val key1 = new PropertyShape().withName("key1").withRange(new ScalarShape().withDataType(DataTypes.Number))
    val text = new PropertyShape().withName("text").withRange(new ScalarShape().withDataType(DataTypes.String))
    val test = new NodeShape()
    test._internal.withProperties(Seq(key1._internal, text._internal))
    val payload = """{"key1": 1, "key1": 1, "text": "b"}""".trim
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

  private def payloadValidator(shape: AnyShape, mediaType: String) = {
    ShapesConfiguration.predefined().elementClient().payloadValidatorFor(shape, mediaType, StrictValidationMode)
  }
}
