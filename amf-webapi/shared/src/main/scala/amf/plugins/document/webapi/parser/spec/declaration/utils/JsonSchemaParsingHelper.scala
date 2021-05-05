package amf.plugins.document.webapi.parser.spec.declaration.utils

import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.domain.shapes.models.UnresolvedShape
import org.yaml.model.YMapEntry

object JsonSchemaParsingHelper {
  def createTemporaryShape(adopt: Shape => Unit,
                           schemaEntry: YMapEntry,
                           ctx: OasLikeWebApiContext,
                           fullRef: String): UnresolvedShape = {
    val tmpShape =
      UnresolvedShape(fullRef, schemaEntry)
        .withName(fullRef)
        .withId(fullRef)
        .withSupportsRecursion(true)
    tmpShape.unresolved(fullRef, schemaEntry)(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    ctx.registerJsonSchema(fullRef, tmpShape)
    tmpShape
  }
}
