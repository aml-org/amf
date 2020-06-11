package amf.client.validation

import amf.client.convert.{NativeOps, WebApiRegister}
import amf.client.convert.CoreClientConverters._
import amf.client.model.DataTypes
import amf.client.model.domain.{AnyShape, ArrayShape, NodeShape, PropertyShape, ScalarShape, Shape}
import amf.core.AMF
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

trait ClientPayloadValidationTest extends AsyncFunSuite with NativeOps with Matchers {

  test("Test parameter validator int payload") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      test
        .parameterValidator("application/yaml")
        .asOption
        .get
        .validate("application/yaml", "1234")
        .asFuture
        .map(r => assert(r.conforms))
    }
  }

  test("Test parameter validator boolean payload") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      test
        .parameterValidator("application/yaml")
        .asOption
        .get
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

      shape
        .payloadValidator("application/json")
        .asOption
        .get
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

      array
        .payloadValidator("application/json")
        .asOption
        .get
        .validate("application/json", payload)
        .asFuture
        .map(r => assert(!r.conforms))
    }
  }

  test("Test sync validation") {
    AMF.init().flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)

      val test = new ScalarShape().withDataType(DataTypes.String).withName("test")

      val report = test
        .parameterValidator("application/yaml")
        .asOption
        .get
        .syncValidate("application/yaml", "1234")
      report.conforms shouldBe true
    }
  }

  test("'null' doesn't conform as string") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload = "null"
      val validator =
        new ScalarShape().withDataType(DataTypes.String).payloadValidator("application/yaml").asOption.get
      validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe false)
    }
  }

  test("'null' conforms as null") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload   = "null"
      val validator = new ScalarShape().withDataType(DataTypes.Nil).payloadValidator("application/yaml").asOption.get
      validator.validate("application/yaml", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Big number against scalar shape") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload = "22337203685477999090"
      val validator =
        new ScalarShape().withDataType(DataTypes.Number).payloadValidator("application/json").asOption.get
      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Very big number against scalar shape") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      val payload = "22e20000"
      val validator =
        new ScalarShape().withDataType(DataTypes.Number).payloadValidator("application/json").asOption.get
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
      val validator = shape.payloadValidator("application/json").asOption.get

      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe true)
    }
  }

  test("Invalid payload for json media type") {
    amf.Core.init().asFuture.flatMap { _ =>
      amf.Core.registerPlugin(PayloadValidatorPlugin)
      WebApiRegister.register(platform)

      val payload            = "Hello World"
      val stringShape: Shape = new ScalarShape().withDataType(DataTypes.String)
      val validator = new AnyShape()
        .withId("someId")
        .withOr(Seq(stringShape._internal).asClient)
        .payloadValidator("application/json")
        .asOption
        .get
      validator.validate("application/json", payload).asFuture.map(r => r.conforms shouldBe false)
    }
  }

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
