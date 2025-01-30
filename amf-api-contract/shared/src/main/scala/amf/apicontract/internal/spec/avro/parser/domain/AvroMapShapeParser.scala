package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel.AdditionalPropertiesSchema
import org.yaml.model.YMap

case class AvroMapShapeParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends AvroCollectionShapeParser[NodeShape](map, "values") {
  override val shape: NodeShape = NodeShape(map)
  override def setMembers(anyShape: AnyShape): Unit =
    shape.setWithoutId(AdditionalPropertiesSchema, anyShape, Annotations(map))

  override def parseSpecificFields(): Unit = {}
}
