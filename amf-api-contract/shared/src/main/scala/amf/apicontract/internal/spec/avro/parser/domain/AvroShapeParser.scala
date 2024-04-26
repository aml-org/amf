package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidTypesType
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence, YTag, YType}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape, UnionShape}

class AvroShapeParser(map: YMap)(implicit ctx: AvroWebAPIContext) {

  def parse(): Option[AnyShape] = {
    map.key("type").flatMap(_.value.asScalar.map(_.text)).map(parseType)
  }

  private def parseUnion(members: Seq[YNode]) = AvroUnionShapeParser(members, map).parse()

  private def parseType(name: String): AnyShape = {
    name match {
      case "map"    => parseMap()
      case "array"  => parseArray()
      case "record" => parseRecord()
      case "error"  => parseRecord()
      case "enum"   => parseEnum()
      case "fixed"  => throw new UnsupportedOperationException() // TODO: discuss what to map fixed
      case _ if AvroShapeParser.isPrimitive(name) => parsePrimitiveType(name)

    }
  }

  private def parseRecord() = new AvroRecordParser(map).parse()

  private def parseEnum() = new AvroEnumParser(map).parse()

  private def parsePrimitiveType(`type`: String) = AvroScalarShapeParser(`type`, Some(map)).parse()

  private def parseMap() = AvroMapShapeParser(map).parse()

  private def parseArray() = AvroArrayShapeParser(map).parse()

}

object AvroShapeParser {
  def isPrimitive(`type`: String): Boolean =
    Seq("null", "boolean", "int", "long", "float", "double", "bytes", "string").contains(`type`)
}
