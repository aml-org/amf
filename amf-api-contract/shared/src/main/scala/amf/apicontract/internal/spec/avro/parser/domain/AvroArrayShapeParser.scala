package amf.apicontract.internal.spec.avro.parser.domain
import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape}
import org.yaml.model.{YMap, YMapEntry}

case class AvroArrayShapeParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends AvroCollectionShapeParser[ArrayShape](map, "items") {
  // TODO: parse defaults
  override val shape: ArrayShape                    = ArrayShape(map)
  override def setMembers(anyShape: AnyShape): Unit = shape.withItems(anyShape)
  override def parseMembers(e: YMapEntry): AnyShape = AvroTextParser(e.value).parse()
}
