package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ScalarShapeModel}
import org.yaml.model.YMap

case class AvroFixedShapeParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroComplexShapeParser(map) {
  override val shape: AnyShape = {
    ScalarShape(Annotations(map)).setWithoutId(
      ScalarShapeModel.DataType,
      AmfScalar(Xsd.base + "fixed", Annotations.inferred()),
      Annotations.inferred()
    )
  }

  override def parseSpecificFields(): Unit = {
    map.key("size", (AnyShapeModel.Size in shape).allowingAnnotations)
  }
}
