package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.AVROSchemaType
import org.yaml.model.{YMap, YNode, YScalar}

case class AvroTextParser(node: YNode)(implicit ctx: AvroSchemaContext) extends AvroKeyExtractor {
  def parse(): AnyShape = {
    node.value match {
      case scalar: YScalar =>
        val avroType    = scalar.text
        val parsedShape = AvroTextTypeParser(scalar.text, None).parse()
        parsedShape.annotations += AVROSchemaType(avroType)
        parsedShape
      case map: YMap => new AvroShapeParser(map).parse().getOrElse(AnyShape())
    }
  }
}
