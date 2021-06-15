package amf.client.validation

import amf.client.convert.{ApiRegister, NativeOps}
import amf.client.environment.APIConfiguration
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.PayloadValidator
import amf.core.internal.validation.ValidationConfiguration
import amf.remod.ClientShapePayloadValidatorFactory
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

trait PayloadValidationUtils {
  protected def config = new ValidationConfiguration(APIConfiguration.API())

  protected def parameterValidator(s: Shape): PayloadValidator = parameterValidator(s, config)

  protected def payloadValidator(s: Shape): PayloadValidator = payloadValidator(s, config)

  protected def parameterValidator(s: Shape, config: ValidationConfiguration): PayloadValidator =
    ClientShapePayloadValidatorFactory.createParameterValidator(s, config)

  protected def payloadValidator(s: Shape, config: ValidationConfiguration): PayloadValidator =
    ClientShapePayloadValidatorFactory.createPayloadValidator(s, config)
}

trait ClientPayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers with PayloadValidationUtils {

  // TODO: ARM - Will be fixed by PR
//  test("Test parameter validator int payload") {
//    AMF.init().flatMap { _ =>
//      AMF.registerPlugin(PayloadValidatorPlugin)
//
//      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")
//
//      parameterValidator(test)
//        .validate("application/yaml", "1234")
//        .asFuture
//        .map(r => assert(r.conforms))
//    }
//  }
//
//  test("Test parameter validator boolean payload") {
//    AMF.init().flatMap { _ =>
//      AMF.registerPlugin(PayloadValidatorPlugin)
//
//      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")
//
//      parameterValidator(test)
//        .validate("application/yaml", "true")
//        .asFuture
//        .map(r => assert(r.conforms))
//    }
//  }
//
//  test("Invalid trailing coma in json object payload") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//
//    val s     = new ScalarShape().withDataType(DataTypes.String)
//    val shape = new NodeShape().withName("person")
//    shape.withProperty("someString").withRange(s)
//
//    val payload =
//      """
//          |{
//          |  "someString": "invalid string value",
//          |}
//        """.stripMargin
//
//    payloadValidator(shape)
//      .validate("application/json", payload)
//      .asFuture
//      .map(r => assert(!r.conforms))
//  }
//
//  test("Invalid trailing coma in json array payload") {
//
//    AMF.registerPlugin(PayloadValidatorPlugin)
//
//    val s     = new ScalarShape().withDataType(DataTypes.String)
//    val array = new ArrayShape().withName("person")
//    array.withItems(s)
//
//    val payload =
//      """
//          |["trailing", "comma",]
//        """.stripMargin
//
//    payloadValidator(array)
//      .validate("application/json", payload)
//      .asFuture
//      .map(r => assert(!r.conforms))
//  }
//
//  test("Test sync validation") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//
//    val test = new ScalarShape().withDataType(DataTypes.String).withName("test")
//
//    val report = parameterValidator(test)
//      .syncValidate("application/yaml", "1234")
//    report.conforms shouldBe true
//  }
//
//  test("'null' doesn't conform as string") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val payload   = "null"
//    val shape     = new ScalarShape().withDataType(DataTypes.String)
//    val validator = payloadValidator(shape)
//    validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe false)
//  }
//
//  test("'null' conforms as null") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val payload   = "null"
//    val shape     = new ScalarShape().withDataType(DataTypes.Nil)
//    val validator = payloadValidator(shape)
//    validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe true)
//  }
//
//  test("Big number against scalar shape") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val payload   = "22337203685477999090"
//    val shape     = new ScalarShape().withDataType(DataTypes.Number)
//    val validator = payloadValidator(shape)
//    validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
//
//  }
//
//  test("Very big number against scalar shape") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val payload   = "22e20000"
//    val shape     = new ScalarShape().withDataType(DataTypes.Number)
//    val validator = payloadValidator(shape)
//    validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
//  }
//
//  test("Big number against node shape") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val payload =
//      """
//          |{
//          | "in": 22337203685477999090
//          |}
//          |""".stripMargin
//    val properties = new PropertyShape()
//      .withName("in")
//      .withRange(new ScalarShape().withDataType(DataTypes.Number))
//    val shape = new NodeShape()
//      .withProperties(Seq(properties._internal).asClient)
//    val validator = payloadValidator(shape)
//
//    validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
//  }
//
//  test("Invalid payload for json media type") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    ApiRegister.register(platform)
//
//    val payload            = "Hello World"
//    val stringShape: Shape = new ScalarShape().withDataType(DataTypes.String)
//    val shape = new AnyShape()
//      .withId("someId")
//      .withOr(Seq(stringShape._internal).asClient)
//    val validator = payloadValidator(shape)
//    validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe false)
//  }
//
//  test("Test control characters in the middle of a number") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//
//    val test = new ScalarShape().withDataType(DataTypes.Integer)
//
//    val report = payloadValidator(test).syncValidate("application/json", "123\n1234")
//    report.conforms shouldBe false
//
//  }
//
//  test("Test that any payload conforms against an any type") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//
//    val test = new AnyShape()
//
//    val report = payloadValidator(test).syncValidate("application/json", "any example")
//    report.conforms shouldBe true
//  }
//
//  test("Test that recursive shape has a payload validator") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val innerShape     = InternalScalarShape().withDataType(DataTypes.Number)
//    val recursiveShape = RecursiveShape(InternalRecursiveShape(innerShape))
//    val validator      = payloadValidator(recursiveShape)
//    validator.syncValidate("application/json", "5").conforms shouldBe true
//    validator.syncValidate("application/json", "true").conforms shouldBe false
//
//  }
//
//  test("Long type with int64 format is validated as long") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val shape     = new ScalarShape().withDataType(DataTypes.Long).withFormat("int64")
//    val validator = payloadValidator(shape)
//    validator.syncValidate("application/json", "0.1").conforms shouldBe false
//  }
//
//  test("Json payload with trailing characters should throw error - Object test") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val propertyA = new PropertyShape()
//      .withName("a")
//      .withRange(new ScalarShape().withDataType(DataTypes.String))
//    val propertyB = new PropertyShape()
//      .withName("b")
//      .withRange(new ScalarShape().withDataType(DataTypes.String))
//    val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
//    val validator = payloadValidator(shape)
//    validator
//      .syncValidate("application/json", """{"a": "aaaa", "b": "bbb"}asdfgh""")
//      .conforms shouldBe false
//  }
//
//  test("Json payload with trailing characters should throw error - Array test") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val propertyA = new PropertyShape()
//      .withName("a")
//      .withRange(new ScalarShape().withDataType(DataTypes.String))
//    val propertyB = new PropertyShape()
//      .withName("b")
//      .withRange(new ScalarShape().withDataType(DataTypes.String))
//    val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
//    val validator = payloadValidator(shape)
//    validator
//      .syncValidate("application/json", """["a", "aaaa", "b", "bbb"]asdfgh""")
//      .conforms shouldBe false
//  }
//
//  test("Date-time can only have 4 digits") {
//    AMF.registerPlugin(PayloadValidatorPlugin)
//    val shape     = new ScalarShape().withDataType(DataTypes.DateTimeOnly)
//    val validator = payloadValidator(shape)
//    validator.syncValidate("application/json", """"22021-06-05T00:00:00"""").conforms shouldBe false
//    validator.syncValidate("application/json", """"2021-06-05T00:00:00"""").conforms shouldBe true
//  }

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
