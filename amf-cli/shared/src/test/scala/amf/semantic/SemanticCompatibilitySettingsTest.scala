package amf.semantic

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class SemanticCompatibilitySettingsTest extends AsyncFunSuite with Matchers {

  private val basePath                                     = "amf-cli/shared/src/test/resources/semantic/compatibility/"
  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Conversion to RAML 1.0 from OAS 2.0 shouldn't flatten semantic extensions") {
    run("api.oas20.json", RAMLConfiguration.RAML10())
  }

  test("Conversion to RAML 1.0 from OAS 3.0 shouldn't flatten semantic extensions") {
    run("api.oas30.json", RAMLConfiguration.RAML10())
  }

  test("Conversion to OAS 2.0 shouldn't flatten semantic extensions") {
    run("api.raml", OASConfiguration.OAS20())
  }

  test("Conversion to OAS 3.0 shouldn't flatten semantic extensions") {
    run("api.raml", OASConfiguration.OAS30())
  }

  private def run(path: String, compatConfig: AMFConfiguration): Future[Assertion] = {
    getConfig("dialect.yaml")
      .flatMap { config =>
        config.baseUnitClient().parseDocument(s"file://${basePath}${path}")
      }
      .map { result =>
        result.conforms shouldBe true
        hasSemanticExtensionInCustom(result.document) shouldBe true
        val client      = compatConfig.baseUnitClient()
        val transformed = client.transform(result.baseUnit, PipelineId.Compatibility)
        transformed.conforms shouldBe true
        hasSemanticExtensionInCustom(transformed.baseUnit.asInstanceOf[Document]) shouldBe true
        hasFlattenedSemanticExtension(transformed.baseUnit.asInstanceOf[Document]) shouldBe false
      }
  }

  private def getConfig(
      dialect: String,
      baseConfig: AMFConfiguration = APIConfiguration.API()
  ): Future[AMFConfiguration] = {
    baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(s"file://$basePath" + dialect)
  }

  private def hasFlattenedSemanticExtension(doc: Document): Boolean = {
    doc.encodes.graph.containsProperty("http://a.ml/vocab#project")
  }

  private def hasSemanticExtensionInCustom(doc: Document): Boolean = {
    doc.encodes.customDomainProperties.head.graph.containsProperty("http://a.ml/vocab#project")
  }
}
