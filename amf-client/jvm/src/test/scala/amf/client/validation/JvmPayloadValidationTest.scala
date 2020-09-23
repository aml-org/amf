package amf.client.validation

import amf.client.model.DataTypes
import amf.client.model.domain.{NodeShape, ScalarShape}
import amf.convert.NativeOpsFromJvm
import amf.core.AMF
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import org.json.JSONException
import org.scalatest.Matchers.a
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._

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
}
