package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import org.yaml.model.YNode

case class AvroUnionShapeParser(members: Seq[YNode], node: YNode)(implicit ctx: AvroSchemaContext) {

  // todo: parse default, should be the default value of the first element of the union (usually null)
  def parse(): AnyShape = {
    val shape         = UnionShape(node).withName("union")
    val parsedMembers = members.map(node => AvroTextParser(node).parse())
    shape.withAnyOf(parsedMembers)
  }
}
