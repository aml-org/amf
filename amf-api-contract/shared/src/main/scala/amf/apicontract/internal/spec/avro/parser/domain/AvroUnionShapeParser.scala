package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import org.yaml.model.{YMap, YNode}

case class AvroUnionShapeParser(members: Seq[YNode], node: YNode)(implicit ctx: AvroWebAPIContext) {

  def parse(): AnyShape = {
    val shape         = UnionShape(node).withName("union")
    val parsedMembers = members.map(node => AvroTextParser(node).parse())
    shape.withAnyOf(parsedMembers)

  }

}
