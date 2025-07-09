package amf.shapes.internal.plugins.parser

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mimes, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.helper.SyncJsonLdSchemaParser
import amf.shapes.internal.spec.common.JSONSchemaUnspecifiedVersion
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaSettings
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.context.JsonSchemaSyntax

abstract class JsonSchemaBasedSpecParsePlugin extends AMFParsePlugin {

  protected val specSchema: JsonSchemaDocument

  protected def existsSpecEntry(document: Root): Boolean

  override val id: String = s"${spec.id.trim.toLowerCase()}-parse-plugin"

  // In a JSON/YAML instance is not possible to have references
  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def applies(element: Root): Boolean = existsSpecEntry(element)

  // In a JSON/YAML instance is not possible to have references
  override def validSpecsToReference: Seq[Spec] = Nil

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`, Mimes.`application/yaml`)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx    = createContext(document, ctx)
    val (unit, _) = SyncJsonLdSchemaParser.parse(specSchema, document, newCtx, document.location)
    unit.processingData.withSourceSpec(spec)
    unit.encodes.headOption.flatMap(_.location()).foreach(unit.withLocation)
    unit
  }

  private def createContext(document: Root, ctx: ParserContext): ShapeParserContext = {
    new ShapeParserContext(
      loc = document.location,
      refs = document.references,
      options = ctx.parsingOptions,
      wrapped = ctx.forLocation(document.location),
      settings = JsonSchemaSettings(JsonSchemaSyntax, JSONSchemaUnspecifiedVersion)
    )
  }
}
