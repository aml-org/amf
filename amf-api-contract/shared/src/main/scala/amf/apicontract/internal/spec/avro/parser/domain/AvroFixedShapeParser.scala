package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.yaml.model.YMap

case class AvroFixedShapeParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroComplexShapeParser(map) {
  override val shape: AnyShape =
    ScalarShape(Annotations(map)).withDataType(Xsd.base + "fixed", Annotations(map.entries.head))

  override def parseSpecificFields(): Unit = {
    map.key("size", (AnyShapeModel.Size in shape).allowingAnnotations)
  }
}
