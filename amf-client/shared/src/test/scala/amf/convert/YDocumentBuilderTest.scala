package amf.convert

import amf.client.environment.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.remod.AMFRenderer
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.AMFSerializer
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeResolver
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.{Raml10EditingPipeline, Raml10TransformationPipeline}
import org.scalatest.Assertion
import org.yaml.builder.YDocumentBuilder
import org.yaml.model.YDocument
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

// TODO add more test cases
abstract class DocBuilderTest extends FunSuiteCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override def basePath: String                            = "amf-client/shared/src/test/resources/render/"

  override def defaultRenderOptions: RenderOptions =
    RenderOptions().withSourceMaps.withPrettyPrint.withAmfJsonLdSerialization

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    amfConfig
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .createClient()
      .transform(unit, Raml10EditingPipeline.name)
      .bu
  }

  private def run(source: String, golden: String, renderOptions: RenderOptions): Future[Assertion] =
    cycle(source, golden, Raml10YamlHint, target = Amf, eh = None, renderOptions = Some(renderOptions))

  multiGoldenTest("Test types with references", "types.%s") { config =>
    run("types.raml", config.golden, config.renderOptions)
  }

  multiGoldenTest("Test union type", "union.%s") { config =>
    run("union.raml", config.golden, config.renderOptions)
  }

  multiGoldenTest("Test recursion type", "recursion.%s") { config =>
    run("recursion.raml", config.golden, config.renderOptions)
  }
}

class YDocumentBuilderTest extends DocBuilderTest {

  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): Future[String] = {
    val builder: YDocumentBuilder = new YDocumentBuilder()
    val renderer                  = new AMFSerializer(unit, "application/graph+ldjson", amfConfig.renderConfiguration)
    renderer
      .renderToBuilder(builder)
      .map(_ => {
        val document = builder.result.asInstanceOf[YDocument]
        JsonRender.render(document)
      })
  }
}
