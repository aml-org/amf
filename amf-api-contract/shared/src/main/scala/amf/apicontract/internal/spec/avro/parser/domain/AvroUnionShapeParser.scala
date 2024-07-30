package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.domain.metamodel.UnionShapeModel
import org.yaml.model.{YMap, YNode}

case class AvroUnionShapeParser(members: Seq[YNode], node: YNode)(implicit ctx: AvroSchemaContext)
    extends AvroComplexShapeParser(node.as[YMap]) {
  override val shape: UnionShape = UnionShape(node).withSynthesizeName("union")

  override def parseSpecificFields(): Unit = {
    val parsedMembers = members.map(node => AvroTextParser(node).parse())
    shape.setWithoutId(UnionShapeModel.AnyOf, AmfArray(parsedMembers, Annotations(node)), Annotations(node))
  }
}
