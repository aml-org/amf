package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import amf.shapes.internal.domain.metamodel.UnionShapeModel
import org.yaml.model.{YMap, YNode, YSequence, YValue}

case class AvroUnionShapeParser(members: Seq[YNode], node: YNode)(implicit ctx: AvroSchemaContext)
    extends AvroComplexShapeParser(node.as[YMap])
    with AvroUnionLikeShapeParser {
  override val shape: UnionShape = UnionShape(node).withSynthesizeName("union")

  override def parseSpecificFields(): Unit = {
    val parsedMembers = parseMembers(members)
    shape.setWithoutId(UnionShapeModel.AnyOf, AmfArray(parsedMembers, Annotations(node)), Annotations(node))
  }
}

case class AvroInlineUnionShapeParser(seq: YSequence)(implicit ctx: AvroSchemaContext)
    extends AvroUnionLikeShapeParser {

  def parse(): AnyShape = {
    val shape: UnionShape = UnionShape(seq).withSynthesizeName("union")
    val parsedMembers     = parseMembers(seq.nodes)
    shape.setWithoutId(UnionShapeModel.AnyOf, AmfArray(parsedMembers, Annotations(seq)), Annotations(seq))
  }

}

trait AvroUnionLikeShapeParser {
  def parseMembers(members: Seq[YNode])(implicit ctx: AvroSchemaContext): Seq[AnyShape] = {
    members.map(node => AvroInlineTypeParser(node).parse())
  }
}
