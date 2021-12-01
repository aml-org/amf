package amf.semanticjsonschema

import amf.aml.client.scala.AMLDialectResult
import amf.aml.client.scala.model.document.Dialect
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, AmlHint, Hint}
import amf.core.internal.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaToDialectTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val dialectPath    = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/dialects/"

  multiOutputTest("Dialect generation from basic JSON schema", "basic")
  multiOutputTest("Dialect generation from intermediate JSON schema", "intermediate")
  multiOutputTest("Dialect generation from JSON schema with allOf", "allOf")
  multiOutputTest("Dialect generation from JSON schema with oneOf", "oneOf")
  multiOutputTest("Dialect generation from JSON schema with remote context", "remote-context")
  multiOutputTest("Dialect generation from JSON schema with maximum and minimum", "minimum-maximum")

  private def multiOutputTest(label: String, filename: String): Unit = {

    val finalPath         = s"$jsonSchemaPath$filename.json"
    val finalGoldenYaml   = s"$dialectPath$filename.yaml"
    val finalGoldenJsonLD = s"$dialectPath$filename.jsonld"

    test(s"$label to JSON-LD") {
      run(finalPath, finalGoldenJsonLD, AmfJsonHint)
    }
    test(s"$label to YAML") {
      run(finalPath, finalGoldenYaml, AmlHint)
    }
  }

  private def run(schema: String, golden: String, hint: Hint): Future[Assertion] = {
    for {
      dialect <- parseSchema(schema).map(_.dialect)
      result <- {
        val expected = emit(dialect, hint)
        writeTemporaryFile(golden)(expected).flatMap(s => assertDifferences(s, golden))
      }
    } yield result
  }

  private def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined()
    config.baseUnitClient().parseDialect(path)
  }

  private def emit(dialect: Dialect, target: Hint)(implicit executionContext: ExecutionContext): String = {
    val options =
      RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
    new AMFRenderer(dialect, target, options).renderToString
  }
}
