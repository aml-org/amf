package amf.apicontract.internal.spec.jsonschema

import amf.apicontract.internal.plugins.SpecAwareParsePlugin
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.common.reference.ApiReferenceHandler
import amf.apicontract.internal.spec.jsonschema.parser.document.JsonSchemaDocumentParser
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchema, Mimes, Spec}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.JSONSchemaUnspecifiedVersion

object JsonSchemaParsePlugin extends SpecAwareParsePlugin {

  override def spec: Spec = JsonSchema

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new ApiReferenceHandler(id)

  override def allowRecursiveReferences: Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def applies(element: Root): Boolean = JsonSchemaEntry(element).nonEmpty

  override def validSpecsToReference: Seq[Spec] = super.validSpecsToReference

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx = createContext(document, ctx)
    restrictCrossSpecReferences(document, newCtx.eh)
    JsonSchemaDocumentParser(document)(newCtx).parse()
  }

  private def createContext(document: Root, ctx: ParserContext): ShapeParserContext = {
    val context = new JsonSchemaWebApiContext(
      document.location,
      document.references,
      ctx,
      None,
      options = ctx.parsingOptions,
      defaultSchemaVersion = JSONSchemaUnspecifiedVersion
    )
    WebApiShapeParserContextAdapter(context)
  }
}
