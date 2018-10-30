package amf.client.validation
import amf.client.model.DataTypes
import amf.client.model.domain.ScalarShape
import org.scalatest.FunSuite

class ClientPayloadValidationTest extends FunSuite {

  test("Test parameter validator int payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    assert(test.parameterValidator().validate("application/yaml", "1234"))
  }

  test("Test parameter validator boolean payload") {

    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    assert(test.parameterValidator().validate("application/yaml", "true"))
  }

}
