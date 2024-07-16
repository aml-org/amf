package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Properties
import org.yaml.model.{YMap, YMapEntry, YNode}

class AvroRecordParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroComplexShapeParser(map) {
  override val shape: NodeShape = NodeShape(map)

  override def parseSpecificFields(): Unit = {
    // todo: parse default
    map.key("fields", parseFieldsEntry)
  }

  private def parseFieldsEntry(e: YMapEntry): Unit = {
    val fields = e.value.as[Seq[YMap]].flatMap(parseField)
    shape.setWithoutId(Properties, AmfArray(fields, Annotations(e.value)), Annotations(e))
  }

  def parseField(map: YMap): Option[PropertyShape] = {
    val maybeShape =
      AvroRecordFieldParser(map)
        .parse()
        .map { s =>
          // add the map annotations + the avro type annotation to the PropertyShape wrapper
          var ann = Annotations(map)
          getAvroType(s).foreach(avroTypeAnnotation => ann = ann += avroTypeAnnotation)
          val p = PropertyShape(ann).withRange(s)
          p.setWithoutId(PropertyShapeModel.Range, s, s.annotations)
        }
    maybeShape.foreach { p =>
      map.key("name", AnyShapeModel.Name in p)
      map.key("aliases", AnyShapeModel.Aliases in p)
      map.key("doc", AnyShapeModel.Description in p)
      // todo: change to new field Order (being a string)
      map.key("order", PropertyShapeModel.SerializationOrder in p)
      map.key(
        "default",
        entry => {
          val dataNode = DataNodeParser(entry.value).parse()
          p.set(ShapeModel.Default, dataNode, Annotations(entry))
        }
      )
    }
    maybeShape
  }
}

case class AvroRecordFieldParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeParser(map) {
  override def parseTypeEntry(value: YNode): Option[AnyShape] = {
    value.asOption[YMap] match {
      case Some(map) => AvroRecordFieldParser(map).parse()
      case _         => super.parseTypeEntry(value)
    }
  }
}
