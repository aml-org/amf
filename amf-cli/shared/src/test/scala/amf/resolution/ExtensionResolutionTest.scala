package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{Amf, Raml10, Raml10YamlHint}
import amf.testing.{AmfJsonLd, Raml10Yaml}

import scala.concurrent.ExecutionContext

class ExtensionResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/resolution/extension/"

  test("Extension with annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10Yaml, directory = s"${basePath}annotations/")
  }

  test("Extension basic to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10Yaml, directory = s"${basePath}basic/")
  }

  test("Extension with traits to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10Yaml, directory = s"${basePath}traits/")
  }

  multiGoldenTest("Extension with traits to Amf", "output.%s") { config =>
    cycle(
      "input.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonLd,
      directory = s"${basePath}traits/",
      renderOptions = Some(config.renderOptions),
      transformWith = Some(Raml10)
    )
  }

  test("Extension chain to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10Yaml, directory = s"${basePath}chain/")
  }

  test("Extension with example to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, target = Raml10Yaml, directory = s"${basePath}example/")
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
