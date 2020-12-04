package amf.cycle

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, JSONSchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import org.yaml.parser.JsonParser

trait JsonSchemaSuite {

  protected def parseSchema(platform: Platform, path: String, mediatype: String, eh: ParserErrorHandler = UnhandledParserErrorHandler) = {
    val content = platform.fs.syncFile(path).read().toString
    val document = JsonParser.withSource(content, path).document()
    val root =  Root(
      SyamlParsedDocument(document),
      path,
      mediatype,
      Seq(),
      SchemaReference,
      content
    )
    val options = ParsingOptions()
    val fragment = new JsonSchemaParser().parse(root, getBogusParserCtx(path, options, eh), options, None).get
    fragment
  }

  private def getBogusParserCtx(location: String, options: ParsingOptions, eh: ParserErrorHandler): JsonSchemaWebApiContext =
    new JsonSchemaWebApiContext(location, Seq(), ParserContext(eh = eh), None, options, JSONSchemaDraft7SchemaVersion)
}
