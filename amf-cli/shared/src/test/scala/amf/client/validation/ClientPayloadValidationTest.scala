package amf.client.validation

import amf.cli.internal.convert.NativeOps
import amf.core.client.common.validation.StrictValidationMode
import amf.core.client.platform.model.DataTypes
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.platform.ShapesConfiguration
import amf.shapes.client.platform.model.domain.ScalarShape
import org.scalatest.funsuite.{AnyFunSuite, AsyncFunSuite}
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

trait ClientPayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

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

  private def payloadValidator(shape: ScalarShape, mediaType: String) = {
    ShapesConfiguration.predefined().elementClient().payloadValidatorFor(shape, mediaType, StrictValidationMode)
  }
}
