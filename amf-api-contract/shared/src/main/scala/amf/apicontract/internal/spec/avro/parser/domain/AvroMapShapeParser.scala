package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import org.yaml.model.YMap

case class AvroMapShapeParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends AvroCollectionShapeParser[NodeShape](map, "values") {
  // TODO: parse defaults
  override val shape: NodeShape                     = NodeShape(map)
  override def setMembers(anyShape: AnyShape): Unit = shape.withAdditionalPropertiesSchema(anyShape)
}
