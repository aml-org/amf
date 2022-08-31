package amf.cycle

import amf.apicontract.client.scala.configuration.OasComponentConfiguration
import amf.core.internal.remote.{AmfJsonHint, Oas30YamlHint}

class OasComponentCycle extends GraphQLFunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/oas-component/"

  test("OAS 3.0 component cycle to JSON-LD") {
    cycle(
      "simple.yaml",
      "simple.jsonld",
      Oas30YamlHint,
      AmfJsonHint,
      amfConfig = Some(OasComponentConfiguration.OAS30Component())
    )
  }

  test("OAS 3.0 component cycle from JSON-LD") {
    cycle(
      "simple.jsonld",
      "simple.jsonld.yaml",
      AmfJsonHint,
      Oas30YamlHint,
      amfConfig = Some(OasComponentConfiguration.OAS30Component())
    )
  }


}
