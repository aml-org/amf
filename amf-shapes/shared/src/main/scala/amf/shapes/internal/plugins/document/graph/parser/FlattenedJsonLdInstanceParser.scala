package amf.shapes.internal.plugins.document.graph.parser

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.ParseConfiguration
import amf.core.internal.plugins.document.graph.parser.{
  FlattenedGraphParser,
  FlattenedUnitGraphParser,
  GraphParserContext
}

class FlattenedJsonLdInstanceParser(startingPoint: String, overrideAliases: Map[String, String] = Map.empty)(implicit
    ctx: GraphParserContext
) extends FlattenedGraphParser(startingPoint, overrideAliases) {

  // TODO override sorted array parsing

}

class FlattenedJsonLdInstanceUnitGraphParser(overrideAliases: Map[String, String] = Map.empty)(implicit
    ctx: GraphParserContext
) extends FlattenedUnitGraphParser(overrideAliases) {

  override def parserProvider(
      rootId: String,
      overrideAliases: Map[String, String],
      ctx: GraphParserContext
  ): FlattenedGraphParser = new FlattenedJsonLdInstanceParser(rootId, overrideAliases)(ctx)

}

object FlattenedJsonLdInstanceUnitGraphParser {

  def apply(config: ParseConfiguration, aliases: Map[String, String]): FlattenedJsonLdInstanceUnitGraphParser =
    new FlattenedJsonLdInstanceUnitGraphParser(aliases)(new GraphParserContext(config = config))

  def canParse(document: SyamlParsedDocument, aliases: Map[String, String] = Map.empty): Boolean =
    FlattenedUnitGraphParser.canParse(document, aliases)

}
