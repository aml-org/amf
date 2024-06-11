package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.YMapOps
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model.{YMap, YMapEntry}

abstract class AvroCollectionShapeParser[T <: AnyShape](map: YMap, membersKey: String)(implicit ctx: AvroSchemaContext)
    extends AvroShapeBaseParser(map) {
  val shape: T

  protected def setMembers(anyShape: AnyShape): Unit

  override def parse(): AnyShape = {
    map
      .key(membersKey)
      .map(parseMembers)
      .foreach(setMembers)
    shape
  }

  protected def parseMembers(e: YMapEntry): AnyShape = AvroScalarShapeParser(e.value.as[String], None).parse()
}
