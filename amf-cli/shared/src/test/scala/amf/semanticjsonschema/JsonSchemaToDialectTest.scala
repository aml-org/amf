package amf.semanticjsonschema

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, AmlHint, Hint}
import amf.core.internal.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{AMFSemanticSchemaResult, SemanticJsonSchemaConfiguration}
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaToDialectTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val dialectPath    = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/dialects/"

  multiOutputTest("basic")
  multiOutputTest("basic-with-characteristics")
  multiOutputTest("complex-with-characteristics")
  multiOutputTest("intermediate")
  multiOutputTest("remote-context")
  multiOutputTest("minimum-maximum")
  multiOutputTest("any-property")
  multiOutputTest("enum")
  multiOutputTest("const")
  multiOutputTest("default")
  multiOutputTest("multiple-characteristics")
  multiOutputTest("duplicate-semantics")
  multiOutputTest("without-schema-key")
  multiOutputTest("any-array")
  multiOutputTest("additional-properties")
  multiOutputTest("oneOf")
  multiOutputTest("number-range-to-double")
  multiOutputTest("oneOf-with-extended-schema")
  multiOutputTest("oneOf-custom")
  multiOutputTest("allOf")
  multiOutputTest("allOf-with-extended-schema")
  multiOutputTest("allOf-custom")
  multiOutputTest("if-then-else")
  multiOutputTest("if-then-else-with-extended-schema")
  multiOutputTest("if-then-without-else")
  multiOutputTest("if-then-without-else-with-extended-schema")
  multiOutputTest("empty-object")
//  // This test breaks if we use a Seq instead of a List in ShapeTransformationContext.transform()
//  // Couldn't figure out why
  multiOutputTest("extended-if-then-semantics")
  multiOutputTest("allOf-complex")
  multiOutputTest("duplicated-semantics-combining")
  multiOutputTest("schema-with-dot-in-title")
  multiOutputTest("property-with-dot-in-title")
  multiOutputTest("default-with-dash-value")

  private def multiOutputTest(filename: String): Unit = {

    val finalLabel        = s"Dialect generation from JSON schema $filename"
    val finalPath         = s"$jsonSchemaPath$filename.json"
    val finalGoldenYaml   = s"$dialectPath$filename.yaml"
    val finalGoldenJsonLD = s"$dialectPath$filename.jsonld"

    test(s"$finalLabel cycle") {
      cycle(finalPath)
    }

    test(s"$finalLabel to JSON-LD") {
      run(finalPath, finalGoldenJsonLD, AmfJsonHint)
    }

    test(s"$finalLabel to YAML") {
      run(finalPath, finalGoldenYaml, AmlHint)
    }
  }

  private def cycle(schema: String): Future[Assertion] = {
    for {
      result      <- parseSchema(schema)
      cycleResult <- cycleDialect(result.baseUnit)
    } yield {
      val assertions = Seq(assert(result.conforms), assert(cycleResult.conforms))
      assert(assertions.forall(_ == Succeeded))
    }
  }

  private def run(schema: String, golden: String, hint: Hint): Future[Assertion] = {
    for {
      result <- parseSchema(schema)
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

  private def cycleDialect(dialect: Dialect): Future[AMFParseResult] =
    AMLConfiguration
      .predefined()
      .baseUnitClient()
      .parseContent(AMLConfiguration.predefined().baseUnitClient().render(dialect))

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
