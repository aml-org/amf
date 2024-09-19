package amf.client.validation

import amf.cli.internal.convert.NativeOps
import amf.core.client.common.validation.{ScalarRelaxedValidationMode, StrictValidationMode}
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.client.scala.validation.payload.{AMFShapePayloadValidationPlugin, AMFShapePayloadValidator}
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.plugin.FailFastJsonSchemaPayloadValidationPlugin
import amf.shapes.internal.annotations.{AVRORawSchema, AVROSchemaType}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

trait PayloadValidationUtils {
  protected def defaultConfig: ShapesConfiguration = ShapesConfiguration.predefined()

  protected def parameterValidator(
      s: Shape,
      mediaType: String,
      config: AMFGraphConfiguration = defaultConfig
  ): AMFShapePayloadValidator =
    config.elementClient().payloadValidatorFor(s, mediaType, ScalarRelaxedValidationMode)

  protected def payloadValidator(
      s: Shape,
      mediaType: String,
      config: AMFGraphConfiguration = defaultConfig
  ): AMFShapePayloadValidator = {
    config.elementClient().payloadValidatorFor(s, mediaType, StrictValidationMode)
  }

  protected def validator(
      s: Shape,
      mediaType: String,
      plugin: AMFShapePayloadValidationPlugin
  ): AMFShapePayloadValidator =
    defaultConfig.withPlugin(plugin).elementClient().payloadValidatorFor(s, mediaType, StrictValidationMode)
}

trait PayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers with PayloadValidationUtils {

  test("Test parameter validator int payload") {
    val test = ScalarShape().withDataType(DataTypes.String).withName("test")

    parameterValidator(test, `application/yaml`)
      .validate("1234")
      .map(r => assert(r.conforms))
  }

  test("Test parameter validator boolean payload") {
    val test = ScalarShape().withDataType(DataTypes.String).withName("test")

    parameterValidator(test, `application/yaml`)
      .validate("true")
      .map(r => assert(r.conforms))
  }

  test("Invalid trailing coma in json object payload") {
    val s     = ScalarShape().withDataType(DataTypes.String)
    val shape = NodeShape().withName("person")
    shape.withProperty("someString").withRange(s)

    val payload =
      """
          |{
          |  "someString": "invalid string value",
          |}
        """.stripMargin

    payloadValidator(shape, `application/json`)
      .validate(payload)
      .map(r => assert(!r.conforms))
  }

  test("Invalid trailing coma in json array payload") {
    val s     = ScalarShape().withDataType(DataTypes.String)
    val array = ArrayShape().withName("person")
    array.withItems(s)

    val payload =
      """
          |["trailing", "comma",]
        """.stripMargin

    payloadValidator(array, `application/json`)
      .validate(payload)
      .map(r => assert(!r.conforms))
  }

  test("Test sync validation") {
    val test   = ScalarShape().withDataType(DataTypes.String).withName("test")
    val report = parameterValidator(test, `application/yaml`).syncValidate("1234")
    report.conforms shouldBe true
  }

  test("'null' doesn't conform as string") {
    val payload   = "null"
    val shape     = ScalarShape().withDataType(DataTypes.String)
    val validator = payloadValidator(shape, `application/yaml`)
    validator.validate(payload).map(r => r.conforms shouldBe false)
  }

  test("'null' conforms as null") {
    val payload   = "null"
    val shape     = ScalarShape().withDataType(DataTypes.Nil)
    val validator = payloadValidator(shape, `application/yaml`)
    validator.validate(payload).map(r => r.conforms shouldBe true)
  }

  test("Big number against scalar shape") {
    val payload   = "22337203685477999090"
    val shape     = ScalarShape().withDataType(DataTypes.Number)
    val validator = payloadValidator(shape, `application/json`)
    validator.validate(payload).map(r => r.conforms shouldBe true)
  }

  test("Very big number against scalar shape") {
    val payload   = "22e20000"
    val shape     = ScalarShape().withDataType(DataTypes.Number)
    val validator = payloadValidator(shape, `application/json`)
    validator.validate(payload).map(r => r.conforms shouldBe true)
  }

