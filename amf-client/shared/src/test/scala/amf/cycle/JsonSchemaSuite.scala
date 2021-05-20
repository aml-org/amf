package amf.cycle

import amf.client.remod.{AMFResult, ParseConfiguration}
import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.remote.Platform
import amf.core.validation.AMFValidationReport
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
                            eh: AMFErrorHandler = UnhandledErrorHandler): AMFResult = {
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
    val unit    = wrapInDataTypeFragment(root, parsed)
    AMFResult(unit, AMFValidationReport.forModel(unit, eh.getResults))
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
                                          ParserContext(config = ParseConfiguration(eh)),
                                          None,
                                          options,
                                          JSONSchemaDraft7SchemaVersion)
    WebApiShapeParserContextAdapter(ctx)
  }
}
