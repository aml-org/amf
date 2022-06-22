package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.common.parser.{WebApiContext, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.raml.parser.external.RamlJsonSchemaParser.{errorShape, withScopedContext}
import amf.core.internal.parser.domain.JsonParserFactory
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.mulesoft.lexer.Position
import org.yaml.model.{YMapEntry, YNode}
import org.yaml.parser.JsonParser

object InlineJsonSchemaParser {

  def parse(key: YNode, ast: YNode, origin: ValueAndOrigin)(implicit ctx: WebApiContext) = {
    val shape = parseInlineJsonShape(origin.text, key, origin.valueAST, ast)
    shape.annotations += ParsedJSONSchema(origin.text)
    shape
  }

  private def parseInlineJsonShape(text: String, key: YNode, valueAST: YNode, value: YNode)(implicit
      ctx: WebApiContext
  ): AnyShape = {

    val node        = jsonParser(text, valueAST).document().node
    val schemaEntry = YMapEntry(key, node)
    val shape = withScopedContext(valueAST, schemaEntry) { jsonSchemaContext =>
      val jsonSchemaShapeContext = WebApiShapeParserContextAdapter(jsonSchemaContext)
      parse(value, schemaEntry, jsonSchemaShapeContext)
    }
    shape
  }

  private def jsonParser(text: String, valueAST: YNode)(implicit ctx: WebApiContext): JsonParser = {
    JsonParserFactory.fromCharsWithSource(
      text,
      valueAST.value.sourceName,
      Position(valueAST.range.lineFrom, valueAST.range.columnFrom)
    )(ctx.eh)
  }

  private def parse(value: YNode, schemaEntry: YMapEntry, jsonSchemaContext: ShapeParserContext)(implicit
      ctx: WebApiContext
  ) = {
    OasTypeParser(schemaEntry, _ => {}, ctx.computeJsonSchemaVersion(schemaEntry.value))(jsonSchemaContext)
      .parse()
      .getOrElse(errorShape(value))
  }
}
