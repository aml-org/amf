package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.yaml.model.YMap

case class AvroFixedShapeParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeBaseParser(map) {

  override def parse(): AnyShape = {
    val datatype = Xsd.base + "fixed" // todo: is this correct? necessary? same question as scalar shape parser
    val shape    = ScalarShape(Annotations(map)).withDataType(datatype, Annotations(map.entries.head))
    map.key("name", AnyShapeModel.Name in shape)
    map.key("namespace", (AnyShapeModel.AvroNamespace in shape).allowingAnnotations)
    map.key("aliases", (AnyShapeModel.Aliases in shape).allowingAnnotations)
    map.key("doc", (AnyShapeModel.Description in shape).allowingAnnotations)
    map.key("size", (AnyShapeModel.Size in shape).allowingAnnotations)
    shape
  }
}
