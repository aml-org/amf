package amf.cli.internal.convert

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.internal.transformation.Raml10EditingPipeline
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{Amf, AmfJsonHint, Raml10YamlHint}
import amf.core.internal.render.AMFSerializer
import amf.io.FunSuiteCycleTests
import amf.testing.ConfigProvider
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.yaml.builder.YDocumentBuilder
import org.yaml.model.{YDocument, YPart}
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

// TODO add more test cases
abstract class DocBuilderTest extends FunSuiteCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override def basePath: String                            = "amf-cli/shared/src/test/resources/render/"

  override def defaultRenderOptions: RenderOptions =
    RenderOptions().withSourceMaps.withPrettyPrint.withAmfJsonLdSerialization

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    configFor(config.hint.spec)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .baseUnitClient()
      .transform(unit, PipelineId.Editing)
      .baseUnit
  }

  private def run(source: String, golden: String, renderOptions: RenderOptions): Future[Assertion] =
    cycle(source, golden, Raml10YamlHint, target = AmfJsonHint, eh = None, renderOptions = Some(renderOptions))

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

  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    val builder: YDocumentBuilder = new YDocumentBuilder()
    val result: YPart             = amfConfig.baseUnitClient().renderGraphToBuilder(unit, builder)
    val document                  = result.asInstanceOf[YDocument]
    JsonRender.render(document)
  }
}
