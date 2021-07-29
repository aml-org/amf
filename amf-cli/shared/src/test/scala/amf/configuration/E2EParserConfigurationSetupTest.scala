package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment}
import amf.core.internal.remote.Spec
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class E2EParserConfigurationSetupTest extends ConfigurationSetupTest {

  implicit override val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  type Expectation = (BaseUnit, Spec) => Assertion

  case class ExpectedParseCase(config: AMFConfiguration, apiPath: String, expectation: Expectation)
  case class ExpectedErrorCase(config: AMFConfiguration, apiPath: String)

  val onlyParseFixtures: Seq[Any] = Seq(
    generateExpectedDocumentParseFixtures("raml10-api.raml",
                                          Spec.RAML10,
                                          List(apiConfig, webApiConfig, ramlConfig, raml10Config)),
    generateExpectedDocumentParseFixtures("raml08-api.raml",
                                          Spec.RAML08,
                                          List(apiConfig, webApiConfig, ramlConfig, raml08Config)),
    generateExpectedDocumentParseFixtures("oas20-api.json",
                                          Spec.OAS20,
                                          List(apiConfig, webApiConfig, oasConfig, oas20Config)),
    generateExpectedDocumentParseFixtures("oas20-api.yaml",
                                          Spec.OAS20,
                                          List(apiConfig, webApiConfig, oasConfig, oas20Config)),
    generateExpectedDocumentParseFixtures("oas30-api.json",
                                          Spec.OAS30,
                                          List(apiConfig, webApiConfig, oasConfig, oas30Config)),
    generateExpectedDocumentParseFixtures("oas30-api.yaml",
                                          Spec.OAS30,
                                          List(apiConfig, webApiConfig, oasConfig, oas30Config)),
    generateExpectedDocumentParseFixtures("async-api.yaml", Spec.ASYNC20, List(apiConfig, async20Config)),
    generateExpectedDocumentParseFixtures("async-api.json", Spec.ASYNC20, List(apiConfig, async20Config))
  ).flatten

  onlyParseFixtures.foreach {
    case f: ExpectedParseCase =>
      test(s"Test - config ${configNames(f.config)} for ${f.apiPath} generates document") {
        val client = f.config.baseUnitClient()
        for {
          result   <- client.parse(f.apiPath)
          document <- Future.successful { result.baseUnit }
        } yield {
          result.results should have length 0
          f.expectation(document, result.rootSpec)
        }
      }
    case e: ExpectedErrorCase =>
      test(s"Test - config ${configNames(e.config)} for ${e.apiPath} doesn't generates document") {
        val client = e.config.baseUnitClient()
        for {
          result   <- client.parse(e.apiPath)
          document <- Future.successful { result.baseUnit }
        } yield {
          document shouldBe a[ExternalFragment]
        }
      }
  }

  private def generateExpectedDocumentParseFixtures(apiPath: String,
                                                    vendor: Spec,
                                                    validConfigs: List[AMFConfiguration]): Seq[Any] = {
    val finalPath    = basePath + apiPath
    val errorConfigs = configs.diff(validConfigs)
    validConfigs.map(conf => ExpectedParseCase(conf, finalPath, documentExpectation(vendor))) ++
      errorConfigs.map(conf => ExpectedErrorCase(conf, finalPath))
  }

  protected def documentExpectation: Spec => Expectation =
    vendor =>
      (document, parsedSpec) => {
        document shouldBe a[Document]
        vendor shouldEqual parsedSpec
        document.sourceVendor shouldEqual Some(parsedSpec)
    }
}
