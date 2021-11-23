package amf.semantic

import amf.apicontract.client.scala.{
  AMFConfiguration,
  APIConfiguration,
  AsyncAPIConfiguration,
  OASConfiguration,
  RAMLConfiguration
}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint, Hint, Oas20YamlHint, Oas30YamlHint, Raml10YamlHint}
import amf.validation.{MultiPlatformReportGenTest, UniquePlatformReportGenTest}

import scala.concurrent.{ExecutionContext, Future}

class SemanticExtensionValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/semantic/validation/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/semantic/validation/reports/"
  override val hint: Hint          = Raml10YamlHint

  test("Validate scalar semantic extensions in RAML 1.0 api") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      validate("api.raml",
               Some("api.raml.report"),
               overridedHint = Some(Raml10YamlHint),
               configOverride = Some(config))
    }
  }

  test("Validate scalar semantic extensions in OAS 2.0 api") {
    getConfig("dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      validate("api.oas20.yaml",
               Some("api.oas20.report"),
               overridedHint = Some(Oas20YamlHint),
               configOverride = Some(config))
    }
  }

  test("Validate scalar semantic extensions in OAS 3.0 api") {
    getConfig("dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      validate("api.oas30.yaml",
               Some("api.oas30.report"),
               overridedHint = Some(Oas30YamlHint),
               configOverride = Some(config))
    }
  }

  test("Validate scalar semantic extensions in ASYNC 2.0 api") {
    getConfig("dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      validate("api.async.yaml",
               Some("api.async.report"),
               overridedHint = Some(Async20YamlHint),
               configOverride = Some(config))
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
