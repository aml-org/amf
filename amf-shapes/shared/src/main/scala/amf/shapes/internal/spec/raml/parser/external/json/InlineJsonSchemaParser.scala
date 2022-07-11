package amf.shapes.internal.spec.raml.parser.external.json

import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.yaml.model.{YMapEntry, YNode}

object InlineJsonSchemaParser extends JsonParsing with ScopedJsonContext with ErrorShapeCreation {

  def parse(key: YNode, ast: YNode, origin: ValueAndOrigin)(implicit ctx: ShapeParserContext) = {
    val shape = parseInlineJsonShape(origin.text, key, origin.valueAST, ast)
    shape.annotations += ParsedJSONSchema(origin.text)
    shape
  }

  private def parseInlineJsonShape(text: String, key: YNode, valueAST: YNode, value: YNode)(implicit
      ctx: ShapeParserContext
  ): AnyShape = {

    val node        = getJsonParserFor(text, valueAST).document().node
    val schemaEntry = YMapEntry(key, node)
    val shape = withScopedContext(valueAST, schemaEntry) { jsonSchemaContext =>
      parse(value, schemaEntry)(jsonSchemaContext)
    }
    shape
  }

  private def parse(value: YNode, schemaEntry: YMapEntry)(implicit ctx: ShapeParserContext) = {
    OasTypeParser(schemaEntry, _ => {}, ctx.computeJsonSchemaVersion(schemaEntry.value))(ctx)
      .parse()
      .getOrElse(errorShape(value))
  }
}
