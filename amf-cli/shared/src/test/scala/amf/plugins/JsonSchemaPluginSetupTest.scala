package amf.plugins

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.core.client.scala.model.document.Document
import amf.core.internal.remote.Spec
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaPluginSetupTest extends AsyncFunSuite with Matchers with FileAssertionTest {

  private val base   = "file://amf-api-contract/shared/src/test/resources/json-schema/"
  private val client = JsonSchemaConfiguration.JsonSchema().baseUnitClient()

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("JsonSchemaParsePlugin plugin is called with a json document and a $schema entry") {
    for {
      parsed <- parse("simple.json")
    } yield {
      parsed.baseUnit shouldBe a[JsonSchemaDocument]
      parsed.sourceSpec shouldBe Spec.JSONSCHEMA
      parsed.conforms shouldBe true
      val doc = parsed.baseUnit.asInstanceOf[JsonSchemaDocument]
      doc.declares should have size 1
      doc.declares.head shouldBe a[AnyShape]
      doc.declares.head.asInstanceOf[AnyShape].name.value() should equal("Person")
    }
  }

  test("JsonSchemaParsePlugin plugin isn't called with just a json document") {
    recoverToSucceededIf[UnsupportedDomainForDocumentException] {
      parse("simple-no-$schema.json")
    }
  }

  test("JsonSchemaRenderPlugin renders a JsonSchemaDocument") {
    parse("simple.json")
      .map { parsed =>
        parsed.conforms shouldBe true
        val transformed = client.transform(parsed.baseUnit)
        transformed.conforms shouldBe true
        client.render(transformed.baseUnit)
      }
      .flatMap(assertEqual(_, "simple-editing.json"))
  }

  test("JsonSchemaParsePlugin is setup to parse with references") {
    for {
      parsed <- parse("simple-with-$ref.json")
    } yield {
      parsed.conforms shouldBe true
      val doc     = parsed.baseUnit.asInstanceOf[Document]
      val encodes = doc.encodes.asInstanceOf[ArrayShape]
      encodes.items shouldBe a[NodeShape]
    }
  }

  test("JsonSchema's can be transformed with editing pipeline") {
    for {
      parsed <- parse("simple.json")
    } yield {
      parsed.conforms shouldBe true
      val transformed = client.transform(parsed.baseUnit, PipelineId.Editing)
      transformed.conforms shouldBe true
    }
  }

  test("Parse document with 2019-09 draft") {
    for {
      parsed <- parse("simple-$defs.json")
    } yield {
      parsed.conforms shouldBe true
      val doc = parsed.baseUnit.asInstanceOf[Document]
      doc.declares should have size 1
    }
  }

  test("Cycled document with 2019-09 draft should use $defs instead of definitions") {
    for {
      parsed <- parse("simple-$defs.json")
    } yield {
      parsed.conforms shouldBe true
      val json = client.render(parsed.baseUnit)
      json should not include ("definitions")
      json should include("$defs")
    }
  }

  private def parse(uri: String): Future[AMFParseResult] = {
    val client = JsonSchemaConfiguration.JsonSchema().baseUnitClient()
    client.parse(base + uri)
  }

  private def assertEqual(value: String, goldenPath: String): Future[Assertion] = {
    val golden = (base + goldenPath)
    writeTemporaryFile(golden)(value).flatMap(assertDifferences(_, golden))
  }
}
