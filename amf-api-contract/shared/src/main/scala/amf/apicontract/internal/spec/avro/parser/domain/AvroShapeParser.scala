package amf.apicontract.internal.spec.avro.parser.domain
import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidTypesType
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.IsAVROSchema
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
      case other =>
        ctx.violation(InvalidTypesType, "", s"Invalid tag type $other for type name", value.location)
        (None, "invalid avro type")
    }
    maybeShape.map(_.annotations += IsAVROSchema(avroType))
    maybeShape
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
      case _                     => AnyShape() // ignore todo: validate UnsuppportedTypeValidation()
    }
  }

  private def parseRecord() = new AvroRecordParser(map).parse()

  private def parseEnum(): AnyShape                        = new AvroEnumParser(map).parse()
  private def parsePrimitiveType(`type`: String): AnyShape = AvroScalarShapeParser(`type`, Some(map)).parse()
  private def parseFixed(): AnyShape                       = AvroFixedShapeParser(map).parse()
  private def parseMap(): AnyShape                         = AvroMapShapeParser(map).parse()
  private def parseArray(): AnyShape                       = AvroArrayShapeParser(map).parse()
}
