package amf.crosspec

import amf.apicontract.client.scala.{AMFConfiguration, OASConfiguration}
import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.validation.UniquePlatformReportGenTest

import scala.concurrent.Future

class CrossSpecValidationTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/cross-spec/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/cross-spec/reports/"

  // With fallback plugin

  test("RAML 1.0 API with include of OAS 3.0 API") {
    validate("raml-point-oas/api-root.raml", Some("raml-point-oas.report"))
  }

  test("OAS 3.0 API with ref to OAS 2.0 API") {
    validate("oas3-point-oas2/api-root.yaml", Some("oas3-point-oas2.report"))
  }

  test("OAS 2.0 API with ref to OAS 3.0 Component Module") {
    validate("oas2-point-oas3component/api-root.json", Some("oas2-point-oas3component.report"))
  }

  // Without fallback plugin (with cache)

  test("OAS 2.0 API with ref to cached OAS 3.0 Component Module") {
    withCachedReference(
      OASConfiguration.OAS20(),
      basePath + "oas2-point-oas3component/component.yaml",
      OASConfiguration.OAS30Component()
    ).flatMap(configuration =>
      validate(
        "oas2-point-oas3component/api-root.json",
        Some("oas2-point-oas3component-cached.report"),
        configOverride = Some(configuration)
      )
    )
  }

  private def withCachedReference(
      baseConfig: AMFConfiguration,
      referencePath: String,
      referenceConfig: AMFConfiguration
  ): Future[AMFConfiguration] = {
    val client = referenceConfig.withErrorHandlerProvider(() => UnhandledErrorHandler).baseUnitClient()
    for {
      parsed <- client.parse(referencePath).map(_.baseUnit)
    } yield {
      val reference = CachedReference(referencePath, parsed)
      val cache     = CustomUnitCache(Seq(reference))
      baseConfig.withUnitCache(cache)
    }
  }

}
