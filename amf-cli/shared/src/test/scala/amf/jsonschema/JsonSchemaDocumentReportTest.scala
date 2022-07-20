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
}
