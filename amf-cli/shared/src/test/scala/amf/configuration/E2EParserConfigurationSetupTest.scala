package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment}
import amf.core.internal.remote.Vendor
import org.scalatest.Assertion

import scala.concurrent.Future

class E2EParserConfigurationSetupTest extends ConfigurationSetupTest {

  type Expectation = BaseUnit => Assertion

  case class ExpectedParseCase(config: AMFConfiguration, apiPath: String, expectation: Expectation)
  case class ExpectedErrorCase(config: AMFConfiguration, apiPath: String)

  val onlyParseFixtures: Seq[Any] = Seq(
    generateExpectedDocumentParseFixtures("raml10-api.raml",
                                          Vendor.RAML10,
                                          List(apiConfig, webApiConfig, ramlConfig, raml10Config)),
    generateExpectedDocumentParseFixtures("raml08-api.raml",
                                          Vendor.RAML08,
                                          List(apiConfig, webApiConfig, ramlConfig, raml08Config)),
    generateExpectedDocumentParseFixtures("oas20-api.json",
                                          Vendor.OAS20,
                                          List(apiConfig, webApiConfig, oasConfig, oas20Config)),
    generateExpectedDocumentParseFixtures("oas20-api.yaml",
                                          Vendor.OAS20,
                                          List(apiConfig, webApiConfig, oasConfig, oas20Config)),
    generateExpectedDocumentParseFixtures("oas30-api.json",
                                          Vendor.OAS30,
                                          List(apiConfig, webApiConfig, oasConfig, oas30Config)),
    generateExpectedDocumentParseFixtures("oas30-api.yaml",
                                          Vendor.OAS30,
                                          List(apiConfig, webApiConfig, oasConfig, oas30Config)),
    generateExpectedDocumentParseFixtures("async-api.yaml", Vendor.ASYNC20, List(apiConfig, async20Config)),
    generateExpectedDocumentParseFixtures("async-api.json", Vendor.ASYNC20, List(apiConfig, async20Config))
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
          f.expectation(document)
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
                                                    vendor: Vendor,
                                                    validConfigs: List[AMFConfiguration]): Seq[Any] = {
    val finalPath    = basePath + apiPath
    val errorConfigs = configs.diff(validConfigs)
    validConfigs.map(conf => ExpectedParseCase(conf, finalPath, documentExpectation(vendor))) ++
      errorConfigs.map(conf => ExpectedErrorCase(conf, finalPath))
  }

  protected def documentExpectation: Vendor => Expectation =
    vendor =>
      document => {
        document shouldBe a[Document]
        document.sourceVendor shouldEqual Some(vendor)
    }
}
