package amf.mcp.internal.plugins.parse

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mcp, Mimes, Spec}
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.internal.spec.common.JSONSchemaUnspecifiedVersion
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaSettings
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.context.JsonSchemaSyntax

object MCPParsePlugin extends AMFParsePlugin {

  private lazy val mcpSchema = MCPSchemaLoader.doc

  override def spec: Spec = Mcp

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler =
    SimpleReferenceHandler // No references to resolve

  override def allowRecursiveReferences: Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def applies(element: Root): Boolean = true // We will not validate this

  override def validSpecsToReference: Seq[Spec] = Nil // No refences supported

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx    = createContext(document, ctx)
    val (unit, _) = SyncJsonLdSchemaParser.parse(mcpSchema, document.raw, newCtx, document.location)
    unit.processingData.withSourceSpec(Mcp)
    unit
  }

  private def createContext(document: Root, ctx: ParserContext): ShapeParserContext = {
    new ShapeParserContext(
      document.location,
      document.references,
      options = ctx.parsingOptions,
      ctx,
      settings = JsonSchemaSettings(JsonSchemaSyntax, JSONSchemaUnspecifiedVersion)
    )
  }
}
