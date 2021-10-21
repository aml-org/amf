package amf.semantic

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

class JsonLdSemanticExtensionsRenderTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/semantic/"

  test("Render flattened semantic extensions to JSON-LD in a RAML 1.0 spec") {
    cycle("api.raml",
          golden = "instance.raml.jsonld",
          Raml10YamlHint,
          AmfJsonHint,
          renderOptions = Some(RenderOptions().withPrettyPrint.withCompactUris))
  }

  test("Render flattened semantic extensions to JSON-LD in a OAS 2.0 spec") {
    cycle("api.oas20.yaml",
          golden = "instance.oas20.jsonld",
          Oas20YamlHint,
          AmfJsonHint,
          renderOptions = Some(RenderOptions().withPrettyPrint.withCompactUris))
  }

  test("Render flattened semantic extensions to JSON-LD in a OAS 3.0 spec") {
    cycle("api.oas30.yaml",
          golden = "instance.oas30.jsonld",
          Oas30YamlHint,
          AmfJsonHint,
          renderOptions = Some(RenderOptions().withPrettyPrint.withCompactUris))
  }

  test("Render flattened semantic extensions to JSON-LD in a ASYNC 2.0 spec") {
    cycle("api.async.yaml",
          golden = "instance.async.jsonld",
          Async20YamlHint,
          AmfJsonHint,
          renderOptions = Some(RenderOptions().withPrettyPrint.withCompactUris))
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    APIConfiguration.fromSpec(unit.sourceSpec.get).baseUnitClient().transform(unit).baseUnit
  }

  override protected def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    getConfig("dialect.yaml")
  }

  override protected def buildConfig(from: AMFConfiguration,
                                     options: Option[RenderOptions],
                                     eh: Option[AMFErrorHandler]): AMFConfiguration = {
    getConfig("dialect.yaml", from)
  }

  private def getConfig(dialect: String, baseConfig: AMFConfiguration = APIConfiguration.API()): AMFConfiguration = {
    val config = baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(s"file://$basePath" + dialect)

    await { config }
  }
}
