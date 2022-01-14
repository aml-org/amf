package amf.semantic

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration, RAMLConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class SemanticExtensionOnTraitsTest extends AsyncFunSuite with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String = "file://amf-cli/shared/src/test/resources/semantic/traits_and_rt/"

  test("Semantic Extensions should be parsed when applying a Resource Type or Trait") {
    for {
      config <- getConfig("dialect.yaml", RAMLConfiguration.RAML10())
      client <- Future.successful(config.baseUnitClient())
      parsed <- client.parse(basePath + "api.raml")
      unit   <- Future.successful(client.transform(parsed.baseUnit).baseUnit)
    } yield {
      val endpoints = unit.asInstanceOf[Document].encodes.asInstanceOf[Api].endPoints
      endpoints.head.operations.head.responses.head.graph
        .containsProperty("http://a.ml/vocab#pagination") shouldBe true
      endpoints(1).operations.head.responses.head.graph.containsProperty("http://a.ml/vocab#pagination") shouldBe true
    }
  }

  private def getConfig(dialect: String,
                        baseConfig: AMFConfiguration = APIConfiguration.API()): Future[AMFConfiguration] = {
    baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(s"$basePath" + dialect)
  }
}
