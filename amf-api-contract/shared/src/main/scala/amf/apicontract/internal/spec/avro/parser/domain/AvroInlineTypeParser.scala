package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model.{YMap, YNode, YScalar, YSequence}

case class AvroInlineTypeParser(node: YNode)(implicit ctx: AvroSchemaContext) extends AvroKeyExtractor {
  def parse(): AnyShape = {
    node.value match {
      case scalar: YScalar =>
        val avroType    = scalar.text
        val parsedShape = AvroTextTypeParser(scalar.text, None).parse()
        annotatedAvroShape(parsedShape, avroType, node)
      case map: YMap => // todo: putting an empty AnyShape when the union type is incorrect is kinda weird behavior
        new AvroShapeParser(map).parse().getOrElse(AnyShape(map))
      case seq: YSequence => // The only valid case here for a seq is a Union
        AvroInlineUnionShapeParser(seq).parse()
      case _ => AnyShape(node)
    }
  }
}
