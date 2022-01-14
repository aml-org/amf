package amf.semantic

import amf.apicontract.client.scala._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

import scala.concurrent.Future

class JsonLdParsedExtensionsRenderTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/semantic/"

  test("Non-flattened semantic extensions should not be rendered to JSON-LD in a RAML 1.0 spec") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      cycle("api.raml", golden = "instance.parsed.raml.jsonld", Raml10YamlHint, AmfJsonHint, amfConfig = Some(config))
    }
  }

  test("Non-flattened semantic extensions should not be rendered to JSON-LD in a OAS 2.0 spec") {
    getConfig("dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      cycle("api.oas20.yaml",
            golden = "instance.parsed.oas20.jsonld",
            Oas20YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened semantic extensions should not be rendered to JSON-LD in a OAS 3.0 spec") {
    getConfig("dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      cycle("api.oas30.yaml",
            golden = "instance.parsed.oas30.jsonld",
            Oas30YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened semantic extensions should not be rendered to JSON-LD in a ASYNC 2.0 spec") {
    getConfig("dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      cycle("api.async.yaml",
            golden = "instance.parsed.async.jsonld",
            Async20YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened scalar semantic extensions should not be rendered to JSON-LD in a RAML 1.0 spec") {
    getConfig("scalar-dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      cycle("api-scalar.raml",
            golden = "instance-scalar.parsed.raml.jsonld",
            Raml10YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened scalar semantic extensions should not be rendered  to JSON-LD in a OAS 2.0 spec") {
    getConfig("scalar-dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      cycle("api-scalar.oas20.yaml",
            golden = "instance-scalar.parsed.oas20.jsonld",
            Oas20YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened scalar semantic extensions should not be rendered  to JSON-LD in a OAS 3.0 spec") {
    getConfig("scalar-dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      cycle("api-scalar.oas30.yaml",
            golden = "instance-scalar.parsed.oas30.jsonld",
            Oas30YamlHint,
            AmfJsonHint,
            amfConfig = Some(config))
    }
  }

  test("Non-flattened scalar semantic extensions should not be rendered  to JSON-LD in a ASYNC 2.0 spec") {
    getConfig("scalar-dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      cycle("api-scalar.async.yaml",
            golden = "instance-scalar.parsed.async.jsonld",
            Async20YamlHint,
            AmfJsonHint,
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
