package amf.avro

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.validation.MultiPlatformReportGenTest

class AvroSchemaValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/avro/schemas/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/avro/reports/"
  val config: AMFConfiguration     = AvroConfiguration.Avro()

  test("valid enum avro schema") {
    validate("enum.json", configOverride = Some(config))
  }

  test("invalid enum avro default value") {
    validate(
      "enum-wrong-default.json",
      Some("enum-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("valid map avro schema") {
    validate("map.json", configOverride = Some(config))
  }

  test("valid fixed avro schema") {
    validate("fixed.json", configOverride = Some(config))
  }

  test("valid array avro schema") {
    validate("array.json", configOverride = Some(config))
  }

  test("valid recursive avro schema") {
    validate("record-valid-recursive.json", configOverride = Some(config))
  }

  test("valid avro schema with all possible types") {
    validate("all-types.json", configOverride = Some(config))
  }

  test("avro schema record missing 'name' field") {
    validate(
      "record-missing-name.json",
      Some("record-missing-name.report"),
      configOverride = Some(config)
    )
  }

  test("avro schema record missing 'fields' field") {
    validate(
      "record-missing-field.json",
      Some("record-missing-field.report"),
      configOverride = Some(config)
    )
  }

  test("avro schema record missing field missing name") {
    validate(
      "record-missing-field-name.json",
      Some("record-missing-field-name.report"),
      configOverride = Some(config)
    )
  }

  test("invalid avro type") {
    validate(
      "invalid-avro-type.json",
      Some("invalid-avro-type.report"),
      configOverride = Some(config)
    )
  }
}
