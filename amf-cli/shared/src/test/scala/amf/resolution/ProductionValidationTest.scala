package amf.resolution

import amf.client.environment.RAMLConfiguration
import amf.client.parse.DefaultErrorHandler
import amf.client.remod.AMFGraphConfiguration
import amf.core.errorhandling.AMFErrorHandler
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml10, Raml10YamlHint}
import amf.facades.{AMFCompiler, Validation}

import scala.concurrent.Future

class ProductionValidationTest extends RamlResolutionTest {
  override val basePath =
    "amf-cli/shared/src/test/resources/production/"

  override def build(config: CycleConfig, amfConfig: AMFGraphConfiguration): Future[BaseUnit] = {
    amfConfig
      .createClient()
      .parse(s"file://${config.sourcePath}")
      .map(_.bu)
  }

  multiGoldenTest("Recursive union raml to amf", "recursive-union.raml.%s") { config =>
    cycle("recursive-union.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Recursive union raml to raml") {
    cycle("recursive-union.raml", "recursive-union.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Patch method raml to raml") {
    cycle("api.raml", "api.raml.raml", Raml10YamlHint, Raml10, directory = basePath + "patch-method/")
  }

}
