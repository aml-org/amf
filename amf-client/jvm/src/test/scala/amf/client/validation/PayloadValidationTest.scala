package amf.client.validation

import amf.client.model.DataTypes
import amf.client.model.domain.{NodeShape, ScalarShape}
import amf.convert.NativeOpsFromJvm
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import org.json.JSONException
import org.scalatest.Matchers.a
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._

class PayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {
  test("Invalid unquoted string value with both payload and parameter validator") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val s     = new ScalarShape().withDataType(DataTypes.String)
      val shape = new NodeShape().withName("person")
      shape.withProperty("someString").withRange(s)

      val payload =
        """
          |{
          |  "someString": invalid string value
          |}
        """.stripMargin

      val f = shape
        .payloadValidator("application/json")
        .asOption
        .get
        .validate("application/json", payload)
        .asFuture

      ScalaFutures.whenReady(f) { report =>
        report.conforms shouldBe false
        report.results.size() shouldBe 1
      }
    }
  }
}
