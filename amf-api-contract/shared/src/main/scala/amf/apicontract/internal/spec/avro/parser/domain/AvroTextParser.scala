package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model.{YNode, YScalar}

case class AvroTextParser(node: YNode)(implicit ctx: AvroSchemaContext) extends AvroKeyExtractor {
  def parse(): AnyShape = {
    val `type` = node.as[YScalar].text
    AvroTextTypeParser(`type`, None).parse()
  }
}