  test("Big number against node shape") {
    val payload =
      """
          |{
          | "in": 22337203685477999090
          |}
          |""".stripMargin
    val properties = PropertyShape()
      .withName("in")
      .withRange(ScalarShape().withDataType(DataTypes.Number))
    val shape = NodeShape()
      .withProperties(Seq(properties))
    val validator = payloadValidator(shape, `application/json`)

    validator.validate(payload).map(r => r.conforms shouldBe true)
  }

  test("Invalid payload for json media type") {
    val payload            = "Hello World"
    val stringShape: Shape = ScalarShape().withDataType(DataTypes.String)
    val shape = AnyShape()
      .withId("someId")
      .withOr(Seq(stringShape))
    val validator = payloadValidator(shape, `application/json`)
    validator.validate(payload).map(r => r.conforms shouldBe false)
  }

  test("Test control characters in the middle of a number") {
    val test   = ScalarShape().withDataType(DataTypes.Integer)
    val report = payloadValidator(test, `application/json`).syncValidate("123\n1234")
    report.conforms shouldBe false
  }

  test("Test that any payload conforms against an any type") {
    val test = AnyShape()

    val report = payloadValidator(test, `application/json`).syncValidate("any example")
    report.conforms shouldBe true
  }

  test("Test that an invalid object payload is validated against an any type") {
    val test = AnyShape()
    val payload = """{
                    |  "a": "something"
                    |  "b": "other thing"
                    |}""".stripMargin

    val report = payloadValidator(test, `application/json`).syncValidate(payload)
    report.conforms shouldBe false
  }

  test("Test that recursive shape has a payload validator") {
    val innerShape     = ScalarShape().withDataType(DataTypes.Number)
    val recursiveShape = RecursiveShape(innerShape)
    val validator      = payloadValidator(recursiveShape, `application/json`)
    validator.syncValidate("5").conforms shouldBe true
    validator.syncValidate("true").conforms shouldBe false
  }

  test("Long type with int64 format is validated as long") {
    val shape     = ScalarShape().withDataType(DataTypes.Long).withFormat("int64")
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate("0.1").conforms shouldBe false
  }

  test("Json payload with trailing characters should throw error - Object test") {
    val propertyA = PropertyShape()
      .withName("a")
      .withRange(ScalarShape().withDataType(DataTypes.String))
    val propertyB = PropertyShape()
      .withName("b")
      .withRange(ScalarShape().withDataType(DataTypes.String))
    val shape     = NodeShape().withProperties(Seq(propertyA, propertyB))
    val validator = payloadValidator(shape, `application/json`)
    validator
      .syncValidate("""{"a": "aaaa", "b": "bbb"}asdfgh""")
      .conforms shouldBe false
  }

  test("Json payload with trailing characters should throw error - Array test") {
    val propertyA = PropertyShape()
      .withName("a")
      .withRange(ScalarShape().withDataType(DataTypes.String))
    val propertyB = PropertyShape()
      .withName("b")
      .withRange(ScalarShape().withDataType(DataTypes.String))
    val shape     = NodeShape().withProperties(Seq(propertyA, propertyB))
    val validator = payloadValidator(shape, `application/json`)
    validator
      .syncValidate("""["a", "aaaa", "b", "bbb"]asdfgh""")
      .conforms shouldBe false
  }

