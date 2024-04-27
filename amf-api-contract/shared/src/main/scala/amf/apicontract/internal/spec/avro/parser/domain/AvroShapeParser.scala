package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidTypesType
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

class AvroShapeParser(map: YMap)(implicit ctx: AvroWebAPIContext) extends AvroKeyExtractor {
  val typeValue                 = map.typeValue
  def parse(): Option[AnyShape] = typeValue.flatMap(parseTypeEntry)

  def parseTypeEntry(value: YNode): Option[AnyShape] = {
    value.tagType match {
      case YType.Seq => Some(parseUnion(value.as[Seq[YNode]]))
      case YType.Str => Some(parseType(value.as[YScalar].text))
      case other =>
        ctx.violation(InvalidTypesType, "", s"Invalid tag type $other for type name", value.location)
        None
    }
  }
  private def parseUnion(members: Seq[YNode]) = AvroUnionShapeParser(members, map).parse()

  private def parseType(name: String): AnyShape = {
    name match {
      case "map"                 => parseMap()
      case "array"               => parseArray()
      case "record"              => parseRecord()
      case "error"               => parseRecord()
      case "enum"                => parseEnum()
      case "fixed"               => throw new UnsupportedOperationException() // TODO: discuss what to map fixed
      case _ if name.isPrimitive => parsePrimitiveType(name)
      case _                     => parseInherits(name)

    }
  }

  private def parseRecord() = new AvroRecordParser(map).parse()

  private def parseInherits(name: String) = {
    val parent = AvroReferenceParser(name, typeValue.getOrElse(YNode.Empty)).parse()
    val shape  = parent.meta.modelInstance
    shape.withInherits(Seq(parent))
  }

  private def parseEnum() = new AvroEnumParser(map).parse()

  private def parsePrimitiveType(`type`: String) = AvroScalarShapeParser(`type`, Some(map)).parse()

  private def parseMap() = AvroMapShapeParser(map).parse()

  private def parseArray() = AvroArrayShapeParser(map).parse()

}
