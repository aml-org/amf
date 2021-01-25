package amf.parser

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

  multiGoldenTest("16 digit long numbers maintain precision in jsonld emission", "large-integer-values.%s") { config =>
    cycle("large-integer-values.raml",
          config.golden,
          RamlYamlHint,
          Amf,
          renderOptions = Some(config.renderOptions.withPrettyPrint))
  }

}
