package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment}
import amf.core.internal.remote.Spec
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class E2EParserConfigurationSetupTest extends ConfigurationSetupTest {

  type Expectation = (BaseUnit, Spec) => Assertion

  case class ExpectedParseCase(config: AMFConfiguration, apiPath: String, expectation: Expectation)
  case class ExpectedErrorCase(config: AMFConfiguration, apiPath: String)

  val onlyParseFixtures: Seq[Any] = Seq(
    generateExpectedDocumentParseFixtures(
      "raml10-api.raml",
      Spec.RAML10,
      List(apiWithJsonSchema, apiConfig, webApiConfig, ramlConfig, raml10Config)
    ),
    generateExpectedDocumentParseFixtures(
      "raml08-api.raml",
      Spec.RAML08,
      List(apiWithJsonSchema, apiConfig, webApiConfig, ramlConfig, raml08Config)
    ),
    generateExpectedDocumentParseFixtures(
      "oas20-api.json",
      Spec.OAS20,
      List(apiWithJsonSchema, apiConfig, webApiConfig, oasConfig, oas20Config)
    ),
    generateExpectedDocumentParseFixtures(
      "oas20-api.yaml",
      Spec.OAS20,
      List(apiWithJsonSchema, apiConfig, webApiConfig, oasConfig, oas20Config)
    ),
    generateExpectedDocumentParseFixtures(
      "oas30-api.json",
      Spec.OAS30,
      List(apiWithJsonSchema, apiConfig, webApiConfig, oasConfig, oas30Config)
    ),
    generateExpectedDocumentParseFixtures(
      "oas30-api.yaml",
      Spec.OAS30,
      List(apiWithJsonSchema, apiConfig, webApiConfig, oasConfig, oas30Config)
    ),
    generateExpectedDocumentParseFixtures(
      "async-api.yaml",
      Spec.ASYNC20,
      List(apiWithJsonSchema, apiConfig, async20Config),
      List(webApiConfig)
    ),
    generateExpectedDocumentParseFixtures(
      "async-api.json",
      Spec.ASYNC20,
      List(apiWithJsonSchema, apiConfig, async20Config),
      List(webApiConfig)
    ),
    generateExpectedDocumentParseFixtures(
      "jsonSchema.json",
      Spec.JSONSCHEMA,
      List(apiWithJsonSchema, jsonSchema),
      List(
        apiConfig,
        webApiConfig,
        ramlConfig,
        raml10Config,
        raml08Config,
        oas20Config,
        oas30Config,
        oasConfig,
        async20Config
      )
    ),
    expectExternalFragment("async-api.yaml", Spec.ASYNC20, List(webApiConfig)),
    expectExternalFragment("async-api.json", Spec.ASYNC20, List(webApiConfig))
  ).flatten

  onlyParseFixtures.foreach {
    case f: ExpectedParseCase =>
      test(s"Test - config ${configNames(f.config)} for ${f.apiPath} generates document") {
        val client = f.config.baseUnitClient()
        for {
          result   <- client.parse(f.apiPath)
          document <- Future.successful { result.baseUnit }
        } yield {
          result.conforms shouldBe true
          f.expectation(document, result.sourceSpec)
        }
      }
    case e: ExpectedErrorCase =>
      test(s"Test - config ${configNames(e.config)} for ${e.apiPath} doesn't generates document") {
        val client = e.config.baseUnitClient()
        recoverToSucceededIf[UnsupportedDomainForDocumentException] {
          client.parse(e.apiPath)
        }
      }
  }

  private def generateExpectedDocumentParseFixtures(
      apiPath: String,
      spec: Spec,
      validConfigs: List[AMFConfiguration],
      ignored: List[AMFConfiguration] = List()
  ): Seq[Any] = {
    val finalPath    = basePath + apiPath
    val errorConfigs = configs.diff(validConfigs ++ ignored)
    validConfigs.map(conf => ExpectedParseCase(conf, finalPath, documentExpectation(spec))) ++
      errorConfigs.map(conf => ExpectedErrorCase(conf, finalPath))
  }

  private def expectExternalFragment(apiPath: String, spec: Spec, validConfigs: List[AMFConfiguration]): Seq[Any] = {
    val finalPath = basePath + apiPath
    validConfigs.map(conf => ExpectedParseCase(conf, finalPath, externalFragmentExpectation))
  }

  protected def externalFragmentExpectation: Expectation = (document, _) => document shouldBe a[ExternalFragment]

  protected def documentExpectation: Spec => Expectation =
    spec =>
      (document, parsedSpec) => {
        document shouldBe a[Document]
        spec shouldEqual parsedSpec
        document.sourceSpec shouldEqual Some(parsedSpec)
      }
}
