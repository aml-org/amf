package amf.semanticjsonschema

import amf.aml.client.scala.{AMLConfiguration, AMLDialectResult}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote.Mimes
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaDialectInstanceTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val instancePath   = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/instances/"

  test("Dialect instance validation with basic JSON schema") {
    run("basic")
  }

  test("Dialect instance validation with basic with characteristics JSON schema") {
    run("basicWithCharacteristics")
  }

  test("Dialect instance validation with intermediate JSON schema") {
    run("intermediate")
  }

  test("Dialect instance validation with allOf JSON schema") {
    run("allOf")
  }

  test("Dialect instance validation with minimum and maximum JSON schema") {
    run("minimum-maximum")
  }

  test("Dialect instance with duplicate-semantics JSON Schema") {
    run("duplicate-semantics")
  }

  test("Dialect instance with multiple-characteristics JSON Schema") {
    run("multiple-characteristics")
  }

  // TODO review this validation. Is failing.
  test("Dialect instance validation with oneOf JSON schema") {
    run("oneOf")
  }

  private def run(filename: String): Future[Assertion] = {

    val finalJsonschemaPath = s"$jsonSchemaPath$filename.json"
    val finalInstancePath   = s"$instancePath$filename.json"
    val finalJsonLdPath     = s"$instancePath$filename.jsonld"

    for {
      dialect <- parseSchema(finalJsonschemaPath)
      config <- Future.successful(
        AMLConfiguration
          .predefined()
          .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .withDialect(dialect.dialect))
      instance <- config.baseUnitClient().parseDialectInstance(finalInstancePath)
      jsonld <- Future.successful(
        config.baseUnitClient().render(instance.dialectInstance, Mimes.`application/ld+json`))
      tmp  <- writeTemporaryFile(finalJsonLdPath)(jsonld)
      diff <- assertDifferences(tmp, finalJsonLdPath)
    } yield {
      assert(instance.conforms)
      diff
    }
  }

  private def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined().withErrorHandlerProvider(() => UnhandledErrorHandler)
    config.baseUnitClient().parseDialect(path)
  }
}
