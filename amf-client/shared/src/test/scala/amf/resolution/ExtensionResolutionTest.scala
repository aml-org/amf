package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml10, Raml10YamlHint}
import amf.emit.AMFRenderer

import scala.concurrent.{ExecutionContext, Future}

class ExtensionResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/extension/"

  test("Extension with annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10, directory = s"${basePath}annotations/")
  }

  test("Extension basic to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10, directory = s"${basePath}basic/")
  }

  test("Extension with traits to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10, directory = s"${basePath}traits/")
  }

  multiGoldenTest("Extension with traits to Amf", "output.%s") { config =>
    cycle(
      "input.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = s"${basePath}traits/",
      renderOptions = Some(config.renderOptions),
      transformWith = Some(Raml10)
    )
  }

  test("Extension chain to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10, directory = s"${basePath}chain/")
  }

  test("Extension with example to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10, directory = s"${basePath}example/")
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
