package amf.avro

import amf.apicontract.client.scala.AvroConfiguration
import amf.core.client.scala.model.domain.RecursiveShape
import amf.io.FileAssertionTest
import amf.shapes.client.scala.model.document.AvroSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, UnionShape}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class AvroSchemaResolutionTest extends AsyncFunSuite with Matchers with FileAssertionTest {
  private val base   = "file://amf-cli/shared/src/test/resources/avro/schemas/"
  private val client = AvroConfiguration.Avro().baseUnitClient()

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Avro Schema with valid recursive record field") {
    for {
      parsed <- client.parse(base + "record-valid-recursive.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
      parsed.conforms shouldBe true
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongList
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      // next (field with recursive union to LongList)
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      parsedRange shouldBe a[UnionShape]
      resolvedRange shouldBe a[UnionShape]
      val parsedUnion   = parsedRange.asInstanceOf[UnionShape]
      val resolvedUnion = resolvedRange.asInstanceOf[UnionShape]
      // link to LongList in parsing, should be a RecursiveShape post transformation
      parsedUnion.anyOf.last.isLink shouldBe true
      resolvedUnion.anyOf.last.isLink shouldBe false
      parsedUnion.anyOf.last shouldBe a[NodeShape]
      resolvedUnion.anyOf.last shouldBe a[RecursiveShape]
    }
  }

  test("Avro Schema with invalid recursive record field") {
    for {
      parsed <- client.parse(base + "record-invalid-recursive.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
//      parsed.conforms shouldBe true this fails in parsing because of the validateSchema() method
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongList
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      // next (field with recursive union to LongList WITHOUT a null union (that makes it valid))
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      // it inserts an empty AnyShape
      parsedRange shouldBe a[AnyShape]
      resolvedRange shouldBe a[AnyShape]
    }
  }

  test("Avro Schema with recursive record field inside another record field") {
    for {
      parsed <- client.parse(base + "record-valid-recursive-nested.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
//      parsed.conforms shouldBe true this fails in parsing because of the validateSchema() method
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongListParent
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      parsedRange shouldBe a[NodeShape]
      resolvedRange shouldBe a[NodeShape]
      // LongListChild
      val parsedRecord   = parsedRange.asInstanceOf[NodeShape]
      val resolvedRecord = resolvedRange.asInstanceOf[NodeShape]
      parsedRecord.properties.head.range shouldBe a[UnionShape]
      resolvedRecord.properties.head.range shouldBe a[UnionShape]
      // next (field with recursive union to LongListChild)
      val parsedUnion   = parsedRecord.properties.head.range.asInstanceOf[UnionShape]
      val resolvedUnion = resolvedRecord.properties.head.range.asInstanceOf[UnionShape]
      // link to LongListChild in parsing, should be a RecursiveShape post transformation
      parsedUnion.anyOf.last.isLink shouldBe true
      resolvedUnion.anyOf.last.isLink shouldBe false
      parsedUnion.anyOf.last shouldBe a[NodeShape]
      resolvedUnion.anyOf.last shouldBe a[RecursiveShape]
    }
  }

  test("Avro Schema with recursive record field inside another record field pointing to parent record") {
    for {
      parsed <- client.parse(base + "record-valid-recursive-nested-2.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
//      parsed.conforms shouldBe true this fails in parsing because of the validateSchema() method
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongListParent
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      parsedRange shouldBe a[NodeShape]
      resolvedRange shouldBe a[NodeShape]
      // LongListChild
      val parsedRecord   = parsedRange.asInstanceOf[NodeShape]
      val resolvedRecord = resolvedRange.asInstanceOf[NodeShape]
      parsedRecord.properties.head.range shouldBe a[UnionShape]
      resolvedRecord.properties.head.range shouldBe a[UnionShape]
      // next (field with recursive union to LongListParent)
      val parsedUnion   = parsedRecord.properties.head.range.asInstanceOf[UnionShape]
      val resolvedUnion = resolvedRecord.properties.head.range.asInstanceOf[UnionShape]
      // link to LongListParent in parsing, should be a RecursiveShape post transformation
      parsedUnion.anyOf.last.isLink shouldBe true
      resolvedUnion.anyOf.last.isLink shouldBe false
      parsedUnion.anyOf.last shouldBe a[NodeShape]
      resolvedUnion.anyOf.last shouldBe a[RecursiveShape]
    }
  }

  test("Avro record with array field with recursive items") {
    for {
      parsed <- client.parse(base + "record-valid-recursive-array-items.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
//      parsed.conforms shouldBe true this fails in parsing because of the validateSchema() method
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongList
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      // next (array field with items pointing to LongList)
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      parsedRange shouldBe a[ArrayShape]
      resolvedRange shouldBe a[ArrayShape]
      val parsedArray   = parsedRange.asInstanceOf[ArrayShape]
      val resolvedArray = resolvedRange.asInstanceOf[ArrayShape]
      // link to LongList in parsing, should be a RecursiveShape post transformation
      parsedArray.items.isLink shouldBe true
      resolvedArray.items.isLink shouldBe false
      parsedArray.items shouldBe a[NodeShape]
      resolvedArray.items shouldBe a[RecursiveShape]
    }
  }

  test("Avro record with map field with recursive values") {
    for {
      parsed <- client.parse(base + "record-valid-recursive-map-values.json")
      resolved = client.transform(parsed.baseUnit.cloneUnit())
    } yield {
//      parsed.conforms shouldBe true this fails in parsing because of the validateSchema() method
      resolved.conforms shouldBe true
      parsed.baseUnit shouldBe a[AvroSchemaDocument]
      resolved.baseUnit shouldBe a[AvroSchemaDocument]
      val parsedDoc   = parsed.baseUnit.asInstanceOf[AvroSchemaDocument]
      val resolvedDoc = resolved.baseUnit.asInstanceOf[AvroSchemaDocument]
      parsedDoc.encodes shouldBe a[NodeShape]
      resolvedDoc.encodes shouldBe a[NodeShape]
      // LongList
      val parsedEncoded   = parsedDoc.encodes.asInstanceOf[NodeShape]
      val resolvedEncoded = resolvedDoc.encodes.asInstanceOf[NodeShape]
      parsedEncoded.properties.nonEmpty shouldBe true
      resolvedEncoded.properties.nonEmpty shouldBe true
      // next (map field with values pointing to LongList)
      val parsedRange   = parsedEncoded.properties.head.range
      val resolvedRange = resolvedEncoded.properties.head.range
      parsedRange shouldBe a[NodeShape]
      resolvedRange shouldBe a[NodeShape]
      val parsedArray   = parsedRange.asInstanceOf[NodeShape]
      val resolvedArray = resolvedRange.asInstanceOf[NodeShape]
      // link to LongList in parsing, should be a RecursiveShape post transformation
      parsedArray.additionalPropertiesSchema.isLink shouldBe true
      resolvedArray.additionalPropertiesSchema.isLink shouldBe false
      parsedArray.additionalPropertiesSchema shouldBe a[NodeShape]
      resolvedArray.additionalPropertiesSchema shouldBe a[RecursiveShape]
    }
  }
}
