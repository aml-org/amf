package amf.jsonschema

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.AsyncAPIConfiguration.Async20
import amf.apicontract.client.scala.OASConfiguration.{OAS20, OAS30}
import amf.apicontract.client.scala.RAMLConfiguration.RAML10
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class JsonSchemaLinkerModelTest extends AsyncFunSuite with Matchers with JsonSchemaDocumentTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val basePath = "file://amf-cli/shared/src/test/resources/jsonschema/"

  test("OAS with linked Json Schema to definitions - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/oas20.yaml", config(OAS20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("OAS with linked Json Schema to root - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/oas20-root.yaml", config(OAS20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.encodes should equal(schema.linkTarget.get)
    }
  }

  test("OAS with linked Json Schema to $defs - Draft 2019-09") {
    for {
      (parsed, doc) <- parse("schemas/simple-2019.json", "apis/oas20-2019.yaml", config(OAS20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  ignore("OAS with linked Json Schema to definitions - Draft 2019-09") {
    for {
      (parsed, doc) <- parse(
        "schemas/simple-2019-definitions.json",
        "apis/oas20-2019-definitions.yaml",
        config(OAS20())
      )
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("RAML with linked Json Schema to definitions - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/api.raml", config(RAML10()))
    } yield {
      val schema = findSchemaInPayload(parsed).inherits.head
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("RAML with linked Json Schema to root - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/api-root.raml", config(RAML10()))
    } yield {
      val schema = findSchemaInPayload(parsed).inherits.head
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.encodes should equal(schema.linkTarget.get)
    }
  }

  test("RAML with linked Json Schema to $defs - Draft 2019-09") {
    for {
      (parsed, doc) <- parse("schemas/simple-2019.json", "apis/api-2019.raml", config(RAML10()))
    } yield {
      val schema = findSchemaInPayload(parsed).inherits.head
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("RAML with linked Json Schema to root - Draft 2019-09") {
    for {
      (parsed, doc) <- parse("schemas/simple-2019.json", "apis/api-2019-root.raml", config(RAML10()))
    } yield {
      val schema = findSchemaInPayload(parsed).inherits.head
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      doc.encodes should equal(schema.linkTarget.get)
    }
  }

  test("OAS 3 with linked Json Schema to definitions - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/oas30.yaml", config(OAS30()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("OAS 3 with linked Json Schema to $defs - Draft 2019") {
    for {
      (parsed, doc) <- parse("schemas/simple-2019.json", "apis/oas30-2019.yaml", config(OAS30()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("Async Api 2.0 with linked Json Schema to definitions - Draft 7") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/async20.yaml", config(Async20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("Async Api 2.0 with linked Json Schema to $defs - Draft 2019") {
    for {
      (parsed, doc) <- parse("schemas/simple-2019.json", "apis/async20-2019.yaml", config(Async20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(schema.linkTarget.get) shouldBe true
    }
  }

  test("Link to Json Schema Doc to External Fragment references Schema from Json Schema Doc") {
    for {
      (parsed, doc) <- parse("schemas/simple.json", "apis/oas20-with-ef.yaml", config(OAS20()))
    } yield {
      val schema = findSchemaInPayload(parsed)
      schema shouldBe a[ArrayShape]
      val itemsShape = schema.asInstanceOf[ArrayShape].items
      itemsShape.isLink shouldBe true
      itemsShape.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.declares.contains(itemsShape.linkTarget.get) shouldBe true
    }
  }

  private def config(base: AMFConfiguration): AMFConfiguration = {
    base.withErrorHandlerProvider(() => UnhandledErrorHandler)
  }

  private def findSchemaInPayload(parsed: AMFParseResult) = {
    parsed.baseUnit
      .asInstanceOf[Document]
      .encodes
      .asInstanceOf[Api]
      .endPoints
      .head
      .operations
      .head
      .responses
      .head
      .payloads
      .head
      .schema
  }
}
