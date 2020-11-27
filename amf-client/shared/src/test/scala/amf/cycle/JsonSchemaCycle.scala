package amf.cycle

import amf.client.parse.DefaultParserErrorHandler
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.parser.{ParserContext, SchemaReference, SyamlParsedDocument}
import amf.core.remote.Vendor
import amf.core.unsafe.PlatformSecrets
import amf.cycle.JsonSchemaTestEmitters._
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft7SchemaVersion, JSONSchemaVersion, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.parser.JsonParser
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaCycle extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath = "amf-client/shared/src/test/resources/cycle/jsonschema/"
  private val JSON = "application/json"

  test("PoC Test") {
    cycle("schema.json", "schema.json", JSONSchemaDraft7SchemaVersion, DRAFT_7_EMITTER, JSON)
  }

  test("Draft 2019-09 $defs") {
    cycle("draft-2019-09/defs.json", "draft-2019-09/cycled/defs.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json", "draft-2019-09/cycled/duration-uuid-format.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 to Json-LD duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json", "draft-2019-09/jsonld/duration-uuid-format.json", JSONSchemaDraft201909SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 unevaluatedProperties") {
    cycle("draft-2019-09/unevaluatedProps-schema.json", "draft-2019-09/cycled/unevaluatedProps-schema.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 unevaluatedProperties boolean") {
    cycle("draft-2019-09/unevaluatedProps-boolean.json", "draft-2019-09/cycled/unevaluatedProps-boolean.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 unevaluatedItems boolean") {
    cycle("draft-2019-09/unevaluatedItems-boolean.json", "draft-2019-09/cycled/unevaluatedItems-boolean.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 unevaluatedItems schema") {
    cycle("draft-2019-09/unevaluatedItems-schema.json", "draft-2019-09/cycled/unevaluatedItems-schema.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 7 required dependencies to Draft 7") {
    cycle("draft-7/required-dependencies.json", "draft-7/required-dependencies.json", JSONSchemaDraft7SchemaVersion, DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 schema dependencies to Draft 7") {
    cycle("draft-7/schema-dependencies.json", "draft-7/schema-dependencies.json", JSONSchemaDraft7SchemaVersion, DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 schema dependencies with $ref to JSON-LD") {
    cycle("draft-7/schema-dependencies-ref.json", "draft-7/jsonld/schema-dependencies-ref.jsonld", JSONSchemaDraft7SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 7 required dependencies to json-ld") {
    cycle("draft-7/required-dependencies.json", "draft-7/jsonld/required-dependencies.jsonld", JSONSchemaDraft7SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 7 schema dependencies to json-ld") {
    cycle("draft-7/schema-dependencies.json", "draft-7/jsonld/schema-dependencies.jsonld", JSONSchemaDraft7SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 2019 dependents to Draft 2019") {
    cycle("draft-2019-09/schema-required-dependencies.json", "draft-2019-09/cycled/schema-required-dependencies.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019 dependents to JsonLd") {
    cycle("draft-2019-09/schema-required-dependencies.json", "draft-2019-09/jsonld/schema-required-dependencies.jsonld", JSONSchemaDraft201909SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 7 content schema to Draft 7") {
    cycle("draft-7/content.json", "draft-7/content.json", JSONSchemaDraft7SchemaVersion, DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 content schema to JSONLD") {
    cycle("draft-7/content.json", "draft-7/jsonld/content.jsonld", JSONSchemaDraft7SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 content schema to JSONLD") {
    cycle("draft-2019-09/content.json", "draft-2019-09/jsonld/content.jsonld", JSONSchemaDraft201909SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 content schema to Draft 2019") {
    cycle("draft-2019-09/content.json", "draft-2019-09/cycled/content.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 $ref alongside facets to Draft 2019") {
    cycle("draft-2019-09/ref-alongside-facets.json", "draft-2019-09/cycled/ref-alongside-facets.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 $ref with allOf to Draft 2019") {
    cycle("draft-2019-09/ref-with-allOf.json", "draft-2019-09/cycled/ref-with-allOf.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 standalone $ref to Draft 2019") {
    cycle("draft-2019-09/standalone-ref.json", "draft-2019-09/cycled/standalone-ref.json", JSONSchemaDraft201909SchemaVersion, DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 7 $id referencing test") {
    cycle("draft-7/with-id.json", "draft-7/jsonld/with-id.jsonld", JSONSchemaDraft7SchemaVersion, JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 $id and $anchor referencing test") {
    cycle("draft-2019-09/with-id.json", "draft-2019-09/jsonld/with-id.jsonld", JSONSchemaDraft201909SchemaVersion, JsonLdEmitter, JSON)
  }

  private def cycle(path: String, golden: String, from: JSONSchemaVersion, emitter: SchemaEmitter, mediatype: String = JSON): Future[Assertion] = {
    val finalPath = basePath + path
    val finalGolden = basePath + golden
    amf.core.AMF.init().flatMap { _ =>
      val fragment = parseSchema(finalPath, mediatype, from)
      emitter.emitSchema(fragment)
    }.flatMap { expected =>
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
    val options = ParsingOptions()
    val fragment = new JsonSchemaParser().parse(root, getBogusParserCtx(path, options), options, Some(from)).get
    fragment
  }

  def getBogusParserCtx(location: String, options: ParsingOptions): JsonSchemaWebApiContext =
    new JsonSchemaWebApiContext(location, Seq(), ParserContext(eh = UnhandledParserErrorHandler), None, options, JSONSchemaDraft7SchemaVersion)
}

sealed trait SchemaEmitter {
  def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): Future[String]
}

object JsonLdEmitter extends SchemaEmitter {

  lazy private val options = RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
  lazy private val vendor = Vendor.AMF

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): Future[String] = {
    AMFRenderer(fragment, vendor, options).renderToString
  }
}

object JsonSchemaTestEmitters {
  val DRAFT_2019_09_EMITTER: JsonSchemaTestEmitter = JsonSchemaTestEmitter(JSONSchemaDraft201909SchemaVersion)
  val DRAFT_7_EMITTER: JsonSchemaTestEmitter = JsonSchemaTestEmitter(JSONSchemaDraft7SchemaVersion)
}

case class JsonSchemaTestEmitter(to: JSONSchemaVersion) extends SchemaEmitter {

  private val options = ShapeRenderOptions().withSchemaVersion(SchemaVersion.toClientOptions(to)).withCompactedEmission

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): Future[String] = {
    val shape = fragment.encodes
    val emitter = JsonSchemaEmitter(shape, Seq(shape), options = options)
    val goldenDoc = emitter.emitDocument()
    Future.successful { JsonRender.render(goldenDoc) }
  }
}