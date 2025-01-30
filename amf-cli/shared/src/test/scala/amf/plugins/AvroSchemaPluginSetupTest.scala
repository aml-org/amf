package amf.plugins

import amf.apicontract.client.scala.AvroConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFParseResult
import amf.core.internal.remote.Spec
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.model.document.AvroSchemaDocument
import amf.shapes.client.scala.model.domain.AnyShape
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class AvroSchemaPluginSetupTest extends AsyncFunSuite with Matchers with FileAssertionTest {

  private val base   = "file://amf-api-contract/shared/src/test/resources/avro/"
  private val client = AvroConfiguration.Avro().baseUnitClient()

  // test with .json and .avsc
  test("AvroSchemaParsePlugin plugin is called with a json document") {
    for {
      parsed <- parse("record.json")
    } yield {
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      parsed.sourceSpec shouldBe Spec.AVRO_SCHEMA
      parsed.conforms shouldBe true
      val doc = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      doc.declares should have size 0  // avro documents don't have declarations
      doc.encodes shouldBe a[AnyShape] // the avro schema
      doc.encodes.asInstanceOf[AnyShape].name.value() should equal("Person")
    }
  }

  test("AvroSchemaParsePlugin plugin is called with an avsc document") {
    for {
      parsed <- parse("record.avsc")
    } yield {
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      parsed.sourceSpec shouldBe Spec.AVRO_SCHEMA
      parsed.conforms shouldBe true
      val doc = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      doc.declares should have size 0  // avro documents don't have declarations
      doc.encodes shouldBe a[AnyShape] // the avro schema
      doc.encodes.asInstanceOf[AnyShape].name.value() should equal("Person")
    }
  }

  test("AvroSchemaRenderPlugin renders a AvroSchemaDocument") {
    parse("record.json")
      .map { parsed =>
        parsed.conforms shouldBe true
        val transformed = client.transform(parsed.baseUnit)
        transformed.conforms shouldBe true
        client.render(transformed.baseUnit)
      }
      .flatMap(assertEqual(_, "record-editing.json"))
  }

  test("AvroSchema's can be transformed with editing pipeline") {
    for {
      parsed <- parse("record.json")
    } yield {
      parsed.conforms shouldBe true
      val transformed = client.transform(parsed.baseUnit, PipelineId.Editing)
      transformed.conforms shouldBe true
    }
  }

  private def parse(uri: String): Future[AMFParseResult] = {
    val client = AvroConfiguration.Avro().baseUnitClient()
    client.parse(base + uri)
  }

  private def assertEqual(value: String, goldenPath: String): Future[Assertion] = {
    val golden = base + goldenPath
    writeTemporaryFile(golden)(value).flatMap(assertDifferences(_, golden))
  }
}
