package amf.cycle

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.ShapeRenderOptions
import amf.core.model.domain.Shape
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft7SchemaVersion, JSONSchemaVersion, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.parser.JsonParser
import org.yaml.render.JsonRender

import scala.concurrent.Future

class JsonSchemaCycle extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {

  private val basePath = "amf-client/shared/src/test/resources/cycle/jsonschema/"
  private val JSON = "application/json"

  test("PoC Test") {
    cycle("schema.json", "schema.json", JSONSchemaDraft7SchemaVersion, JSONSchemaDraft7SchemaVersion, JSON)
  }

  test("Draft 2019-09 $defs") {
    cycle("draft-2019-09/defs.json", "draft-2019-09/defs.json", JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft201909SchemaVersion, JSON)
  }

  private def cycle(path: String, golden: String, from: JSONSchemaVersion, to: JSONSchemaVersion, mediatype: String = JSON): Future[Assertion] = {
    val finalPath = basePath + path
    val finalGolden = basePath + golden
    amf.core.AMF.init().flatMap { _ =>
      val shape = parseSchema(finalPath, mediatype, from)
      val expected = emitSchema(shape, to)
      writeTemporaryFile(finalGolden)(expected).flatMap(s => assertDifferences(s, finalGolden))
    }
  }

  private def parseSchema(path: String, mediatype: String, from: JSONSchemaVersion) = {
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
    val fragment = new JsonSchemaParser().parse(root, ParserContext(path, eh = UnhandledParserErrorHandler), ParsingOptions(), Some(from)).get
    fragment.encodes
  }

  private def emitSchema(shape: Shape, to: JSONSchemaVersion): String = {
    val emitter = JsonSchemaEmitter(shape, Seq(shape), options = ShapeRenderOptions().withSchemaVersion(SchemaVersion.toClientOptions(to)))
    val goldenDoc = emitter.emitDocument()
    JsonRender.render(goldenDoc)
  }
}
