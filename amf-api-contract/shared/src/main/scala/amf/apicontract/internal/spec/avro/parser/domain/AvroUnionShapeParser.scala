package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.UnionShape
import org.yaml.model.{YMap, YNode}

case class AvroUnionShapeParser(members: Seq[YNode], node: YNode)(implicit ctx: AvroSchemaContext)
    extends AvroComplexShapeParser(node.as[YMap]) {
  override val shape: UnionShape = UnionShape(node).withName("union")

  override def parseSpecificFields(): Unit = {
    val parsedMembers = members.map(node => AvroTextParser(node).parse())
    shape.withAnyOf(parsedMembers, Annotations(node))
  }
}
