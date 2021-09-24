package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaVersion
import org.yaml.model.YMap

case class InnerShapeParser(key: String,
                            field: Field,
                            map: YMap,
                            shape: Shape,
                            adopt: Shape => Unit,
                            version: SchemaVersion)(implicit ctx: ShapeParserContext) {

  def parse(): Unit = {
    map.key(
      key, { entry =>
        adopt(shape)
        OasTypeParser(entry, item => Unit, version).parse() match {
          case Some(parsedShape) =>
            shape.setWithoutId(field, parsedShape, Annotations(entry.value))
          case _ => // ignore
        }
      }
    )
  }
}
