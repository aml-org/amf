package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import org.yaml.model.{YMap, YMapEntry}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.apicontract.internal.spec.common.parser._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps

class AvroRecordParser(map: YMap)(implicit ctx: AvroWebAPIContext) extends QuickFieldParserOps {

  private val shape = NodeShape(map)

  def parse() = {
    map.key("name", (ShapeModel.Name in shape).allowingAnnotations)

    map.key("namespace", (ShapeModel.DisplayName in shape).allowingAnnotations) // TODO: review
    map.key("doc", (ShapeModel.Description in shape).allowingAnnotations)
    // TODO : where store aliases?
    // map.key("aliases", (ShapeModel.Tag in shape).allowingAnnotations)

    map.key("fields", parseFieldsEntry)
    shape
  }

  def parseFieldsEntry(e: YMapEntry) = {
    val fields = e.value.as[Seq[YMap]].flatMap(parseField)
    shape.withProperties(fields)
  }

  def parseField(map: YMap) = new AvroShapeParser(map).parse().map(buildProperty)

  def buildProperty(anyShape: AnyShape): PropertyShape =
    PropertyShape(Annotations.virtual()).withName("field").withRange(anyShape)
}
