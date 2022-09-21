package amf.shapes.internal.spec.raml.parser.external.json

import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.raml.parser.external.json.JsonSchemaContextAdapter.toSchemaContext
import org.yaml.model.{YMapEntry, YNode}

trait ScopedJsonContext {

  def withScopedContext[T](valueAST: YNode, schemaEntry: YMapEntry)(
      block: ShapeParserContext => T
  )(implicit ctx: ShapeParserContext): T = {
    val nextContext = getContext(valueAST, schemaEntry)
    val parsed      = block(nextContext)
    cleanGlobalSpace(nextContext)            // this works because globalSpace is mutable everywhere
    nextContext.removeLocalJsonSchemaContext // we reset the JSON schema context after parsing
    parsed
  }

  private def getContext(valueAST: YNode, schemaEntry: YMapEntry)(implicit ctx: ShapeParserContext) = {
    // we set the local schema entry to be able to resolve local $refs
    ctx.setJsonSchemaAST(schemaEntry.value)
    val context = toSchemaContext(ctx, valueAST)

    // TODO: find way to avoid doing this.
    /*
     * This is related to a bug that occurs when a RAML and a JSON Schema point to the same JSON Schema where the pointed JSON schema occupies the whole file (it isn't pointed at by a json pointer with uri fragment)
     */
    context.declarations.fragments =
      Map.empty // Leaving fragments empty to avoid regression. This is the behaviour that ended up occurring due to a bug.
    context
  }

  /** Clean from globalSpace the local references
    */
  private def cleanGlobalSpace(ctx: ShapeParserContext): Unit = {
    ctx.globalSpace.foreach { e =>
      val refPath = e._1.split("#").headOption.getOrElse("")
      if (refPath == ctx.getLocalJsonSchemaContext.get.sourceName) ctx.globalSpace.remove(e._1)
    }
  }
}
