package amf.shapes.internal.spec.jsonschema

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchema, Mimes, Spec}
import amf.shapes.internal.spec.common.JSONSchemaUnspecifiedVersion
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.common.reference.JsonRefsReferenceHandler
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaSettings
import amf.shapes.internal.spec.jsonschema.parser.document.JsonSchemaDocumentParser
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.context.JsonSchemaSyntax

object JsonSchemaParsePlugin extends AMFParsePlugin {

  override def spec: Spec = JsonSchema

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new JsonRefsReferenceHandler()

  override def allowRecursiveReferences: Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def applies(element: Root): Boolean = JsonSchemaEntry(element).nonEmpty

  override def validSpecsToReference: Seq[Spec] = super.validSpecsToReference

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx = createContext(document, ctx)
    JsonSchemaDocumentParser(document)(newCtx).parse()
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
