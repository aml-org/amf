package amf.shapes.internal.spec.raml.parser.external.json

import amf.shapes.internal.spec.ShapeParserContext
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
    toSchemaContext(ctx, valueAST)
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
