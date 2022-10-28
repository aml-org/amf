package amf.shapes.internal.spec.raml.parser.external.json

import amf.core.internal.parser.domain.JsonParserFactory
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YNode
import org.yaml.parser.JsonParser

trait JsonParsing {

  protected def getJsonParserFor(text: String, valueAST: YNode)(implicit ctx: ShapeParserContext): JsonParser = {
    JsonParserFactory.fromCharsWithSource(
      text,
      valueAST.value.sourceName,
      Position(valueAST.range.lineFrom, valueAST.range.columnFrom),
      ctx.options.getMaxJsonYamlDepth
    )(ctx.eh)
  }

  protected def getJsonParserFor(text: String, valueAST: YNode, extLocation: Option[String])(implicit
      ctx: ShapeParserContext
  ): JsonParser = {
    val url = extLocation.flatMap(ctx.fragments.get).flatMap(_.location)
    url
      .map { JsonParserFactory.fromCharsWithSource(text, _, ctx.options.getMaxJsonYamlDepth)(ctx.eh) }
      .getOrElse(getJsonParserFor(text, valueAST))
  }
}
