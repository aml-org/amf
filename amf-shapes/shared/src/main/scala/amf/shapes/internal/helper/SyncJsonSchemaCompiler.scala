package amf.shapes.internal.helper

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.{AMFGraphConfiguration, AMFResult}
import amf.core.client.scala.adoption.IdAdopter
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParserContext, SyamlParsedDocument, UnspecifiedReference}
import amf.core.internal.parser.{ParseConfig, Root}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.internal.spec.jsonschema.JsonSchemaParsePlugin
import org.yaml.parser.{JsonParser, YamlParser}

object SyncJsonSchemaCompiler {

  def compile(schema: String): AMFResult = {
    val config      = JsonSchemaConfiguration.JsonSchema()
    val ctx         = context(config)
    val ast         = parseJson(schema, ctx.eh)
    val parsed      = adopt(JsonSchemaParsePlugin.parse(ast, ctx))
    val transformed = config.baseUnitClient().transform(parsed, PipelineId.Editing)
    transformed.copy(results = ctx.eh.getResults ++ transformed.results)
  }

  private def adopt(unit: BaseUnit): BaseUnit = {
    new IdAdopter("root").adoptFromRoot(unit)
    unit
  }

  def context(config: AMFGraphConfiguration): ParserContext = {
    ParserContext("", config = ParseConfig(config))
  }

  def parseString(content: String, eh: AMFErrorHandler, location: String = ""): Root = {
    if (isJson(content)) {
      parseJson(content, eh, location)
    } else {
      parseYaml(content, eh, location)
    }
  }

  def parseJson(content: String, eh: AMFErrorHandler, location: String = ""): Root = {
    val json = JsonParser(content)(new SyamlAMFErrorHandler(eh)).document()
    Root(
      parsed = SyamlParsedDocument(json),
      location = location,
      mediatype = Mimes.`application/json`,
      references = Seq.empty,
      referenceKind = UnspecifiedReference,
      raw = content
    )
  }

  def parseYaml(content: String, eh: AMFErrorHandler, location: String = ""): Root = {
    val yaml = YamlParser(content)(new SyamlAMFErrorHandler(eh)).document()
    Root(
      parsed = SyamlParsedDocument(yaml),
      location = location,
      mediatype = Mimes.`application/yaml`,
      references = Seq.empty,
      referenceKind = UnspecifiedReference,
      raw = content
    )
  }

  private def isJson(content: String): Boolean = content.startsWith("{") || content.startsWith("[")
}
