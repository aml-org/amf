package amf.mcp.internal.plugins.parse

import amf.core.client.scala.adoption.IdAdopter
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.validation.AMFValidationResult
import amf.mcp.internal.plugins.parse.SyncJsonSchemaCompiler.{context, parseJson}
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument, JsonSchemaDocument}
import amf.shapes.internal.spec.jsonldschema.JsonLDSchemaParsePlugin

object SyncJsonLdSchemaParser {

  def parse(
      doc: JsonSchemaDocument,
      payload: String,
      ctx: ParserContext = context(JsonSchemaConfiguration.JsonSchema()),
      location: String = "root"
  ): (JsonLDInstanceDocument, Seq[AMFValidationResult]) = {
    val ast    = parseJson(payload, ctx.eh)
    val plugin = new JsonLDSchemaParsePlugin(doc)
    val parsed = plugin.parse(ast, ctx).asInstanceOf[JsonLDInstanceDocument]
    (adopt(parsed, location), ctx.eh.getResults)
  }

  private def adopt[T <: AmfObject](obj: T, location: String): T = {
    new IdAdopter(location).adoptFromRoot(obj)
    obj
  }
}
