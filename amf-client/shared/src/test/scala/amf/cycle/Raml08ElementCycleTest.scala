package amf.cycle

import amf.core.model.domain.DomainElement
import amf.core.remote.{RamlYamlHint, Vendor}
import amf.plugins.document.webapi.annotations.ForceEntry
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}

class Raml08ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val cyclePath: String         = "amf-client/shared/src/test/resources/cycle/raml08/"
  val validationsPath: String   = "amf-client/shared/src/test/resources/validations/"
  val resourcesPath: String     = "amf-client/shared/src/test/resources/"
  val vendor: Vendor            = Vendor.RAML08

  test("type - inlined json schema") {
    renderElement(
      "schema-position/api.raml",
      CommonExtractors.declaresIndex(0),
      "schema-position/type-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/"
    )
  }

  test("type - emission of created link with force entry") {
    renderElement(
      "schema-position/api.raml",
      (b) => {
        val original: Option[AnyShape] = CommonExtractors.declaresIndex(0)(b).map(_.asInstanceOf[AnyShape])
        val link                       = original.map(_.link[AnyShape]("someName").add(ForceEntry()))
        link
      },
      "schema-position/link-force-entry-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/"
    )
  }

  test("type - reference to external json schema with relative path") {
    renderElement(
      "raml08/json_schema_array.raml",
      CommonExtractors.declaresIndex(0),
      "raml08/json_schema_array-type.yaml",
      RamlYamlHint
    )
  }

  test("trait") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(1),
      "abstract/trait-emission.yaml",
      RamlYamlHint,
      directory = cyclePath
    )
  }

  test("resource type") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(0),
      "abstract/rt-emission.yaml",
      RamlYamlHint,
      directory = cyclePath
    )
  }

  test("security scheme") {
    renderElement(
      "raml08AuthorizationGrant.raml",
      CommonExtractors.declaresIndex(0),
      "raml08-scheme-emission.yaml",
      RamlYamlHint,
      directory = validationsPath + "security-schemes/"
    )
  }

  test("parameters") {
    renderElement(
      "input.raml",
      CommonExtractors.firstOperation.andThen(_.map(_.request.queryParameters.head)),
      "param-emission.yaml",
      RamlYamlHint,
      directory = resourcesPath + "org/raml/api/v08/full/"
    )
  }

  test("operation") {
    renderElement(
      "api.raml",
      CommonExtractors.firstOperation,
      "operation-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("endpoint") {
    renderElement(
      "api.raml",
      CommonExtractors.firstEndpoint,
      "endpoint-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("example") {
    renderElement(
      "api.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head.schema.asInstanceOf[AnyShape].examples.head)),
      "example-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("payload") {
    renderElement(
      "api.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head)),
      "payload-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("security requirement") {
    renderElement(
      "valid-raml08-oauth2.raml",
      CommonExtractors.firstOperation.andThen(_.map(_.security(1))),
      "requirement-emission.yaml",
      RamlYamlHint,
      directory = validationsPath + "security-schemes/"
    )
  }

  test("documentation facet - creative work") {
    renderElement(
      "valid-raml08-oauth2.raml",
      CommonExtractors.webapi.andThen(_.map(_.documentations.head)),
      "documentation-emission.yaml",
      RamlYamlHint,
      directory = validationsPath + "security-schemes/"
    )
  }

}
