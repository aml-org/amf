package amf.cycle

import amf.client.remod.amfcore.config.RenderOptions
import amf.client.environment.ShapesConfiguration
import amf.client.remod.ParseConfiguration
import amf.core.Root
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.remote.{JsonSchemaDialect, Platform, Vendor}
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.plugins.parser.JsonSchemaDialectParsePlugin
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.parser.JsonParser

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaDialectCycleTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "amf-cli/shared/src/test/resources/cycle/jsonschemadialects/"

  test("can generate a dialect from a semantic JSON schema") {
    cycle("simple.json", "simple.yaml")
  }


  def parseSchema(platform: Platform, path: String, mediatype: String): BaseUnit = {
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
    val eh      = ShapesConfiguration.predefined().errorHandlerProvider.errorHandler()
    val ctx     = ParserContext(config = ParseConfiguration(eh))
    JsonSchemaDialectParsePlugin.parse(root, ctx)
  }

  private def cycle(path: String,
                    golden: String): Future[Assertion] = {
    val finalPath   = basePath + path
    val finalGolden = basePath + golden
    val dialect = parseSchema(platform, finalPath, JsonSchemaDialect.mediaType)
    val expected = emit(dialect)
    writeTemporaryFile(finalGolden)(expected).flatMap(s => assertDifferences(s, finalGolden))
  }


  private def emit(unit: BaseUnit)(implicit executionContext: ExecutionContext): String = {
    val options = RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
    new AMFRenderer(unit, Vendor.AML, options, None).renderToString
  }
}
