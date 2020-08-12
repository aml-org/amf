package amf.cycle

import amf.core.remote.{RamlYamlHint, Vendor}
import amf.plugins.domain.shapes.models.NodeShape

class Raml10ElementCycleTest extends DomainElementCycleTest {

  val basePath: String        = "amf-client/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String  = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  val parserPath: String      = "amf-client/shared/src/test/resources/org/raml/parser/"
  val validationsPath: String = "amf-client/shared/src/test/resources/validations/"
  val vendor: Vendor          = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.namedRootInDeclares,
      "type/complex-inheritance-unions.yaml",
      RamlYamlHint
    )
  }

  test("type - reference to external fragment") {
    renderElement(
      "multiple-refs/input.raml",
      CommonExtractors.namedRootInDeclares,
      "multiple-refs/type-cycle-emission.yaml",
      RamlYamlHint,
      directory = jsonSchemaPath
    )
  }

  test("type - ref to external json schemas") {
    renderElement(
      "type/external-json-schema-refs.raml",
      CommonExtractors.declaresIndex(0),
      "type/json-schema-refs-emission.yaml",
      RamlYamlHint
    )
  }

  test("response - named example included") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstResponse,
      "response/output.yaml",
      RamlYamlHint
    )
  }

  test("trait") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(1),
      "abstract/trait-emission.yaml",
      RamlYamlHint
    )
  }

  test("resource type") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(0),
      "abstract/rt-emission.yaml",
      RamlYamlHint
    )
  }

  test("security scheme") {
    renderElement(
      "raml10AuthorizationGrant.raml",
      CommonExtractors.declaresIndex(0),
      "raml10-scheme-emission.yaml",
      RamlYamlHint,
      directory = validationsPath + "security-schemes/"
    )
  }

  test("parameter") {
    renderElement(
      "input.raml",
      CommonExtractors.firstOperation.andThen(_.map(_.request.queryParameters.head)),
      "parameter-emission.yaml",
      RamlYamlHint,
      directory = parserPath + "examples/connect/"
    )
  }

  test("operation") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstOperation,
      "response/operation-emission.yaml",
      RamlYamlHint
    )
  }

  test("endpoint") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstEndpoint,
      "response/endpoint-emission.yaml",
      RamlYamlHint
    )
  }

  test("external example") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head.schema.asInstanceOf[NodeShape].examples.head)),
      "response/example-emission.yaml",
      RamlYamlHint
    )
  }

  test("payload") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head)),
      "response/payload-emission.yaml",
      RamlYamlHint
    )
  }

  test("security requirement") {
    renderElement(
      "security-schemes/oauth-2/secured-by.raml",
      CommonExtractors.firstOperation.andThen(_.map(_.security.head)),
      "security-schemes/oauth-2/requirement-emission.yaml",
      RamlYamlHint,
      validationsPath
    )
  }

}
