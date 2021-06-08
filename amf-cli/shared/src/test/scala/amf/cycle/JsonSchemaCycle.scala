package amf.cycle

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.document.DataTypeFragment
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.plugins.render.EmptyRenderConfiguration
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.cycle.JsonSchemaTestEmitters._
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaEmitter
import amf.testing.HintProvider
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaCycle extends AsyncFunSuite with PlatformSecrets with FileAssertionTest with JsonSchemaSuite {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "amf-cli/shared/src/test/resources/cycle/jsonschema/"

  test("PoC Test") {
    cycle("schema.json", "schema.json", DRAFT_7_EMITTER, `application/json`)
  }

  test("Draft 2019-09 $defs") {
    cycle("draft-2019-09/defs.json", "draft-2019-09/cycled/defs.json", DRAFT_2019_09_EMITTER, `application/json`)
  }

  test("HERE_HERE Draft 2019-09 semantics") {
    cycle("draft-2019-09/semantics.json", "draft-2019-09/cycled/semantics.json", DRAFT_2019_09_EMITTER, JSON)
  }

  test("Draft 2019-09 duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json",
          "draft-2019-09/cycled/duration-uuid-format.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 to Json-LD duration and uuid formats") {
    cycle("draft-2019-09/duration-uuid-format.json",
          "draft-2019-09/jsonld/duration-uuid-format.json",
          JsonLdEmitter,
          `application/json`)
  }

  test("Draft 2019-09 unevaluatedProperties") {
    cycle("draft-2019-09/unevaluatedProps-schema.json",
          "draft-2019-09/cycled/unevaluatedProps-schema.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 unevaluatedProperties boolean") {
    cycle("draft-2019-09/unevaluatedProps-boolean.json",
          "draft-2019-09/cycled/unevaluatedProps-boolean.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 unevaluatedItems boolean") {
    cycle("draft-2019-09/unevaluatedItems-boolean.json",
          "draft-2019-09/cycled/unevaluatedItems-boolean.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 unevaluatedItems schema") {
    cycle("draft-2019-09/unevaluatedItems-schema.json",
          "draft-2019-09/cycled/unevaluatedItems-schema.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 7 required dependencies to Draft 7") {
    cycle("draft-7/required-dependencies.json",
          "draft-7/required-dependencies.json",
          DRAFT_7_EMITTER,
          `application/json`)
  }

  test("Draft 7 schema dependencies to Draft 7") {
    cycle("draft-7/schema-dependencies.json", "draft-7/schema-dependencies.json", DRAFT_7_EMITTER, `application/json`)
  }

  test("Draft 7 schema dependencies with $ref to `application/json`-LD") {
    cycle("draft-7/schema-dependencies-ref.json",
          "draft-7/jsonld/schema-dependencies-ref.jsonld",
          JsonLdEmitter,
          `application/json`)
  }

  test("Draft 7 required dependencies to json-ld") {
    cycle("draft-7/required-dependencies.json",
          "draft-7/jsonld/required-dependencies.jsonld",
          JsonLdEmitter,
          `application/json`)
  }

  test("Draft 7 schema dependencies to json-ld") {
    cycle("draft-7/schema-dependencies.json",
          "draft-7/jsonld/schema-dependencies.jsonld",
          JsonLdEmitter,
          `application/json`)
  }

  test("Draft 2019 dependents to Draft 2019") {
    cycle("draft-2019-09/schema-required-dependencies.json",
          "draft-2019-09/cycled/schema-required-dependencies.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019 dependents to JsonLd") {
    cycle("draft-2019-09/schema-required-dependencies.json",
          "draft-2019-09/jsonld/schema-required-dependencies.jsonld",
          JsonLdEmitter,
          `application/json`)
  }

  test("Draft 7 content schema to Draft 7") {
    cycle("draft-7/content.json", "draft-7/content.json", DRAFT_7_EMITTER, `application/json`)
  }

  test("Draft 7 content schema to JSONLD") {
    cycle("draft-7/content.json", "draft-7/jsonld/content.jsonld", JsonLdEmitter, `application/json`)
  }

  test("Draft 2019-09 content schema to JSONLD") {
    cycle("draft-2019-09/content.json", "draft-2019-09/jsonld/content.jsonld", JsonLdEmitter, `application/json`)
  }

  test("Draft 2019-09 content schema to Draft 2019") {
    cycle("draft-2019-09/content.json", "draft-2019-09/cycled/content.json", DRAFT_2019_09_EMITTER, `application/json`)
  }

  test("Draft 2019-09 $ref alongside facets to Draft 2019") {
    cycle("draft-2019-09/ref-alongside-facets.json",
          "draft-2019-09/cycled/ref-alongside-facets.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 $ref with allOf to Draft 2019") {
    cycle("draft-2019-09/ref-with-allOf.json",
          "draft-2019-09/cycled/ref-with-allOf.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 standalone $ref to Draft 2019") {
    cycle("draft-2019-09/standalone-ref.json",
          "draft-2019-09/cycled/standalone-ref.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 2019-09 min and max contains to Draft 2019") {
    cycle("draft-2019-09/min-and-max-contains.json",
          "draft-2019-09/cycled/min-and-max-contains.json",
          DRAFT_2019_09_EMITTER,
          `application/json`)
  }

  test("Draft 7 $id referencing test") {
    cycle("draft-7/with-id.json", "draft-7/jsonld/with-id.jsonld", JsonLdEmitter, `application/json`)
  }

  test("Draft 2019-09 $id and $anchor referencing test") {
    cycle("draft-2019-09/with-id.json", "draft-2019-09/jsonld/with-id.jsonld", JsonLdEmitter, `application/json`)
  }

  private def cycle(path: String,
                    golden: String,
                    emitter: SchemaEmitter,
                    mediatype: String = `application/json`): Future[Assertion] = {
    val finalPath   = basePath + path
    val finalGolden = basePath + golden
    val fragment =
      parseSchema(platform, finalPath, mediatype, APIConfiguration.API())
    val expected = emitter
      .emitSchema(fragment.baseUnit.asInstanceOf[DataTypeFragment])
    writeTemporaryFile(finalGolden)(expected).flatMap(s => assertDifferences(s, finalGolden))
  }
}

sealed trait SchemaEmitter {
  def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String
}

object JsonLdEmitter extends SchemaEmitter {

  lazy private val options =
    RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
  lazy private val spec = Spec.AMF

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String = {
    AMFRenderer(fragment, HintProvider.defaultHintFor(spec), options).renderToString
  }
}

object JsonSchemaTestEmitters {
  val DRAFT_2019_09_EMITTER: JsonSchemaTestEmitter = JsonSchemaTestEmitter(JSONSchemaDraft201909SchemaVersion)
  val DRAFT_7_EMITTER: JsonSchemaTestEmitter       = JsonSchemaTestEmitter(JSONSchemaDraft7SchemaVersion)
}

case class JsonSchemaTestEmitter(to: JSONSchemaVersion) extends SchemaEmitter {

  private val options =
    RenderOptions().withSchemaVersion(SchemaVersion.toClientOptions(to))

  override def emitSchema(fragment: DataTypeFragment)(implicit executionContext: ExecutionContext): String = {
    val config    = EmptyRenderConfiguration(UnhandledErrorHandler, options)
    val shape     = fragment.encodes
    val emitter   = JsonSchemaEmitter(shape, Seq(shape), renderConfig = config, errorHandler = UnhandledErrorHandler)
    val goldenDoc = emitter.emitDocument()
    JsonRender.render(goldenDoc)
  }
}
