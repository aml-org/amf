package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YScalar}

case class InnerShapeParser(
    key: String,
    field: Field,
    map: YMap,
    shape: Shape,
    adopt: Shape => Unit,
    version: SchemaVersion
)(implicit ctx: ShapeParserContext) {

  def parse(): Unit = {
    map.key(
      key,
      { entry =>
        adopt(shape)
        val shapeName = entry.key.as[YScalar].text
        OasTypeParser(YMapEntryLike.buildFakeMapEntry(entry), shapeName, item => Unit, version).parse() match {
          case Some(parsedShape) =>
            shape.setWithoutId(field, parsedShape, Annotations(entry.value))
          case _ => // ignore
        }
      }
    )
  }
}
