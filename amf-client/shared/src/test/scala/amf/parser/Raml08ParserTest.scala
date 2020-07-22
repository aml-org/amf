package amf.parser

import amf.core.remote.{Amf, RamlYamlHint}
import amf.io.FunSuiteCycleTests

class Raml08ParserTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/raml08/"

  multiGoldenTest("Raml types include the name in the lexical information", "schemas-lexical-info.%s") { config =>
    cycle("schemas-lexical-info.raml",
          config.golden,
          RamlYamlHint,
          Amf,
          renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint))
  }
}
