package amf.cycle

import amf.core.internal.remote.Vendor
import amf.shapes.internal.annotations.ForceEntry
import amf.shapes.client.scala.model.domain.AnyShape
import amf.testing.{Raml08Yaml, Raml10Yaml}

class Raml08ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/"
  val cyclePath: String         = "amf-cli/shared/src/test/resources/cycle/raml08/"
  val validationsPath: String   = "amf-cli/shared/src/test/resources/validations/"
  val resourcesPath: String     = "amf-cli/shared/src/test/resources/"
  val vendor: Vendor            = Vendor.RAML08

  test("type - inlined json schema") {
    renderElement(
      "schema-position/api.raml",
      CommonExtractors.declaresIndex(0),
      "schema-position/type-emission.yaml",
      Raml08Yaml,
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
      Raml08Yaml,
      directory = basePath + "cycle/raml08/"
    )
  }

  test("type - reference to external json schema with relative path") {
    renderElement(
      "raml08/json_schema_array.raml",
      CommonExtractors.declaresIndex(0),
      "raml08/json_schema_array-type.yaml",
      Raml08Yaml
    )
  }

  test("trait") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(1),
      "abstract/trait-emission.yaml",
      Raml08Yaml,
      directory = cyclePath
    )
  }

  test("resource type") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(0),
      "abstract/rt-emission.yaml",
      Raml08Yaml,
      directory = cyclePath
    )
  }

  test("security scheme") {
    renderElement(
      "raml08AuthorizationGrant.raml",
      CommonExtractors.declaresIndex(0),
      "raml08-scheme-emission.yaml",
      Raml08Yaml,
      directory = validationsPath + "security-schemes/"
    )
  }

  test("parameters") {
    renderElement(
      "input.raml",
      CommonExtractors.firstOperation.andThen(_.map(_.request.queryParameters.head)),
      "param-emission.yaml",
      Raml08Yaml,
      directory = resourcesPath + "org/raml/api/v08/full/"
    )
  }

  test("operation") {
    renderElement(
      "api.raml",
      CommonExtractors.firstOperation,
      "operation-emission.yaml",
      Raml08Yaml,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("endpoint") {
    renderElement(
      "api.raml",
      CommonExtractors.firstEndpoint,
      "endpoint-emission.yaml",
      Raml08Yaml,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("example") {
    renderElement(
      "api.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head.schema.asInstanceOf[AnyShape].examples.head)),
      "example-emission.yaml",
      Raml08Yaml,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("payload") {
    renderElement(
      "api.raml",
      CommonExtractors.firstResponse.andThen(_.map(r => r.payloads.head)),
      "payload-emission.yaml",
      Raml08Yaml,
      directory = basePath + "cycle/raml08/americanflightapi/"
    )
  }

  test("security requirement") {
    renderElement(
      "valid-raml08-oauth2.raml", // todo another one
      CommonExtractors.firstOperation.andThen(_.map(_.security(1))),
      "requirement-emission.yaml",
      Raml10Yaml,
      directory = validationsPath + "security-schemes/"
    )
  }

  test("documentation facet - creative work") {
    renderElement(
      "valid-raml08-oauth2.raml", // TODO: raml 10 with raml 08 name at a 08 test suit? XD
      CommonExtractors.webapi.andThen(_.map(_.documentations.head)),
      "documentation-emission.yaml",
      Raml10Yaml,
      directory = validationsPath + "security-schemes/"
    )
  }

}
