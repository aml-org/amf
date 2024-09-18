package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model.{YMap, YMapEntry, YScalar}

abstract class AvroCollectionShapeParser[T <: AnyShape](map: YMap, membersKey: String)(implicit ctx: AvroSchemaContext)
    extends AvroComplexShapeParser(map) {
  val shape: T

  protected def setMembers(anyShape: AnyShape): Unit

  override def parse(): AnyShape = {
    map
      .key(membersKey)
      .map(parseMembers)
      .foreach(setMembers)
    parseDefault()
    shape
  }

  protected def parseMembers(e: YMapEntry): AnyShape = {
    e.value.value match {
      case scalar: YScalar =>
        val avroType    = scalar.text
        val parsedShape = AvroTextTypeParser(avroType, None).parse()
        annotatedAvroShape(parsedShape, avroType, e.value)
      case map: YMap => new AvroShapeParser(map).parse().getOrElse(AnyShape(Annotations(e.value)))
    }
  }
}
