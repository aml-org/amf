package amf.avro

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.validation.UniquePlatformReportGenTest

class AvroSchemaValidationTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/avro/schemas/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/avro/reports/"
  val config: AMFConfiguration     = AvroConfiguration.Avro()

  test("valid Avro Schema") {
    validate(
      "enum.json",
      Some("enum.report"),
      configOverride = Some(config)
    )
  }

  test("avro schema with invalid 'example' field in record fields") {
    validate(
      "invalid-example-field.json",
      Some("invalid-example-field.report"),
      configOverride = Some(config)
    )
  }
}
