package amf.avro

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.validation.MultiPlatformReportGenTest

class AvroSchemaValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/avro/schemas/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/avro/reports/"
  val config: AMFConfiguration     = AvroConfiguration.Avro()

  test("validate avro boolean wrong default value") {
    validate(
      "boolean-wrong-default.json",
      Some("boolean-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("validate avro int wrong default value") {
    validate(
      "int-wrong-default.json",
      Some("int-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("validate avro long wrong default value") {
    validate(
      "long-wrong-default.json",
      Some("long-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("validate avro float wrong default value") {
    validate(
      "float-wrong-default.json",
      Some("float-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("validate avro double wrong default value") {
    validate(
      "double-wrong-default.json",
      Some("double-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  // todo: bytes doesn't throw violation, is any default valid?
  test("validate avro bytes wrong default value") {
    validate(
      "bytes-wrong-default.json",
      Some("bytes-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("validate avro string wrong default value") {
    validate(
      "string-wrong-default.json",
      Some("string-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("valid enum avro schema with valid default value") {
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

  // todo: jvm doesn't validate map or array defaults
  test("validate map avro schema with wrong default value") {
    validate(
      "map-wrong-default.json",
      Some("map-wrong-default.report"),
      configOverride = Some(config)
    )
  }

  test("valid fixed avro schema") {
    validate("fixed.json", configOverride = Some(config))
  }

  test("valid array avro schema") {
    validate("array.json", configOverride = Some(config))
  }

  // todo: jvm doesn't validate map or array defaults
  test("validate array avro schema with wrong default value") {
    validate(
      "array-wrong-default.json",
      Some("array-wrong-default.report"),
      configOverride = Some(config)
    )
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

  // todo: has many violations but only throws the first one, how to fix? iterative validations by field?
  test("validate default values in avro primitive types") {
    validate(
      "primitive-types-wrong-defaults.json",
      Some("primitive-types-wrong-defaults.report"),
      configOverride = Some(config)
    )
  }

  test("validate wrong type") {
    validate(
      "wrong-type.json",
      Some("wrong-type.json.report"),
      configOverride = Some(config)
    )
  }

  test("validate simple union in record - valid") {
    validate(
      "union-simple-record-valid.json",
      None,
      configOverride = Some(config)
    )
  }

  test("validate simple union in record - invalid") {
    validate(
      "union-simple-record-invalid.json",
      Some("union-simple-record-invalid.report"),
      configOverride = Some(config)
    )
  }

  test("validate nullable union in record - valid") {
    validate(
      "union-nullable-record-valid.json",
      None,
      configOverride = Some(config)
    )
  }

  test("validate nullable union in record - invalid") {
    validate(
      "union-nullable-record-invalid.json",
      Some("union-nullable-record-invalid.report"),
      configOverride = Some(config)
    )
  }

  test("validate simple union in array - valid") {
    validate(
      "union-simple-array-valid.json",
      None,
      configOverride = Some(config)
    )
  }

  test("validate simple union in array - invalid") {
    validate(
      "union-simple-array-invalid.json",
      Some("union-simple-array-invalid.report"),
      configOverride = Some(config)
    )
  }

  test("validate union at root level is invalid") {
    validate(
      "union-root-invalid.json",
      Some("union-root-invalid.report"),
      configOverride = Some(config)
    )
  }

  test("validate forward reference in between fields from a record") {
    validate(
      "forward-reference.avsc",
      None,
      configOverride = Some(config)
    )
  }

// TODO We need to see how implement this in with AVRO 1.11.3

//  if (platform.name == "jvm") { // We were able only to change this behavior in JVM validator. JS one is still strict (only letter, numbers and '_')
//    test("validate record name with special chars") {
//      validate(
//        "name-with-special-chars.json",
//        None,
//        configOverride = Some(config)
//      )
//    }
//  }
}
