package amf.cycle

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.declaration.JSONSchemaDraft7SchemaVersion
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import amf.plugins.document.webapi.parser.{ShapeParserContext, WebApiShapeParserContextAdapter}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.parser.JsonParser

trait JsonSchemaSuite {

  protected def parseSchema(platform: Platform,
                            path: String,
                            mediatype: String,
                            eh: AMFErrorHandler = UnhandledErrorHandler) = {
    val content  = platform.fs.syncFile(path).read().toString
    val document = JsonParser.withSource(content, path).document()
    val root = Root(
      SyamlParsedDocument(document),
      path,
      mediatype,
      Seq(),
      SchemaReference,
      content
    )
    val options = ParsingOptions()
    val parsed  = new JsonSchemaParser().parse(root, getBogusParserCtx(path, options, eh), options, None)
    wrapInDataTypeFragment(root, parsed)
  }

  private def wrapInDataTypeFragment(document: Root, parsed: AnyShape): DataTypeFragment = {
    val unit: DataTypeFragment =
      DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
    unit.withRaw(document.raw)
    unit
  }

  private def getBogusParserCtx(location: String, options: ParsingOptions, eh: AMFErrorHandler): ShapeParserContext = {
    val ctx = new JsonSchemaWebApiContext(location,
                                          Seq(),
                                          ParserContext(eh = eh),
                                          None,
                                          options,
                                          JSONSchemaDraft7SchemaVersion)
    WebApiShapeParserContextAdapter(ctx)
  }
}
