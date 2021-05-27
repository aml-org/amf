package amf.resolution

import amf.client.environment.RAMLConfiguration
import amf.client.parse.DefaultErrorHandler
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml10, Raml10YamlHint}
import amf.facades.{AMFCompiler, Validation}

import scala.concurrent.Future

class ProductionValidationTest extends RamlResolutionTest {
  override val basePath =
    "amf-client/shared/src/test/resources/production/"
  override def build(config: CycleConfig,
                     eh: Option[AMFErrorHandler],
                     useAmfJsonldSerialization: Boolean): Future[BaseUnit] = {
    val amfConfig = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => eh.getOrElse(DefaultErrorHandler()))
    Validation(platform).flatMap { _ =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, config = amfConfig)
        .build()
    }
  }

  multiGoldenTest("Recursive union raml to amf", "recursive-union.raml.%s") { config =>
    cycle("recursive-union.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  test("Recursive union raml to raml") {
    cycle("recursive-union.raml", "recursive-union.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Patch method raml to raml") {
    cycle("api.raml", "api.raml.raml", Raml10YamlHint, Raml10, directory = basePath + "patch-method/")
  }

}
