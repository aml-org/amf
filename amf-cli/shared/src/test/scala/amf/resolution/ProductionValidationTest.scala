package amf.resolution

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, Raml10, Raml10YamlHint}

import scala.concurrent.Future

class ProductionValidationTest extends RamlResolutionTest {
  override val basePath =
    "amf-cli/shared/src/test/resources/production/"

  override def build(config: CycleConfig, amfConfig: AMFGraphConfiguration): Future[BaseUnit] = {
    amfConfig
      .baseUnitClient()
      .parse(s"file://${config.sourcePath}")
      .map(_.baseUnit)
  }

  multiGoldenTest("Recursive union raml to amf", "recursive-union.raml.%s") { config =>
    cycle("recursive-union.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Recursive union raml to raml") {
    cycle("recursive-union.raml", "recursive-union.raml.raml", Raml10YamlHint, Raml10YamlHint)
  }

  test("Patch method raml to raml") {
    cycle("api.raml", "api.raml.raml", Raml10YamlHint, Raml10YamlHint, directory = basePath + "patch-method/")
  }

  test("Override enum in extension raml to raml") {
    cycle("extension.raml", "result.raml", Raml10YamlHint, Raml10YamlHint, directory = basePath + "override-enum/")
  }
}
