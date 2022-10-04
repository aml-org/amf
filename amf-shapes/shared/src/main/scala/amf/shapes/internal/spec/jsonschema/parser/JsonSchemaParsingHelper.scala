package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.UnresolvedShape
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import org.yaml.model.YPart

object JsonSchemaParsingHelper {
  def createTemporaryShape(
      adopt: Shape => Unit,
      schemaEntry: YPart,
      ctx: ShapeParserContext,
      fullRef: String
  ): UnresolvedShape = {
    val tmpShape =
      UnresolvedShape(fullRef, schemaEntry)
        .withName(fullRef)
        .withId(fullRef)
        .withSupportsRecursion(true)
    tmpShape.unresolved(fullRef, Nil, Some(schemaEntry.location))(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    ctx.registerJsonSchema(fullRef, tmpShape)
    tmpShape
  }
}
