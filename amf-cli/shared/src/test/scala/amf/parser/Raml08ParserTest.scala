package amf.parser

import amf.core.internal.remote.{Amf, AmfJsonHint, Raml08YamlHint}
import amf.io.FunSuiteCycleTests
import amf.testing.AmfJsonLd

class Raml08ParserTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/raml08/"

  multiGoldenTest("Raml types include the name in the lexical information", "schemas-lexical-info.%s") { config =>
    cycle("schemas-lexical-info.raml",
          config.golden,
          Raml08YamlHint,
          AmfJsonHint,
          renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint))
  }
}
