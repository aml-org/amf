package amf.apicontract.internal.spec.avro.parser.domain
import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape}
import amf.shapes.internal.domain.metamodel.ArrayShapeModel
import org.yaml.model.{YMap, YMapEntry}

case class AvroArrayShapeParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends AvroCollectionShapeParser[ArrayShape](map, "items") {
  override val shape: ArrayShape = ArrayShape(map)
  override def setMembers(anyShape: AnyShape): Unit =
    shape.setWithoutId(ArrayShapeModel.Items, anyShape, Annotations.inferred())

  override def parseMembers(e: YMapEntry): AnyShape = AvroTextParser(e.value).parse()

  override def parseSpecificFields(): Unit = {}
}
