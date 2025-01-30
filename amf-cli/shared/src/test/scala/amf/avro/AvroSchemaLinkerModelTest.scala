package amf.avro

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.AsyncAPIConfiguration.Async20
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.shapes.client.scala.model.domain.AnyShape
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class AvroSchemaLinkerModelTest extends AsyncFunSuite with Matchers with AvroSchemaDocumentTest {

  protected val basePath = "file://amf-cli/shared/src/test/resources/avro/doc/"

  test("Async API with AVRO Schema Fragment reference (JSON)") {
    for {
      (parsed, doc) <- parse("schemas/schema-1.9.0.json", "apis/async-basic-json.yaml", config(Async20()))
    } yield {
      val schema = findSchemaInPayload(parsed.baseUnit)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.encodes == schema.linkTarget.get shouldBe true
    }
  }

  test("Async API with AVRO Schema Fragment reference (AVSC)") {
    for {
      (parsed, doc) <- parse("schemas/schema-1.9.0.avsc", "apis/async-basic-avsc.yaml", config(Async20()))
    } yield {
      val schema = findSchemaInPayload(parsed.baseUnit)
      schema.isLink shouldBe true
      schema.linkTarget should not be None
      schema.linkTarget.get.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.encodes == schema.linkTarget.get shouldBe true
    }
  }

  test("Async API with AVRO Schema Fragment reference (JSON) - Resolved") {
    val configuration = config(Async20())
    for {
      (parsed, doc) <- parse("schemas/schema-1.9.0.json", "apis/async-basic-json.yaml", configuration)
      resolved = configuration.baseUnitClient().transform(parsed.baseUnit)
    } yield {
      val schema = findSchemaInPayload(resolved.baseUnit)
      schema.isLink shouldBe false
      schema.linkTarget shouldBe None
      schema.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.encodes == schema shouldBe true
    }
  }

  test("Async API with AVRO Schema Fragment reference (AVSC) - Resolved") {
    val configuration = config(Async20())
    for {
      (parsed, doc) <- parse("schemas/schema-1.9.0.avsc", "apis/async-basic-avsc.yaml", config(Async20()))
      resolved = configuration.baseUnitClient().transform(parsed.baseUnit)
    } yield {
      val schema = findSchemaInPayload(resolved.baseUnit)
      schema.isLink shouldBe false
      schema.linkTarget shouldBe None
      schema.asInstanceOf[AnyShape].name.value() shouldBe "Person"
      doc.encodes == schema shouldBe true
    }
  }

  private def config(base: AMFConfiguration): AMFConfiguration = {
    base.withErrorHandlerProvider(() => UnhandledErrorHandler)
  }

  private def findSchemaInPayload(baseUnit: BaseUnit) = {
    baseUnit
      .asInstanceOf[Document]
      .encodes
      .asInstanceOf[Api]
      .endPoints
      .head
      .operations
      .head
      .request
      .payloads
      .head
      .schema
  }
}
