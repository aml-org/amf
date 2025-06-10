package amf.mcp.internal.plugins.parse

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.adoption.IdAdopter
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParserContext, SyamlParsedDocument, UnspecifiedReference}
import amf.core.internal.parser.{ParseConfig, Root}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.internal.spec.jsonschema.JsonSchemaParsePlugin
import org.yaml.parser.JsonParser

object SyncJsonSchemaCompiler {

  def compile(schema: String) = {
    val config      = JsonSchemaConfiguration.JsonSchema()
    val ctx         = context(config)
    val ast         = parseJson(schema, ctx.eh)
    val parsed      = adopt(JsonSchemaParsePlugin.parse(ast, ctx))
    val transformed = config.baseUnitClient().transform(parsed, PipelineId.Editing)
    transformed.copy(results = ctx.eh.getResults ++ transformed.results)
  }

  private def adopt(unit: BaseUnit) = {
    new IdAdopter("root").adoptFromRoot(unit)
    unit
  }

  def context(config: AMFGraphConfiguration): ParserContext = {
    ParserContext("", config = ParseConfig(config))
  }

  def parseJson(schema: String, eh: AMFErrorHandler) = {
    val json = JsonParser(schema)(new SyamlAMFErrorHandler(eh)).document()
    Root(
        SyamlParsedDocument(json),
        "",
        Mimes.`application/json`,
        Seq.empty,
        UnspecifiedReference,
        ""
    )
  }
}
