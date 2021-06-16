package amf.client.validation

import amf.apicontract.client.platform.config.WebAPIConfiguration
import amf.cli.internal.convert.NativeOps
import amf.shapes.internal.convert.ShapeClientConverters._
import amf.core.client.common.validation.{ScalarRelaxedValidationMode, StrictValidationMode}
import amf.core.client.platform.AMFGraphConfiguration
import amf.core.client.platform.model.DataTypes
import amf.core.client.platform.model.domain.{PropertyShape, RecursiveShape, Shape}
import amf.core.client.platform.validation.AMFShapePayloadValidator
import amf.core.client.scala.model.domain.{RecursiveShape => InternalRecursiveShape}
import amf.shapes.client.platform.model.domain._
import amf.shapes.client.scala.domain.models.{ScalarShape => InternalScalarShape}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

trait PayloadValidationUtils {
  protected def defaultConfig = WebAPIConfiguration.WebAPI()

  protected def parameterValidator(s: Shape,
                                   mediaType: String,
                                   config: AMFGraphConfiguration = defaultConfig): AMFShapePayloadValidator =
    config.payloadValidatorFactory().createFor(s, mediaType, ScalarRelaxedValidationMode)

  protected def payloadValidator(s: Shape,
                                 mediaType: String,
                                 config: AMFGraphConfiguration = defaultConfig): AMFShapePayloadValidator =
    config.payloadValidatorFactory().createFor(s, mediaType, StrictValidationMode)
}

trait ClientPayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers with PayloadValidationUtils {

  protected val APPLICATION_YAML = "application/yaml"
  protected val APPLICATION_JSON = "application/json"

  test("Test parameter validator int payload") {
    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    parameterValidator(test, APPLICATION_YAML)
      .validate("1234")
      .asFuture
      .map(r => assert(r.conforms))
  }

  test("Test parameter validator boolean payload") {
    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

    parameterValidator(test, APPLICATION_YAML)
      .validate("true")
      .asFuture
      .map(r => assert(r.conforms))
  }

  test("Invalid trailing coma in json object payload") {
    val s     = new ScalarShape().withDataType(DataTypes.String)
    val shape = new NodeShape().withName("person")
    shape.withProperty("someString").withRange(s)

    val payload =
      """
          |{
          |  "someString": "invalid string value",
          |}
        """.stripMargin

    payloadValidator(shape, APPLICATION_JSON)
      .validate(payload)
      .asFuture
      .map(r => assert(!r.conforms))
  }

  test("Invalid trailing coma in json array payload") {

    val s     = new ScalarShape().withDataType(DataTypes.String)
    val array = new ArrayShape().withName("person")
    array.withItems(s)

    val payload =
      """
          |["trailing", "comma",]
        """.stripMargin

    payloadValidator(array, APPLICATION_JSON)
      .validate(payload)
      .asFuture
      .map(r => assert(!r.conforms))
  }

  test("Test sync validation") {
    val test   = new ScalarShape().withDataType(DataTypes.String).withName("test")
    val report = parameterValidator(test, APPLICATION_YAML).syncValidate("1234")
    report.conforms shouldBe true
  }

  test("'null' doesn't conform as string") {
    val payload   = "null"
    val shape     = new ScalarShape().withDataType(DataTypes.String)
    val validator = payloadValidator(shape, APPLICATION_YAML)
    validator.validate(payload).asFuture.map(r => r.conforms shouldBe false)
  }

  test("'null' conforms as null") {

    val payload   = "null"
    val shape     = new ScalarShape().withDataType(DataTypes.Nil)
    val validator = payloadValidator(shape, APPLICATION_YAML)
    validator.validate(payload).asFuture.map(r => r.conforms shouldBe true)
  }

  test("Big number against scalar shape") {

    val payload   = "22337203685477999090"
    val shape     = new ScalarShape().withDataType(DataTypes.Number)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator.validate(payload).asFuture.map(r => r.conforms shouldBe true)
  }

  test("Very big number against scalar shape") {

    val payload   = "22e20000"
    val shape     = new ScalarShape().withDataType(DataTypes.Number)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator.validate(payload).asFuture.map(r => r.conforms shouldBe true)
  }

  test("Big number against node shape") {

    val payload =
      """
          |{
          | "in": 22337203685477999090
          |}
          |""".stripMargin
    val properties = new PropertyShape()
      .withName("in")
      .withRange(new ScalarShape().withDataType(DataTypes.Number))
    val shape = new NodeShape()
      .withProperties(Seq(properties._internal).asClient)
    val validator = payloadValidator(shape, APPLICATION_JSON)

    validator.validate(payload).asFuture.map(r => r.conforms shouldBe true)
  }

  test("Invalid payload for json media type") {

    val payload            = "Hello World"
    val stringShape: Shape = new ScalarShape().withDataType(DataTypes.String)
    val shape = new AnyShape()
      .withId("someId")
      .withOr(Seq(stringShape._internal).asClient)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator.validate(payload).asFuture.map(r => r.conforms shouldBe false)
  }

  test("Test control characters in the middle of a number") {

    val test = new ScalarShape().withDataType(DataTypes.Integer)

    val report = payloadValidator(test, APPLICATION_JSON).syncValidate("123\n1234")
    report.conforms shouldBe false

  }

  test("Test that any payload conforms against an any type") {

    val test = new AnyShape()

    val report = payloadValidator(test, APPLICATION_JSON).syncValidate("any example")
    report.conforms shouldBe true
  }

  test("Test that recursive shape has a payload validator") {

    val innerShape     = InternalScalarShape().withDataType(DataTypes.Number)
    val recursiveShape = RecursiveShape(InternalRecursiveShape(innerShape))
    val validator      = payloadValidator(recursiveShape, APPLICATION_JSON)
    validator.syncValidate("5").conforms shouldBe true
    validator.syncValidate("true").conforms shouldBe false

  }

  test("Long type with int64 format is validated as long") {

    val shape     = new ScalarShape().withDataType(DataTypes.Long).withFormat("int64")
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator.syncValidate("0.1").conforms shouldBe false
  }

  test("Json payload with trailing characters should throw error - Object test") {

    val propertyA = new PropertyShape()
      .withName("a")
      .withRange(new ScalarShape().withDataType(DataTypes.String))
    val propertyB = new PropertyShape()
      .withName("b")
      .withRange(new ScalarShape().withDataType(DataTypes.String))
    val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator
      .syncValidate("""{"a": "aaaa", "b": "bbb"}asdfgh""")
      .conforms shouldBe false
  }

  test("Json payload with trailing characters should throw error - Array test") {

    val propertyA = new PropertyShape()
      .withName("a")
      .withRange(new ScalarShape().withDataType(DataTypes.String))
    val propertyB = new PropertyShape()
      .withName("b")
      .withRange(new ScalarShape().withDataType(DataTypes.String))
    val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator
      .syncValidate("""["a", "aaaa", "b", "bbb"]asdfgh""")
      .conforms shouldBe false
  }

  test("Date-time can only have 4 digits") {

    val shape     = new ScalarShape().withDataType(DataTypes.DateTimeOnly)
    val validator = payloadValidator(shape, APPLICATION_JSON)
    validator.syncValidate(""""22021-06-05T00:00:00"""").conforms shouldBe false
    validator.syncValidate(""""2021-06-05T00:00:00"""").conforms shouldBe true
  }

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
