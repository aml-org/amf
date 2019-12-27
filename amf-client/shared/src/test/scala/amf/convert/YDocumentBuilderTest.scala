package amf.convert

import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.Raml10Plugin
import org.scalatest.Assertion
import org.yaml.builder.YDocumentBuilder
import org.yaml.model.YDocument
import org.yaml.render.JsonRender

import scala.concurrent.{ExecutionContext, Future}

// TODO add more test cases
abstract class DocBuilderTest extends FunSuiteCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override def basePath: String                            = "amf-client/shared/src/test/resources/render/"

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    Raml10Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)

  private def run(source: String, golden: String): Future[Assertion] =
    cycle(source, golden, RamlYamlHint, Amf, eh = None)

  test("Test types with references") {
    run("types.raml", "types.jsonld")
  }

  test("Test union type") {
    run("union.raml", "union.jsonld")
  }

  test("Test recursion type") {
    run("recursion.raml", "recursion.jsonld")
  }
}

class YDocumentBuilderTest extends DocBuilderTest {

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val builder: YDocumentBuilder = new YDocumentBuilder()
    val options                   = RenderOptions().withSourceMaps.withPrettyPrint.withAmfJsonLdSerialization
    val renderer                  = new AMFSerializer(unit, "application/ld+json", "AMF Graph", options)
    renderer
      .renderToBuilder(builder)
      .map(_ => {
        val document = builder.result.asInstanceOf[YDocument]
        JsonRender.render(document)
      })
  }
}
