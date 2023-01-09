package amf.jsonschema

import amf.apicontract.client.scala.ConfigurationAdapter
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.validation.UniquePlatformReportGenTest

import scala.concurrent.ExecutionContext

class JsonSchemaDocumentReportTest extends UniquePlatformReportGenTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/jsonschema/doc/"
  override val reportsPath: String = "file://amf-cli/shared/src/test/resources/jsonschema/reports/"

  private val baseConfig = ConfigurationAdapter.adapt(JsonSchemaConfiguration.JsonSchema())

  test("Document with references to nowhere should be invalid") {
    validate(
      "invalid-schema-with-refs-to-nowhere.json",
      Some("invalid-schema-with-refs-to-nowhere.report"),
      configOverride = Some(baseConfig)
    )
  }

  test("Document with reference to root from declaration should work") {
    validate(
      "root-reference-from-declaration.json",
      None,
      configOverride = Some(baseConfig)
    )
  }

  test("Document with reference to root from root schema should work") {
    validate(
      "root-reference-from-root.json",
      None,
      configOverride = Some(baseConfig)
    )
  }

  test("Document with reference to definition from another definition") {
    validate(
      "reference-to-def-inside-def.json",
      None,
      configOverride = Some(baseConfig)
    )
  }

  test("Document with $ref in external with empty value shouldn't conform") {
    validate("empty-ref-in-external.json", Some("empty-ref-in-external.report"), configOverride = Some(baseConfig))
  }

  test("Document with $ref with empty value shouldn't conform") {
    validate("empty-ref-in-root.json", Some("empty-ref-in-root.report"), configOverride = Some(baseConfig))
  }
}
