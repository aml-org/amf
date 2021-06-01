package amf.cycle

import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.remote.Vendor
import amf.core.unsafe.PlatformSecrets
import amf.cycle.JsonSchemaTestEmitters._
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.render.JsonRender
import amf.client.remod.amfcore.config.{RenderOptions, ShapeRenderOptions => ImmutableShapeRenderOptions}
import amf.core.errorhandling.UnhandledErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaCycle extends AsyncFunSuite with PlatformSecrets with FileAssertionTest with JsonSchemaSuite {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "amf-client/shared/src/test/resources/cycle/jsonschema/"
  private val JSON                                         = "application/json"

  test("PoC Test") {
    cycle("schema.json", "schema.json", DRAFT_7_EMITTER, JSON)
  }

  test("Draft 2019-09 $defs") {
    cycle("draft-2019-09/defs.json", "draft-2019-09/cycled/defs.json", DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json",
          "draft-2019-09/cycled/duration-uuid-format.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019-09 to Json-LD duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json",
          "draft-2019-09/jsonld/duration-uuid-format.json",
          JsonLdEmitter,
          JSON)
  }

  test("Draft 2019-09 unevaluatedProperties") {
    cycle("draft-2019-09/unevaluatedProps-schema.json",
          "draft-2019-09/cycled/unevaluatedProps-schema.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019-09 unevaluatedProperties boolean") {
    cycle("draft-2019-09/unevaluatedProps-boolean.json",
          "draft-2019-09/cycled/unevaluatedProps-boolean.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019-09 unevaluatedItems boolean") {
    cycle("draft-2019-09/unevaluatedItems-boolean.json",
          "draft-2019-09/cycled/unevaluatedItems-boolean.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019-09 unevaluatedItems schema") {
    cycle("draft-2019-09/unevaluatedItems-schema.json",
          "draft-2019-09/cycled/unevaluatedItems-schema.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 7 required dependencies to Draft 7") {
    cycle("draft-7/required-dependencies.json", "draft-7/required-dependencies.json", DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 schema dependencies to Draft 7") {
    cycle("draft-7/schema-dependencies.json", "draft-7/schema-dependencies.json", DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 schema dependencies with $ref to JSON-LD") {
    cycle("draft-7/schema-dependencies-ref.json", "draft-7/jsonld/schema-dependencies-ref.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 7 required dependencies to json-ld") {
    cycle("draft-7/required-dependencies.json", "draft-7/jsonld/required-dependencies.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 7 schema dependencies to json-ld") {
    cycle("draft-7/schema-dependencies.json", "draft-7/jsonld/schema-dependencies.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 2019 dependents to Draft 2019") {
    cycle("draft-2019-09/schema-required-dependencies.json",
          "draft-2019-09/cycled/schema-required-dependencies.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019 dependents to JsonLd") {
    cycle("draft-2019-09/schema-required-dependencies.json",
          "draft-2019-09/jsonld/schema-required-dependencies.jsonld",
          JsonLdEmitter,
          JSON)
  }

  test("Draft 7 content schema to Draft 7") {
    cycle("draft-7/content.json", "draft-7/content.json", DRAFT_7_EMITTER, JSON)
  }

  test("Draft 7 content schema to JSONLD") {
    cycle("draft-7/content.json", "draft-7/jsonld/content.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 content schema to JSONLD") {
    cycle("draft-2019-09/content.json", "draft-2019-09/jsonld/content.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 content schema to Draft 2019") {
    cycle("draft-2019-09/content.json", "draft-2019-09/cycled/content.json", DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 $ref alongside facets to Draft 2019") {
    cycle("draft-2019-09/ref-alongside-facets.json",
          "draft-2019-09/cycled/ref-alongside-facets.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 2019-09 $ref with allOf to Draft 2019") {
    cycle("draft-2019-09/ref-with-allOf.json", "draft-2019-09/cycled/ref-with-allOf.json", DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 standalone $ref to Draft 2019") {
    cycle("draft-2019-09/standalone-ref.json", "draft-2019-09/cycled/standalone-ref.json", DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 min and max contains to Draft 2019") {
    cycle("draft-2019-09/min-and-max-contains.json",
          "draft-2019-09/cycled/min-and-max-contains.json",
          DRAFT_2019_09_EMITTER,
          JSON)
  }

  test("Draft 7 $id referencing test") {
    cycle("draft-7/with-id.json", "draft-7/jsonld/with-id.jsonld", JsonLdEmitter, JSON)
  }

  test("Draft 2019-09 $id and $anchor referencing test") {
    cycle("draft-2019-09/with-id.json", "draft-2019-09/jsonld/with-id.jsonld", JsonLdEmitter, JSON)
  }

  private def cycle(path: String,
                    golden: String,
                    emitter: SchemaEmitter,
                    mediatype: String = JSON): Future[Assertion] = {
    val finalPath   = basePath + path
    val finalGolden = basePath + golden
    val fragment =
      parseSchema(platform, finalPath, mediatype, WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()))
    val expected = emitter
      .emitSchema(fragment.bu.asInstanceOf[DataTypeFragment])
    writeTemporaryFile(finalGolden)(expected).flatMap(s => assertDifferences(s, finalGolden))
  }
}

sealed trait SchemaEmitter {
  def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String
}

object JsonLdEmitter extends SchemaEmitter {

  lazy private val options =
    RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
  lazy private val vendor = Vendor.AMF

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String = {
    AMFRenderer(fragment, vendor, options).renderToString
  }
}

object JsonSchemaTestEmitters {
  val DRAFT_2019_09_EMITTER: JsonSchemaTestEmitter = JsonSchemaTestEmitter(JSONSchemaDraft201909SchemaVersion)
  val DRAFT_7_EMITTER: JsonSchemaTestEmitter       = JsonSchemaTestEmitter(JSONSchemaDraft7SchemaVersion)
}

case class JsonSchemaTestEmitter(to: JSONSchemaVersion) extends SchemaEmitter {

  private val options =
    ImmutableShapeRenderOptions().withSchemaVersion(SchemaVersion.toClientOptions(to)).withCompactedEmission

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String = {
    val shape     = fragment.encodes
    val emitter   = JsonSchemaEmitter(shape, Seq(shape), options = options, errorHandler = UnhandledErrorHandler)
    val goldenDoc = emitter.emitDocument()
    JsonRender.render(goldenDoc)
  }
}
