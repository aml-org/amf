package amf.parser

import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.{Amf, RamlYamlHint}
import amf.io.FunSuiteCycleTests

class Raml10ParserTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/raml10/"

  multiGoldenTest("Type with json schema in it's type facet has an inheritance to it",
                  "type-with-json-schema-in-type-facet.%s") { config =>
    cycle("type-with-json-schema-in-type-facet.raml",
          config.golden,
          RamlYamlHint,
          Amf,
          renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint))
  }

  multiGoldenTest("Nillable types in params are parsed", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      RamlYamlHint,
      Amf,
      renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
      directory = s"${basePath}nillable-type-in-parameter/",
      eh = Some(UnhandledParserErrorHandler)
    )
  }
}
