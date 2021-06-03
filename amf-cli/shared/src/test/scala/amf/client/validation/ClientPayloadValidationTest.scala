package amf.client.validation

import amf.cli.convert.NativeOps
import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.convert.ApiRegister
import amf.client.environment.Environment
import amf.client.model.DataTypes
import amf.client.model.domain._
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.plugins.domain.shapes.models.{ScalarShape => InternalScalarShape}
import amf.core.model.domain.{RecursiveShape => InternalRecursiveShape}
import amf.client.validate.PayloadValidator
import amf.core.AMF
import amf.plugins.document.apicontract.validation.PayloadValidatorPlugin
import amf.remod.{ClientShapePayloadValidatorFactory, ShapePayloadValidatorFactory}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

trait PayloadValidationUtils {
  protected def parameterValidator(s: Shape, environment: Environment = Environment.empty()): PayloadValidator =
    ClientShapePayloadValidatorFactory.createParameterValidator(s, AMFGraphConfiguration.predefined())
  protected def payloadValidator(s: Shape, environment: Environment = Environment.empty()): PayloadValidator =
    ClientShapePayloadValidatorFactory.createPayloadValidator(s, AMFGraphConfiguration.predefined())
}

trait ClientPayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers with PayloadValidationUtils {

  test("Test parameter validator int payload") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      parameterValidator(test)
        .validate("application/yaml", "1234")
        .asFuture
        .map(r => assert(r.conforms))
    }
  }

  test("Test parameter validator boolean payload") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      parameterValidator(test)
        .validate("application/yaml", "true")
        .asFuture
        .map(r => assert(r.conforms))
    }
  }

  test("Invalid trailing coma in json object payload") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val s     = new ScalarShape().withDataType(DataTypes.String)
      val shape = new NodeShape().withName("person")
      shape.withProperty("someString").withRange(s)

      val payload =
        """
          |{
          |  "someString": "invalid string value",
          |}
        """.stripMargin

      payloadValidator(shape)
        .validate("application/json", payload)
        .asFuture
        .map(r => assert(!r.conforms))
    }
  }

  test("Invalid trailing coma in json array payload") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val s     = new ScalarShape().withDataType(DataTypes.String)
      val array = new ArrayShape().withName("person")
      array.withItems(s)

      val payload =
        """
          |["trailing", "comma",]
        """.stripMargin

      payloadValidator(array)
        .validate("application/json", payload)
        .asFuture
        .map(r => assert(!r.conforms))
    }
  }

  test("Test sync validation") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      val report = parameterValidator(test)
        .syncValidate("application/yaml", "1234")
      report.conforms shouldBe true
    }
  }

  test("'null' doesn't conform as string") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload   = "null"
      val shape     = new ScalarShape().withDataType(DataTypes.String)
      val validator = payloadValidator(shape)
      validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe false)
    }
  }

  test("'null' conforms as null") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload   = "null"
      val shape     = new ScalarShape().withDataType(DataTypes.Nil)
      val validator = payloadValidator(shape)
      validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Big number against scalar shape") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload   = "22337203685477999090"
      val shape     = new ScalarShape().withDataType(DataTypes.Number)
      val validator = payloadValidator(shape)
      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Very big number against scalar shape") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload   = "22e20000"
      val shape     = new ScalarShape().withDataType(DataTypes.Number)
      val validator = payloadValidator(shape)
      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Big number against node shape") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
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
      val validator = payloadValidator(shape)

      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Invalid payload for json media type") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      ApiRegister.register(platform)

      val payload            = "Hello World"
      val stringShape: Shape = new ScalarShape().withDataType(DataTypes.String)
      val shape = new AnyShape()
        .withId("someId")
        .withOr(Seq(stringShape._internal).asClient)
      val validator = payloadValidator(shape)
      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe false)
    }
  }

  test("Test control characters in the middle of a number") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.Integer)

      val report = payloadValidator(test).syncValidate("application/json", "123\n1234")
      report.conforms shouldBe false
    }
  }

  test("Test that any payload conforms against an any type") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new AnyShape()

      val report = payloadValidator(test).syncValidate("application/json", "any example")
      report.conforms shouldBe true
    }
  }

  test("Test that recursive shape has a payload validator") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val innerShape     = InternalScalarShape().withDataType(DataTypes.Number)
      val recursiveShape = RecursiveShape(InternalRecursiveShape(innerShape))
      val validator      = payloadValidator(recursiveShape)
      validator.syncValidate("application/json", "5").conforms shouldBe true
      validator.syncValidate("application/json", "true").conforms shouldBe false
    }
  }

  test("Long type with int64 format is validated as long") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val shape     = new ScalarShape().withDataType(DataTypes.Long).withFormat("int64")
      val validator = payloadValidator(shape)
      validator.syncValidate("application/json", "0.1").conforms shouldBe false
    }
  }

  test("Json payload with trailing characters should throw error - Object test") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val propertyA = new PropertyShape()
        .withName("a")
        .withRange(new ScalarShape().withDataType(DataTypes.String))
      val propertyB = new PropertyShape()
        .withName("b")
        .withRange(new ScalarShape().withDataType(DataTypes.String))
      val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
      val validator = payloadValidator(shape)
      validator
        .syncValidate("application/json", """{"a": "aaaa", "b": "bbb"}asdfgh""")
        .conforms shouldBe false
    }
  }

  test("Json payload with trailing characters should throw error - Array test") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val propertyA = new PropertyShape()
        .withName("a")
        .withRange(new ScalarShape().withDataType(DataTypes.String))
      val propertyB = new PropertyShape()
        .withName("b")
        .withRange(new ScalarShape().withDataType(DataTypes.String))
      val shape     = new NodeShape().withProperties(Seq(propertyA._internal, propertyB._internal).asClient)
      val validator = payloadValidator(shape)
      validator
        .syncValidate("application/json", """["a", "aaaa", "b", "bbb"]asdfgh""")
        .conforms shouldBe false
    }
  }

  test("Date-time can only have 4 digits") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val shape     = new ScalarShape().withDataType(DataTypes.DateTimeOnly)
      val validator = payloadValidator(shape)
      validator.syncValidate("application/json", """"22021-06-05T00:00:00"""").conforms shouldBe false
      validator.syncValidate("application/json", """"2021-06-05T00:00:00"""").conforms shouldBe true
    }
  }

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
