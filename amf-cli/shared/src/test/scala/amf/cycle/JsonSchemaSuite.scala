package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.document.DataTypeFragment
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.jsonschema.JsonSchemaWebApiContext
import amf.core.client.scala.AMFResult
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.internal.parser.{ParseConfiguration, Root}
import amf.core.internal.remote.Platform
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser
import org.yaml.parser.JsonParser

trait JsonSchemaSuite {

  protected def parseSchema(platform: Platform,
                            path: String,
                            mediatype: String,
                            amfConfig: AMFConfiguration): AMFResult = {
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
    val eh      = amfConfig.errorHandlerProvider.errorHandler()
    val parsed  = new JsonSchemaParser().parse(root, getBogusParserCtx(path, options, eh), options, None)
    val unit    = wrapInDataTypeFragment(root, parsed)
    AMFResult(unit, eh.getResults)
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