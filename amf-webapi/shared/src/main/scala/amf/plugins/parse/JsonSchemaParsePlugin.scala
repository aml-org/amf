package amf.plugins.parse

import amf.client.remod.amfcore.config.ParsingOptions
import amf.client.remod.amfcore.plugins.{NormalPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.parse.AMFParsePlugin
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedReference, ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.remote.{JsonSchema, Vendor}
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.declaration.JSONSchemaUnspecifiedVersion
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaParsePlugin extends AMFParsePlugin {

  override val id: String = Vendor.JSONSCHEMA.name

  override def applies(element: Root): Boolean = false

  override def priority: PluginPriority = NormalPriority

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx = context(document.location, document.references, ctx.parsingOptions, ctx)
    val parsed =
      new JsonSchemaParser().parse(document, WebApiShapeParserContextAdapter(newCtx), ctx.parsingOptions)
    wrapInDataTypeFragment(document, parsed)
  }

  private def context(loc: String,
                      refs: Seq[ParsedReference],
                      options: ParsingOptions,
                      wrapped: ParserContext,
                      ds: Option[OasWebApiDeclarations] = None): JsonSchemaWebApiContext = {
    // todo: we can set this default as this plugin is hardcoded to not parse
    // todo 2: we should debate the default version to use in the Plugin if we are to use it.
    new JsonSchemaWebApiContext(loc, refs, wrapped, ds, options, JSONSchemaUnspecifiedVersion)
  }

  private def wrapInDataTypeFragment(document: Root, parsed: AnyShape): DataTypeFragment = {
    val unit: DataTypeFragment =
      DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
    unit.withRaw(document.raw)
    unit
  }

  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(JsonSchema.mediaType)

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validMediaTypesToReference: Seq[String] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = true
}
