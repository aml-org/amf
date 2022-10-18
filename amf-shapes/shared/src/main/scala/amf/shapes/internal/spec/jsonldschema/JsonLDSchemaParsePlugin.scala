package amf.shapes.internal.spec.jsonldschema

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{
  ParserContext,
  ReferenceHandler,
  SimpleReferenceHandler,
  SyamlParsedDocument
}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonLDSchema, Mimes, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.jsonschema.JsonSchemaParsePlugin

class JsonLDSchemaParsePlugin(jsonSchema: JsonSchemaDocument) extends AMFParsePlugin {
  override def spec: Spec = JsonLDSchema

  override def parse(document: Root, ctx: ParserContext): BaseUnit =
    new JsonLDSchemaNativeParser(ctx.eh).parse(document, jsonSchema)

  /** media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(Mimes.`application/schema+ld+json`)

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = false

  override def applies(element: Root): Boolean =
    element.parsed.isInstanceOf[SyamlParsedDocument] && !JsonSchemaParsePlugin.applies(element)

  override def priority: PluginPriority = NormalPriority
}
