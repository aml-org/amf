package amf.semanticjsonschema

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, AmlHint, Hint}
import amf.core.internal.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{AMFSemanticSchemaResult, SemanticJsonSchemaConfiguration}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaToDialectTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val dialectPath    = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/dialects/"

  multiOutputTest("Dialect generation from basic JSON schema", "basic")
  multiOutputTest("Dialect generation from basic JSON schema with characteristics", "basicWithCharacteristics")
  multiOutputTest("Dialect generation from JSON schema with characteristics", "complexWithCharacteristics")
  multiOutputTest("Dialect generation from intermediate JSON schema", "intermediate")
  multiOutputTest("Dialect generation from JSON schema with allOf", "allOf")
  multiOutputTest("Dialect generation from JSON schema with oneOf", "oneOf")
  multiOutputTest("Dialect generation from JSON schema with remote context", "remote-context")
  multiOutputTest("Dialect generation from JSON schema with maximum and minimum", "minimum-maximum")
  multiOutputTest("Dialect generation from JSON schema with any type property", "any-property")
  multiOutputTest("Dialect generation from JSON schema with enum property", "enum")
  multiOutputTest("Dialect generation from JSON schema with const property", "const")
  multiOutputTest("Dialect generation from JSON schema with default property", "default")
  multiOutputTest("Dialect generation from JSON schema with multiple characteristics in the same property",
                  "multiple-characteristics")
  multiOutputTest("Dialect generation from JSON schema with duplicate semantic terms", "duplicate-semantics")

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
      result <- parseSchema(schema)
      _ <- {
        assert(result.conforms)
        Future.successful(result)
      }
      emitted <- {
        val expected = emit(result, hint)
        writeTemporaryFile(golden)(expected).flatMap(s => assertDifferences(s, golden))
      }
    } yield emitted
  }

  private def parseSchema(path: String): Future[AMFSemanticSchemaResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined()
    config.baseUnitClient().parseSemanticSchema(path)
  }

  private def emit(result: AMFSemanticSchemaResult, target: Hint)(
      implicit executionContext: ExecutionContext): String = {
    val options =
      RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
    val AMFSemanticSchemaResult(dialect, vocab, _) = result
    if (target != AmfJsonHint) {
      val dialectAsString = new AMFRenderer(dialect, target, options).renderToString
      val refsAsString    = vocab.map(ref => new AMFRenderer(ref, target, options).renderToString).toList
      (dialectAsString :: refsAsString).mkString("---\n")
    } else {
      new AMFRenderer(dialect, target, options).renderToString
    }
  }
}