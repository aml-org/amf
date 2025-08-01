package amf.client.validation

import amf.cli.internal.convert.NativeOpsFromJvm
import amf.core.client.platform.model.DataTypes
import amf.core.client.platform.model.domain.PropertyShape
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.platform.model.domain.{NodeShape, ScalarShape}

class JvmClientPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {
  // W-17300955
  test("Test duplicated key payload in object shape") {
    val key1 = new PropertyShape().withName("key1").withRange(new ScalarShape().withDataType(DataTypes.Number))
    val text = new PropertyShape().withName("text").withRange(new ScalarShape().withDataType(DataTypes.String))
    val test = new NodeShape()
    test._internal.withProperties(Seq(key1._internal, text._internal))
    val payload = """{"key1": 1, "key1": 1, "text": "b"}""".trim
    val report  = payloadValidator(test, `application/json`).syncValidate(payload)
    report.conforms shouldBe false
    report._internal.results.head.completeMessage.contains("Duplicate key \"key1\"") shouldBe true
  }
}
