package amf.emit

import amf.core.internal.remote.{Oas20, Raml08, Raml08YamlHint, Raml10}
import amf.io.FunSuiteCycleTests

class Cycle08ToVersion extends FunSuiteCycleTests {
  override val basePath: String = "amf-cli/shared/src/test/resources/upanddown/raml08/"

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
    FixtureData("operation with a description", "operation-description.raml", "operation-description-10.raml"),
    FixtureData("parameter with file type", "file-type.raml", "file-type-10.raml")
  )

  cycle08to10.foreach(f => {
    test(s"Test Cycle raml 08 to raml 10 ${f.name}") {
      cycle(f.apiFrom, f.apiTo, Raml08YamlHint, Raml10)
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
    FixtureData("operation with a description", "operation-description.raml", "operation-description.raml"),
    FixtureData("Include in documentation content", "include-documentation.raml", "include-documentation-08.raml"),
    FixtureData("Include in resource types and traits", "include-resource-type.raml", "include-resource-type-08.raml"),
    FixtureData("include in anonymous seq of schemas declaration",
                "include-anonymous-schema.raml",
                "include-anonymous-schema-08.raml"),
    FixtureData("Include xsd schema", "include-xsd-schema.raml", "include-xsd-schema-08.raml"),
    FixtureData("security schemes", "security-schemes.raml", "security-schemes-08.raml"),
    FixtureData("empty parameter", "empty-param.raml", "empty-param.raml.raml"),
    FixtureData("empty resource type reference", "empty-resource-type-ref.raml", "empty-resource-type-ref.raml.raml"),
    FixtureData("name in json schema def", "name-json-schema.raml", "name-json-schema-08.raml"),
    FixtureData("empty media type", "empty-media-type.raml", "empty-media-type-08.raml"),
    FixtureData("empty params", "empty-params.raml", "empty-params-08.raml"),
    FixtureData("nullpointer", "default-type-payloads.raml", "default-type-payloads-08.raml"),
    FixtureData("parameter with file type", "file-type.raml", "file-type-08.raml"),
    FixtureData("json schema array", "jsonschema-array.raml", "jsonschema-array-08.raml"),
    FixtureData("required params explicit and not present", "requireds.raml", "requireds-08.raml")
  )

  cycles08.foreach(f => {
    test(s"Test 08 Cycle ${f.name}") {
      cycle(f.apiFrom, f.apiTo, Raml08YamlHint, Raml08)
    }
  })

  // todo add more test, this is important for toJsonSchema logic
  val cycleOas = Seq(
    FixtureData("operation base uri parameters",
                "operation-base-uri-parameters.raml",
                "operation-base-uri-parameters.json"),
    FixtureData("parameter with file type", "file-type.raml", "file-type.json")
  )

  cycleOas.foreach { f =>
    test(s"Test 08 to Oas Cycle ${f.name}") {
      cycle(f.apiFrom, f.apiTo, Raml08YamlHint, Oas20)
    }
  }

}
