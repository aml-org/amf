package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import org.yaml.model.{YMap, YMapEntry, YNode}

class AvroRecordParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroComplexShapeParser(map) {
  override val shape: NodeShape = NodeShape(map)

  override def parseSpecificFields(): Unit = {
    // todo: parse default
    map.key("fields", parseFieldsEntry)
  }

  private def parseFieldsEntry(e: YMapEntry): Unit = {
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

  private def buildProperty(anyShape: AnyShape): PropertyShape =
    PropertyShape(Annotations.virtual()).withName("field").withRange(anyShape)
}

case class AvroRecordFieldParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeParser(map) {
  override def parseTypeEntry(value: YNode, isRecordField: Boolean = false): Option[AnyShape] = {
    value.asOption[YMap] match {
      case Some(map) => AvroRecordFieldParser(map).parse()
      case _         => super.parseTypeEntry(value, isRecordField = true)
    }
  }
}
