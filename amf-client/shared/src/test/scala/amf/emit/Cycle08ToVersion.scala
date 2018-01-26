package amf.emit

import amf.core.remote.{Oas, Raml08, Raml10, RamlYamlHint}
import amf.io.BuildCycleTests

class Cycle08ToVersion extends BuildCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/raml08/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cycle08to10 = Seq(
    FixtureData("basic diff", "basics-differences.raml", "basics-differences-10.raml"),
    FixtureData("form parameters", "form-parameters.raml", "form-parameters-10.raml"),
    FixtureData("repeat property in parameter", "repeat-property.raml", "repeat-property-10.raml"),
    FixtureData("date type convertion", "date-type.raml", "date-type-10.raml"),
    FixtureData("optional data nodes", "optional-data-nodes.raml", "optional-data-nodes-10.raml"),
    FixtureData("operation base uri parameters",
                "operation-base-uri-parameters.raml",
                "operation-base-uri-parameters-10.raml"),
    FixtureData("named parameters in media type", "named-type.raml", "named-type-10.raml"),
    FixtureData("operation with a description", "operation-description.raml", "operation-description-10.raml")
  )

  cycle08to10.foreach(f => {
    test(s"Test Cycle raml 08 to raml 10 ${f.name}") {
      cycle(f.apiFrom, f.apiTo, RamlYamlHint, Raml10)
    }
  })

  val cycles08 = Seq(
    FixtureData("form parameters", "form-parameters.raml", "form-parameters-08.raml"),
    FixtureData("repeat property in parameter", "repeat-property.raml", "repeat-property-08.raml"),
    FixtureData("date type convertion", "date-type.raml", "date-type.raml"),
    FixtureData("operation base uri parameters",
                "operation-base-uri-parameters.raml",
                "operation-base-uri-parameters.raml"),
    FixtureData("named parameters in media type", "named-type.raml", "named-type.raml"),
    FixtureData("operation with a description", "operation-description.raml", "operation-description.raml")
  )

  cycles08.foreach(f => {
    test(s"Test 08 Cycle ${f.name}") {
      cycle(f.apiFrom, f.apiTo, RamlYamlHint, Raml08)
    }
  })

  val cycleOas = Seq(
    FixtureData("operation base uri parameters",
                "operation-base-uri-parameters.raml",
                "operation-base-uri-parameters.json")
  )

  cycleOas.foreach { f =>
    test(s"Test 08 to Oas Cycle ${f.name}") {
      cycle(f.apiFrom, f.apiTo, RamlYamlHint, Oas)
    }
  }

}
