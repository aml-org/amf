package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.apicontract.internal.spec.common.parser._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps

class AvroRecordParser(map: YMap)(implicit ctx: AvroWebAPIContext) extends AvroShapeBaseParser(map) {

  private val shape = NodeShape(map)

  override def parseShape(): NodeShape = {

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

  def parseField(map: YMap) = {
    val maybeShape: Option[PropertyShape] = AvroRecordFieldParser(map).parse().map(buildProperty)
    maybeShape.foreach { p =>
      map.key("order", PropertyShapeModel.SerializationOrder in p)
    }
    maybeShape
  }

  def buildProperty(anyShape: AnyShape): PropertyShape =
    PropertyShape(Annotations.virtual()).withName("field").withRange(anyShape)

}

case class AvroRecordFieldParser(map: YMap)(implicit ctx: AvroWebAPIContext) extends AvroShapeParser(map) {
  override def parseTypeEntry(value: YNode): Option[AnyShape] = {
    value.asOption[YMap] match {
      case Some(map) => AvroRecordFieldParser(map).parse()
      case _         => super.parseTypeEntry(value)
    }
  }
}
