package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.yaml.model.{YMap, YMapEntry, YNode}

class AvroRecordParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeBaseParser(map) {
  private val shape = NodeShape(map)

  override def parse(): NodeShape = {
    map.key("name", (AnyShapeModel.Name in shape).allowingAnnotations)
    map.key("namespace", (AnyShapeModel.AvroNamespace in shape).allowingAnnotations)
    map.key("doc", (ShapeModel.Description in shape).allowingAnnotations)
    map.key("aliases", (AnyShapeModel.Aliases in shape).allowingAnnotations)
    map.key("fields", parseFieldsEntry)
    // todo: parse default
    shape
  }

  def parseFieldsEntry(e: YMapEntry): Unit = {
    val fields = e.value.as[Seq[YMap]].flatMap(parseField)
    shape.withProperties(fields)
  }

  def parseField(map: YMap): Option[PropertyShape] = {
    val maybeShape: Option[PropertyShape] = AvroRecordFieldParser(map).parse().map(buildProperty)
    maybeShape.foreach { p =>
      map.key("order", PropertyShapeModel.SerializationOrder in p)
    }
    maybeShape
  }

  def buildProperty(anyShape: AnyShape): PropertyShape =
    PropertyShape(Annotations.virtual()).withName("field").withRange(anyShape)

}

// todo: analyze defaulting to base shape parser instead
case class AvroRecordFieldParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeParser(map) {
  override def parseTypeEntry(value: YNode): Option[AnyShape] = {
    value.asOption[YMap] match {
      case Some(map) => AvroRecordFieldParser(map).parse()
      case _         => super.parseTypeEntry(value)
    }
  }
}
