package amf.client.validation

import amf.client.model.DataTypes
import amf.client.model.domain.{NodeShape, ScalarShape}
import amf.convert.NativeOpsFromJvm
import amf.core.AMF
import amf.plugins.document.apicontract.validation.PayloadValidatorPlugin
import org.json.JSONException
import org.scalatest.Matchers.a
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._

class JvmPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {

  test("Test unexpected type error") {
    AMF.registerPlugin(PayloadValidatorPlugin)

    val test = new ScalarShape().withDataType(DataTypes.String)

    val report = payloadValidator(test).syncValidate("application/json", "1234")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
  }

  // regex pending analysis (APIMF-3058) jvm cannot process regex, on the other hand it is valid for js
  test("Avoid exception for pattern regex that cannot be parsed") {
    AMF.registerPlugin(PayloadValidatorPlugin)
    val shape = new ScalarShape()
      .withDataType(DataTypes.String)
      .withPattern(
        "^(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
    val validator = payloadValidator(shape)
    val report    = validator.syncValidate("application/json", """"irrelevant text"""")
    report.conforms shouldBe false
    report.results.asSeq.head.message shouldBe "Regex defined in schema could not be processed"
  }
}
