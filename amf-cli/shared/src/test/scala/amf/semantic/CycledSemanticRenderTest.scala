package amf.semantic

import amf.apicontract.client.scala._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote.{Async20YamlHint, Oas20YamlHint, Oas30YamlHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

import scala.concurrent.Future

class CycledSemanticRenderTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/semantic/"

  test("Cycle RAML 1.0 with semantic extensions") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      cycle("api.raml", golden = "api.raml", Raml10YamlHint, Raml10YamlHint, amfConfig = Some(config))
    }
  }

  test("Cycle OAS 2.0 with semantic extensions") {
    getConfig("dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      cycle("api.oas20.yaml", golden = "api.oas20.yaml", Oas20YamlHint, Oas20YamlHint, amfConfig = Some(config))
    }
  }

  test("Cycle OAS 3.0 with semantic extensions") {
    getConfig("dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      cycle("api.oas30.yaml", golden = "api.oas30.yaml", Oas30YamlHint, Oas30YamlHint, amfConfig = Some(config))
    }
  }

  test("Cycle ASYNC 3.0 with semantic extensions") {
    getConfig("dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      cycle("api.async.yaml", golden = "api.async.yaml", Async20YamlHint, Async20YamlHint, amfConfig = Some(config))
    }
  }

  test("Cycle RAML 1.0 with scalar semantic extensions") {
    getConfig("scalar-dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      cycle("api-scalar.raml", golden = "api-scalar.raml", Raml10YamlHint, Raml10YamlHint, amfConfig = Some(config))
    }
  }

  test("Cycle OAS 2.0 with scalar semantic extensions") {
    getConfig("scalar-dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      cycle("api-scalar.oas20.yaml",
            golden = "api-scalar.oas20.yaml",
            Oas20YamlHint,
            Oas20YamlHint,
            amfConfig = Some(config))
    }
  }

  test("Cycle OAS 3.0 with scalar semantic extensions") {
    getConfig("scalar-dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      cycle("api-scalar.oas30.yaml",
            golden = "api-scalar.oas30.yaml",
            Oas30YamlHint,
            Oas30YamlHint,
            amfConfig = Some(config))
    }
  }

  test("Cycle ASYNC 2.0 with scalar semantic extensions") {
    getConfig("scalar-dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      cycle("api-scalar.async.yaml",
            golden = "api-scalar.async.yaml",
            Async20YamlHint,
            Async20YamlHint,
            amfConfig = Some(config))
    }
  }

  test("Cycle RAML 1.0 with nested object semantic extensions") {
    getConfig("nested-object-dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      cycle("api-nested-object.raml",
            golden = "api-nested-object.raml",
            Raml10YamlHint,
            Raml10YamlHint,
            amfConfig = Some(config))
    }
  }

  private def getConfig(dialect: String,
                        baseConfig: AMFConfiguration = APIConfiguration.API()): Future[AMFConfiguration] = {
    baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(s"file://$basePath" + dialect)
  }
}
