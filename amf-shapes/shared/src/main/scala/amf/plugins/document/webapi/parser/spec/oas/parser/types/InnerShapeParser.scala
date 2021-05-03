package amf.plugins.document.webapi.parser.spec.oas.parser.types

import amf.core.metamodel.Field
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.parser.ShapeParserContext
import amf.plugins.document.webapi.parser.spec.declaration.{OasTypeParser, SchemaVersion}
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
        OasTypeParser(entry, item => item.adopted(shape.id + s"/$key"), version).parse() match {
          case Some(parsedShape) =>
            shape.set(field, parsedShape, Annotations(entry.value))
          case _ => // ignore
        }
      }
    )
  }
}
