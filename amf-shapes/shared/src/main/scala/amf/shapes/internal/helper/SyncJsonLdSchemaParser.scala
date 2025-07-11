package amf.shapes.internal.helper

import amf.core.client.scala.adoption.IdAdopter
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.parser.Root
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument, JsonSchemaDocument}
import amf.shapes.internal.helper.SyncJsonSchemaCompiler.{context, parseJson, parseString}
import amf.shapes.internal.spec.jsonldschema.JsonLDSchemaParsePlugin

object SyncJsonLdSchemaParser {

  def parse(
             doc: JsonSchemaDocument,
             payload: String,
             ctx: ParserContext,
             location: String
           ): (JsonLDInstanceDocument, Seq[AMFValidationResult]) = {
    val ast = parseString(payload, ctx.eh, location)
    parse(doc, ast, ctx, location)
  }

  def parse(
             doc: JsonSchemaDocument,
             payloadAst: Root,
             ctx: ParserContext = context(JsonSchemaConfiguration.JsonSchema()),
             location: String = "root"
           ): (JsonLDInstanceDocument, Seq[AMFValidationResult]) = {
    val plugin = new JsonLDSchemaParsePlugin(doc)
    val parsed = plugin.parse(payloadAst, ctx).asInstanceOf[JsonLDInstanceDocument]
    (adopt(parsed, location), ctx.eh.getResults)
  }

  private def adopt[T <: AmfObject](obj: T, location: String): T = {
    new IdAdopter(location).adoptFromRoot(obj)
    obj
  }
}
