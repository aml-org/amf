package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.UnresolvedShape
import amf.shapes.internal.spec.ShapeParserContext
import org.yaml.model.YMapEntry

object JsonSchemaParsingHelper {
  def createTemporaryShape(adopt: Shape => Unit,
                           schemaEntry: YMapEntry,
                           ctx: ShapeParserContext,
                           fullRef: String): UnresolvedShape = {
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
