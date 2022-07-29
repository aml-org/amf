package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDScalarElementBuilder
import org.yaml.model.YScalar

case class JsonLDScalarElementParser(scalar: YScalar)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDScalarElementBuilder](scalar)(ctx) {
  override def foldLeft(
      current: JsonLDScalarElementBuilder,
      other: JsonLDScalarElementBuilder
  ): JsonLDScalarElementBuilder = {
    current.merge(other)
  }

  override def unsupported(s: Shape): JsonLDScalarElementBuilder = JsonLDScalarElementBuilder.empty()

  override def parseNode(shape: Shape): JsonLDScalarElementBuilder = {}
}
