package amf.client.model

import amf.core.client.platform.model.DataTypes
import amf.shapes.client.platform.model.domain.ScalarShape
import org.scalatest.{FunSuite, Matchers}

class ScalarModelTest extends FunSuite with Matchers {

  test("Double scalar fields") {
    val number = new ScalarShape()
      .withDataType(DataTypes.Double)
      .withMinLength(1)
      .withMaxLength(2)
      .withMinimum(1.1)
      .withMaximum(50.50)
      .withExclusiveMinimum(true)
      .withExclusiveMaximum(true)
      .withFormat("double")
      .withMultipleOf(2.2)

    number.dataType.value() should be(DataTypes.Double)
    number.minLength.value() should be(1)
    number.maxLength.value() should be(2)
    number.minimum.value() should be(1.1)
    number.maximum.value() should be(50.50)
    number.exclusiveMinimum.value() should be(true)
    number.exclusiveMaximum.value() should be(true)
    number.multipleOf.value() should be(2.2)

  }

  test("Double scalar empty fields") {
    val number = new ScalarShape()

    assert(number.dataType.value() == null)
    number.minLength.value() should be(0)
    number.maxLength.value() should be(0)
    number.minimum.value() should be(0.0)
    number.maximum.value() should be(0.0)
    number.exclusiveMinimum.value() should be(false)
    number.exclusiveMaximum.value() should be(false)
    number.multipleOf.value() should be(0.0)
  }

  test("Integer scalar fields") {
    val number = new ScalarShape()
      .withDataType(DataTypes.Integer)
      .withMinLength(1)
      .withMaxLength(2)
      .withMinimum(1)
      .withMaximum(50)
      .withExclusiveMinimum(true)
      .withExclusiveMaximum(true)
      .withFormat("double")
      .withMultipleOf(2)

    number.dataType.value() should be(DataTypes.Integer)
    number.minLength.value() should be(1)
    number.maxLength.value() should be(2)
    number.minimum.value() should be(1)
    number.maximum.value() should be(50)
    number.exclusiveMinimum.value() should be(true)
    number.exclusiveMaximum.value() should be(true)
    number.multipleOf.value() should be(2)
  }

  test("Integer scalar empty fields") {
    val number = new ScalarShape()
    assert(number.dataType.value() == null)
    number.minLength.value() should be(0)
    number.maxLength.value() should be(0)
    number.minimum
    number.minimum.value() should be(0.0)
    number.maximum.value() should be(0.0)
    number.exclusiveMinimum.value() should be(false)
    number.exclusiveMaximum.value() should be(false)
    number.multipleOf.value() should be(0.0)
  }
}
