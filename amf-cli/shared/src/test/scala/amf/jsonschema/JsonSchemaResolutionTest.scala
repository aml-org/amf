package amf.jsonschema

import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaResolutionTest extends AsyncFunSuite with Matchers with FileAssertionTest {
  private val base   = "file://amf-cli/shared/src/test/resources/jsonschema/schemas/"
  private val client = JsonSchemaConfiguration.JsonSchema().baseUnitClient()

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Json Schema Fragment with root $ref to an internal declaration") {
    for {
      parsed   <- client.parse(base + "simple-inner-ref.json")
      resolved <- Future.successful(client.transform(parsed.baseUnit.cloneUnit()))
    } yield {
      parsed.conforms shouldBe true
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[JsonSchemaDocument]
      resolved.baseUnit shouldBe a[JsonSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[JsonSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[JsonSchemaDocument]
      parsedDoc.encodes shouldBe a[ArrayShape]
      resolvedDoc.encodes shouldBe a[ArrayShape]
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[ArrayShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[ArrayShape]
      parsedEncoded.items shouldBe a[NodeShape]
      resolvedEncoded.items shouldBe a[NodeShape]
      parsedEncoded.items.isLink shouldBe true
      resolvedEncoded.items.isLink shouldBe false
      resolvedEncoded.items.asInstanceOf[NodeShape].properties.size shouldBe 2
      resolvedEncoded.items.asInstanceOf[NodeShape].properties.map(_.name.value()).contains("age") shouldBe true
    }
  }

  test("Json Schema Fragment with root $ref to an external Json Schema declaration") {
    for {
      parsed   <- client.parse(base + "simple-ref-external/base.json")
      resolved <- Future.successful(client.transform(parsed.baseUnit.cloneUnit()))
    } yield {
      parsed.conforms shouldBe true
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[JsonSchemaDocument]
      resolved.baseUnit shouldBe a[JsonSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[JsonSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[JsonSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      parsedDoc.encodes.asInstanceOf[NodeShape].properties.head.range.isLink shouldBe true
      resolvedDoc.encodes.asInstanceOf[NodeShape].properties.head.range.isLink shouldBe false
      resolvedDoc.encodes.asInstanceOf[NodeShape].properties.head.range shouldBe a[NodeShape]
      val encodedPropertyRange =
        resolvedDoc.encodes.asInstanceOf[NodeShape].properties.head.range.asInstanceOf[NodeShape]
      encodedPropertyRange.properties.map(_.name.value()).contains("frequency") shouldBe true
    }
  }

}