  test("Date-time can only have 4 digits") {
    val shape     = ScalarShape().withDataType(DataTypes.DateTimeOnly)
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate(""""22021-06-05T00:00:00"""").conforms shouldBe false
    validator.syncValidate(""""2021-06-05T00:00:00"""").conforms shouldBe true
  }

  test("Fail fast plugin should have less results than complete") {
    val node = NodeShape()
    node.withProperty("a").withRange(ScalarShape().withDataType(DataTypes.String))
    node.withProperty("b").withRange(ScalarShape().withDataType(DataTypes.Boolean))
    val payload =
      """
        |{
        |  "a": true,
        |  "b": 5
        |}
        |""".stripMargin

    val failFast =
      payloadValidator(node, `application/json`, defaultConfig.withPlugin(FailFastJsonSchemaPayloadValidationPlugin))
    val slimReport = failFast.syncValidate(payload)
    slimReport.conforms shouldBe false
    slimReport.results should have length 1

    val fullValidator = payloadValidator(node, `application/json`)
    val fullReport    = fullValidator.syncValidate(payload)
    fullReport.conforms shouldBe false
    fullReport.results should have length 2
  }

  test("Leap year DateTimeOnly") {
    val shape     = ScalarShape().withDataType(DataTypes.DateTimeOnly)
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate(""""2020-02-29T00:00:00"""").conforms shouldBe true
    validator.syncValidate(""""2023-02-29T00:00:00"""").conforms shouldBe false
  }

  test("Leap year DateTime") {
    val shape     = ScalarShape().withDataType(DataTypes.DateTime)
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate(""""2020-02-29T16:41:41.090Z"""").conforms shouldBe true
    validator.syncValidate(""""2023-02-29T16:41:41.090Z"""").conforms shouldBe false
  }

  test("Leap year Date") {
    val shape     = ScalarShape().withDataType(DataTypes.Date)
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate(""""2020-02-29"""").conforms shouldBe true
    validator.syncValidate(""""2023-02-29"""").conforms shouldBe false
  }

  test("Leap year DateTime CRI Case") {
    val shape     = ScalarShape().withDataType(DataTypes.DateTime).withFormat("rfc3339")
    val validator = payloadValidator(shape, `application/json`)
    validator.syncValidate(""""2022-02-29T23:59:59Z""").conforms shouldBe false
    validator.syncValidate(""""2024-02-29T23:59:59Z"""").conforms shouldBe true
  }

  test("Invalid avro record payload") {
    val rawSchema =
      """
        |{
        |  "type": "record",
        |  "name": "LongList",
        |  "namespace": "root",
        |  "aliases": ["LinkedLongs"],
        |  "fields": [
        |    {
        |      "name": "value",
        |      "type": "long"
        |    }
        |  ]
        |}
        """.stripMargin

    val avroSchema = NodeShape()
    avroSchema.annotations += AVROSchemaType("record")
    avroSchema.annotations += AVRORawSchema(rawSchema)

    val payload =
      """
        |{
        |  "someString": "invalid string value"
        |}
        """.stripMargin

    val validator = payloadValidator(avroSchema, `application/json`)
    validator
      .validate(payload)
      .map { r =>
        assert(!r.conforms)
      }
  }


  test("valid avro int payload") {
    val shape = ScalarShape().withName("int")
    shape.annotations += AVROSchemaType("record")

    val raw =
      """
        |{
        |  "type": "int",
        |  "name": "this is an int"
        |}
        """.stripMargin

    shape.annotations += AVRORawSchema(raw)

    val payload = "1"

    val validator = payloadValidator(shape, `application/json`)
    validator
      .validate(payload)
      .map { r =>
        assert(r.conforms)
      }
  }

  protected def makeAvroShape(raw: String, kind: String, base: AnyShape): AnyShape = {
    base.annotations += AVROSchemaType(kind)
    base.annotations += AVRORawSchema(raw)
    base
  }

  protected def reportContainError(report: AMFValidationReport, message: String): Boolean = {
    report.results.exists(result => result.message.contains(message))
  }

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}

object AvroTestSchemas {
  val invalidSchema: String = // Schema and field has no name
    """
      |{
      |  "type": "record",
      |  "fields": [
      |    {
      |      "type": "string"
      |    }
      |  ]
      |}
        """.stripMargin

  val recordSchema: String =
    """
      |{
      |  "type": "record",
      |  "name": "recordTest",
      |  "fields": [
      |    {
      |      "name": "a",
      |      "type": "string"
      |    }
      |  ]
      |}
        """.stripMargin
}
