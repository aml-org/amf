package amf.plugins.parse

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchema, Vendor}
import amf.shapes.internal.spec.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.apicontract.model.DataTypeFragment
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasWebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.declaration.JSONSchemaUnspecifiedVersion
import amf.plugins.document.apicontract.parser.spec.jsonschema.JsonSchemaParser

import amf.client.exported.ProvidedMediaType
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
  override def mediaTypes: Seq[String] = Seq(ProvidedMediaType.JsonSchema)

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validMediaTypesToReference: Seq[String] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = true
}
