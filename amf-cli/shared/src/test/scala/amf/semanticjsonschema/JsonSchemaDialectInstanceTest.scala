package amf.semanticjsonschema

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.{AMLConfiguration, AMLDialectResult}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote.Mimes
import amf.core.internal.unsafe.PlatformSecrets
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaDialectInstanceTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"
  private val instancePath   = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/instances/"

  instanceValidation("basic")
  instanceValidation("basic-with-characteristics")
  instanceValidation("intermediate")
  instanceValidation("minimum-maximum")
  instanceValidation("duplicate-semantics")
  instanceValidation("multiple-characteristics")
  instanceValidation("basic-with-extra-properties")
  instanceValidation("oneOf")
  instanceValidation("oneOf-with-extended-schema")
  instanceValidation("oneOf-custom")
  instanceValidation("allOf")
  instanceValidation("allOf-with-extended-schema")
  instanceValidation("allOf-custom")
  instanceValidation("if-then-else", Some("if-then-else-match"))
  instanceValidation("if-then-else", Some("if-then-else-no-match"))
  instanceValidation("if-then-else-with-extended-schema")
  instanceValidation("if-then-without-else", Some("if-then-without-else-match"))
  instanceValidation("if-then-without-else", Some("if-then-without-else-no-match"))
  instanceValidation("if-then-without-else-with-extended-schema",
                     Some("if-then-without-else-with-extended-schema-match"))
  instanceValidation("if-then-without-else-with-extended-schema",
                     Some("if-then-without-else-with-extended-schema-no-match"))
  instanceValidation("empty-object")

  private def instanceValidation(schemaName: String, instanceName: Option[String] = None): Unit = {
    val instanceFinal = instanceName.getOrElse(schemaName)
    val label         = s"Dialect instance $instanceFinal validation with $schemaName JSON Schema"
    test(label) {
      run(schemaName, instanceFinal)
    }
  }

  private def run(schemaName: String, instanceName: String): Future[Assertion] = {

    val jsonschemaFinalPath    = s"$jsonSchemaPath$schemaName.json"
    val instanceFinalPath      = s"$instancePath$instanceName.json"
    val jsonLdFinalPath        = s"$instancePath$instanceName.jsonld"
    val instanceCycleFinalPath = s"$instancePath$instanceName.cycle.json"

    for {
      dialect <- parseSchema(jsonschemaFinalPath)
      // TODO remove this cycle after fix W-10790290. Take into account that the IDs in the goldens will change.
      dialectCycled <- AMLConfiguration
        .predefined()
        .baseUnitClient()
        .parseContent(AMLConfiguration.predefined().baseUnitClient().render(dialect.dialect))
      config <- Future.successful(
        AMLConfiguration
          .predefined()
          .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .withDialect(dialectCycled.baseUnit.asInstanceOf[Dialect]))
      instance <- config.baseUnitClient().parseDialectInstance(instanceFinalPath)
      jsonld <- Future.successful(
        config.baseUnitClient().render(instance.dialectInstance, Mimes.`application/ld+json`))
      tmpLD     <- writeTemporaryFile(jsonLdFinalPath)(jsonld)
      diffLD    <- assertDifferences(tmpLD, jsonLdFinalPath)
      cycled    <- Future.successful(config.baseUnitClient().render(instance.dialectInstance, Mimes.`application/json`))
      tmpCycle  <- writeTemporaryFile(instanceCycleFinalPath)(cycled)
      diffCycle <- assertDifferences(tmpCycle, instanceCycleFinalPath)
    } yield {
      val assertions = Seq(assert(instance.conforms), diffLD, diffCycle)
      assert(assertions.forall(_ == Succeeded))
    }
  }

  private def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined().withErrorHandlerProvider(() => UnhandledErrorHandler)
    config.baseUnitClient().parseDialect(path)
  }
}
