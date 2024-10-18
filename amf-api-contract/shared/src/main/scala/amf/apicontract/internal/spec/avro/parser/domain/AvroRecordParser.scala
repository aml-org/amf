package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Properties
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

class AvroRecordParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroComplexShapeParser(map) {
  override val shape: NodeShape = NodeShape(map)

  override def parseSpecificFields(): Unit = map.key("fields", parseFieldsEntry)

  private def parseFieldsEntry(e: YMapEntry): Unit = {
    val fields = e.value.as[Seq[YMap]].map(parseField)
    shape.setWithoutId(Properties, AmfArray(fields, Annotations(e.value)), Annotations(e))
  }

  private def parsePropertyRange(p: PropertyShape, map: YMap, ann: Annotations): Unit = {
    AvroRecordFieldParser(map).parse() match {
      case Some(fieldShape) =>
        p.setWithoutId(PropertyShapeModel.Range, fieldShape, Annotations.inferred())
      case None =>
        p.setWithoutId(PropertyShapeModel.Range, AnyShape(ann), Annotations.inferred())
    }
  }

  def parseField(map: YMap): PropertyShape = {
    val ann = Annotations(map)
    val p   = PropertyShape(ann)
    parsePropertyRange(p, map, ann)
    map.key("name", AnyShapeModel.Name in p)
    map.key("aliases", AnyShapeModel.Aliases in p)
    map.key("doc", AnyShapeModel.Description in p)
    map
      .key("order")
      .foreach(f =>
        f.value.value match {
          case scalar: YScalar =>
            val orderIntMapping = AvroFieldOrder.fromString(scalar.text)
            p.set(
              PropertyShapeModel.SerializationOrder,
              AmfScalar(orderIntMapping, Annotations(f.value)),
              Annotations(f)
            )
          case _ =>
        }
      )
    super.parseDefault(map, p.range)
    // the validator treats all record fields as required
    p.setWithoutId(PropertyShapeModel.MinCount, AmfScalar(1), Annotations.synthesized())
    p
  }
}

object AvroFieldOrder extends Enumeration {
  private type SortOrder = Value
  private val Ascending: Value  = Value(1, "ascending")
  private val Ignore: Value     = Value(0, "ignore")
  private val Descending: Value = Value(-1, "descending")

  val default: SortOrder = Ascending

  def fromString(order: String): Int = order.toLowerCase match {
    case "ascending"  => Ascending.id
    case "descending" => Descending.id
    case "ignore"     => Ignore.id
    case _            => default.id
  }

  def fromInt(id: Int): String = id match {
    case 1  => Ascending.toString
    case 0  => Ignore.toString
    case -1 => Descending.toString
    case _  => default.toString
  }
}

case class AvroRecordFieldParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroShapeParser(map) {
  override def parseTypeEntry(value: YNode, isRoot: Boolean = false): Option[AnyShape] = {
    value.asOption[YMap] match {
      case Some(map) => AvroRecordFieldParser(map).parse()
      case _         => super.parseTypeEntry(value)
    }
  }
}
