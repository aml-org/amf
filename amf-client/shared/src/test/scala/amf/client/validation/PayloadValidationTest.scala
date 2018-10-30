package amf.client.validation
import amf.client.convert.NativeOps
import amf.client.model.DataTypes
import amf.client.model.domain.ScalarShape
import org.scalatest.FunSuite

trait ClientPayloadValidationTest extends FunSuite with NativeOps {

  test("Test parameter validator int payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    assert(test.parameterValidator("application/yaml").asOption.get.fastValidation("application/yaml", "1234"))
  }

  test("Test parameter validator boolean payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    assert(test.parameterValidator("application/yaml").asOption.get.fastValidation("application/yaml", "true"))
  }

}
