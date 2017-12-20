package amf.emit

import amf.core.remote.{Raml, RamlYamlHint}
import amf.io.BuildCycleTests

class Cycle08To10Test extends BuildCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/raml08/"

  case class FixtureData(name: String, api08: String, api10: String)

  val fixture = Seq(
    FixtureData("basic diff", "basics-differences.raml", "basics-differences-10.raml"),
    FixtureData("form parameters", "form-parameters.raml", "form-parameters-10.raml"),
    FixtureData("repeat property in parameter", "repeat-property.raml", "repeat-property-10.raml")
  )

  fixture.foreach(f => {
    test(s"Test Cycle raml 08 to raml 10 ${f.name}") {
      cycle(f.api08, f.api10, RamlYamlHint, Raml)
    }
  })

}
