package amf.client.validation
import amf.client.convert.NativeOps
import amf.client.model.DataTypes
import amf.client.model.domain.ScalarShape
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

trait ClientPayloadValidationTest extends FunSuite with NativeOps {

  test("Test parameter validator int payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    test
      .parameterValidator("application/yaml")
      .asOption
      .get
      .isValid("application/yaml", "1234")
      .asFuture
      .map(assert(_))
  }

  test("Test parameter validator boolean payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    test
      .parameterValidator("application/yaml")
      .asOption
      .get
      .isValid("application/yaml", "true")
      .asFuture
      .map(assert(_))
  }

}
