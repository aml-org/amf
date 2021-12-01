package amf.semanticjsonschema

import amf.aml.client.scala.{AMLConfiguration, AMLDialectResult}
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaDialectInstanceTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val instancePath   = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/instances/"

  test("Dialect instance validation with basic JSON schema") {
    run("basic")
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

  // TODO review this validation. Is failing.
  ignore("Dialect instance validation with oneOf JSON schema") {
    run("oneOf")
  }

  private def run(filename: String): Future[Assertion] = {

    val finalJsonschemaPath = s"$jsonSchemaPath$filename.json"
    val finalInstancePath   = s"$instancePath$filename.json"

    for {
      dialect <- parseSchema(finalJsonschemaPath)
      instance <- {
        val amlConfig = AMLConfiguration.predefined().withDialect(dialect.dialect)
        amlConfig.baseUnitClient().parseDialectInstance(finalInstancePath)
      }
    } yield assert(instance.conforms)
  }

  private def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined()
    config.baseUnitClient().parseDialect(path)
  }
}
