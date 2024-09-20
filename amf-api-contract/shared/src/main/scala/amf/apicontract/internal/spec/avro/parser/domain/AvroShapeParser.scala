package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.apicontract.unsafe.AvroSchemaValidatorBuilder.validateSchema
import amf.shapes.internal.validation.avro.AvroKnownErrors
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidAvroSchema
import org.yaml.model.{YMap, YNode, YScalar, YType}

class AvroShapeParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroKeyExtractor {
  val typeValue: Option[YNode] = map.typeValue

  def parse(): Option[AnyShape] = typeValue.flatMap(parseTypeEntry)

  def parseTypeEntry(value: YNode): Option[AnyShape] = {
    val (maybeShape, avroType) = value.tagType match {
      case YType.Seq =>
        val union = parseUnion(value.as[Seq[YNode]])
        (Some(union), "union")
      case YType.Str =>
        val specificType = value.as[YScalar].text
        (Some(parseType(specificType)), specificType)
      case _ => (None, "invalid")
    }

    postProcessShape(maybeShape, avroType, map)

    maybeShape
  }

  private def postProcessShape(maybeShape: Option[AnyShape], avroType: String, map: YMap): Unit = {
    val shape = maybeShape.getOrElse(AnyShape(map))
    annotatedAvroShape(shape, avroType, map)
    val results = validateSchema(shape)
    AvroKnownErrors.filterResults(results).foreach(r => ctx.violation(InvalidAvroSchema, shape, r.message))
  }

  private def parseUnion(members: Seq[YNode]): AnyShape = AvroUnionShapeParser(members, map).parse()

  private def parseType(name: String): AnyShape = {
    name match {
      case "map"                 => parseMap()
      case "array"               => parseArray()
      case "record"              => parseRecord()
      case "enum"                => parseEnum()
      case "fixed"               => parseFixed()
      case _ if name.isPrimitive => parsePrimitiveType(name)
      // todo: should validate invalid type here? already validating with validator
      case _ => AnyShape() // ignore
    }
  }

  private def parseRecord()         = new AvroRecordParser(map).parse()
  private def parseEnum(): AnyShape = new AvroEnumParser(map).parse()
  private def parsePrimitiveType(`type`: String): AnyShape =
    AvroTextTypeParser(`type`, Some(map)).parse()
  private def parseFixed(): AnyShape = AvroFixedShapeParser(map).parse()
  private def parseMap(): AnyShape   = AvroMapShapeParser(map).parse()
  private def parseArray(): AnyShape = AvroArrayShapeParser(map).parse()
}
