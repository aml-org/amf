package amf.cycle

import amf.core.annotations.ExternalFragmentRef
import amf.core.remote.{RamlYamlHint, Vendor}
import amf.plugins.document.webapi.annotations.ForceEntry
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.domain.webapi.models.security.SecurityScheme

class Raml10ElementCycleTest extends DomainElementCycleTest {

  val basePath: String        = "amf-client/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String  = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  val parserPath: String      = "amf-client/shared/src/test/resources/org/raml/parser/"
  val validationsPath: String = "amf-client/shared/src/test/resources/validations/"
  val vendor: Vendor          = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.declaredWithName("root"),
      "type/complex-inheritance-unions.yaml",
      RamlYamlHint
    )
  }

  test("type - emission of created link with force entry") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      (b) => {
        val original: Option[AnyShape] = CommonExtractors.declaredWithName("root")(b).map(_.asInstanceOf[AnyShape])
        val link: Option[AnyShape]     = original.map(_.link[AnyShape]("someName").add(ForceEntry()))
        link
      },
      "type/link-force-entry-emission.yaml",
      RamlYamlHint
    )
  }

  test("type - emission of created link with force entry and external fragment ref") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      (b) => {
        val original: Option[AnyShape] = CommonExtractors.declaredWithName("root")(b).map(_.asInstanceOf[AnyShape])
        val link: Option[AnyShape] =
          original.map(_.link[AnyShape]("someName.raml").add(ForceEntry()).add(ExternalFragmentRef("someName.raml")))
        link
      },
      "type/link-force-entry-fragment-emission.yaml",
      RamlYamlHint
    )
  }

  test("type - reference to external fragment") {
    renderElement(
      "multiple-refs/input.raml",
      CommonExtractors.declaredWithName("root"),
      "multiple-refs/type-cycle-emission.yaml",
      RamlYamlHint,
      directory = jsonSchemaPath
    )
  }

  test("type - reference to external fragment and declared type with entry") {
    renderElement(
      "type/refs-with-entry.raml",
      CommonExtractors.declaredWithName("root"),
      "type/refs-with-entry-emission.yaml",
      RamlYamlHint
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
      CommonExtractors.declaresIndex(2),
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

  test("resource type link") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(1),
      "abstract/rt-link-emission.yaml",
      RamlYamlHint
    )
  }

  test("trait link") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(3),
      "abstract/trait-link-emission.yaml",
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

  test("security scheme link") {
    renderElement(
      "security-scheme/api.raml",
      CommonExtractors.declaresIndex(0),
      "security-scheme/security-scheme-link-emission.yaml",
      RamlYamlHint
    )
  }

  test("security scheme created link with ExternalFragmentRef") {
    renderElement(
      "security-scheme/api.raml",
      (b) => {
        val original = CommonExtractors.declaresIndex(1)(b).map(_.asInstanceOf[SecurityScheme])
        original.map(_.link[SecurityScheme]("someName.raml").add(ExternalFragmentRef("someName.raml")))
      },
      "security-scheme/security-scheme-created-link-emission.yaml",
      RamlYamlHint
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

  test("annotation type") {
    renderElement(
      "annotation-type/api.raml",
      CommonExtractors.declaresIndex(0),
      "annotation-type/annotation-type-emission.yaml",
      RamlYamlHint
    )
  }

  test("annotation type link") {
    renderElement(
      "annotation-type/api.raml",
      CommonExtractors.declaresIndex(1),
      "annotation-type/annotation-type-link-emission.yaml",
      RamlYamlHint
    )
  }

  test("annotation type library link") {
    renderElement(
      "annotation-type/api.raml",
      CommonExtractors.declaresIndex(2),
      "annotation-type/annotation-type-lib-link-emission.yaml",
      RamlYamlHint
    )
  }

}
