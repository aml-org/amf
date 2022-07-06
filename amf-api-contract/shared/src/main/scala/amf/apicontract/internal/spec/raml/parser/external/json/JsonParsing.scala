package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.core.internal.parser.domain.JsonParserFactory
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YNode
import org.yaml.parser.JsonParser

trait JsonParsing {

  protected def getJsonParserFor(text: String, valueAST: YNode)(implicit ctx: WebApiContext): JsonParser = {
    JsonParserFactory.fromCharsWithSource(
      text,
      valueAST.value.sourceName,
      Position(valueAST.range.lineFrom, valueAST.range.columnFrom)
    )(ctx.eh)
  }

  protected def getJsonParserFor(text: String, valueAST: YNode, extLocation: Option[String])(implicit
      ctx: WebApiContext
  ): JsonParser = {
    val url = extLocation.flatMap(ctx.declarations.fragments.get).flatMap(_.location)
    url
      .map { JsonParserFactory.fromCharsWithSource(text, _)(ctx.eh) }
      .getOrElse(getJsonParserFor(text, valueAST))
  }
}
