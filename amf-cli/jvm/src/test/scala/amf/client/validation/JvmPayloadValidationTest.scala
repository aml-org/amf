package amf.client.validation

import amf.cli.internal.convert.NativeOpsFromJvm
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.config.ParsingOptions
import amf.core.internal.remote.Mimes
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.annotations.{AVRORawSchema, AVROSchemaType}

class JvmPayloadValidationTest extends PayloadValidationTest with NativeOpsFromJvm {

  test("Test unexpected type error") {
    val test   = ScalarShape().withDataType(DataTypes.String)
    val report = payloadValidator(test, `application/json`).syncValidate("1234")
    report.conforms shouldBe false
    report.results.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
  }

  // regex pending analysis (APIMF-3058) jvm cannot process regex, on the other hand it is valid for js
  test("Avoid exception for pattern regex that cannot be parsed") {
    val shape = ScalarShape()
      .withDataType(DataTypes.String)
      .withPattern(
        "^(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
      )
    val validator = payloadValidator(shape, `application/json`)
    val report    = validator.syncValidate(""""irrelevant text"""")
    report.conforms shouldBe false
    report.results.head.message shouldBe "Regex defined in schema could not be processed"
  }

  test("Validation against a number with multipleOf 0 should throw violation") {
    val shape                = ScalarShape().withDataType(DataTypes.Number).withMultipleOf(0)
    val validator            = payloadValidator(shape, `application/json`)
    val positiveNumberReport = validator.syncValidate("5")
    val zeroReport           = validator.syncValidate("0")
    positiveNumberReport.results.head.message shouldBe "Can't divide by 0"
    zeroReport.results.head.message shouldBe "Can't divide by 0"
  }

  test("Validation order shouldn't affect conformance of each payload") {
    val itemsShape = NodeShape()
    val shape      = ArrayShape().withItems(itemsShape)
    val validator  = payloadValidator(shape, `application/yaml`)

    val validPayload = "- {}"
    // an error in multiple lines should not break the next validation
    val invalidPayload = "- {}\n - {}"

    val validReport1  = validator.syncValidate(validPayload)
    val invalidReport = validator.syncValidate(invalidPayload)
    val validReport2  = validator.syncValidate(validPayload)

    validReport1.results.size shouldBe 0
    invalidReport.results.size shouldBe 1
    validReport2.results.size shouldBe 0 // this line should not fail
  }

  test("Payload with max depth that exceeds 50 should not conform") {
    val dummyShape = NodeShape()
    val maxDepth   = 50
    val options    = ParsingOptions().setMaxJsonYamlDepth(maxDepth)
    val config     = ShapesConfiguration.predefined().withParsingOptions(options)
    val validator  = payloadValidator(dummyShape, Mimes.`application/json`, config)
    val payload =
      """{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{
        |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
        |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
        |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
        |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
        |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
        |{{{{{[{{{{{{""".stripMargin

    payload.count(char => char == '{' || char == '[') shouldBe >(maxDepth)

    val report = validator.syncValidate(payload)
    report.conforms shouldBe false
    report.results.head.message should include(s"Reached maximum nesting value of $maxDepth in JSON")
  }

  test(
    "Big payload with nested depth accumulation of more than 7 but max accumulation of less than 7 should be valid"
  ) {
    val dummyShape = NodeShape()
    val maxDepth   = 7
    val options    = ParsingOptions().setMaxJsonYamlDepth(maxDepth)
    val config     = ShapesConfiguration.predefined().withParsingOptions(options)
    val validator  = payloadValidator(dummyShape, Mimes.`application/json`, config)
    val payload =
      """{
        | "items": [
        |   {
        |     "a": {
        |       "b": {
        |         "c": {
        |         }
        |       }
        |     }
        |   },
        |   {
        |     "a": {
        |       "b": {
        |         "c": {
        |         }
        |       }
        |     }
        |   }
        | ]
        |}""".stripMargin
    val report = validator.syncValidate(payload)
    report.conforms shouldBe true
  }

  test("Invalid avro record payload in JVM") {
    val s     = ScalarShape().withDataType(DataTypes.String)
    val shape = NodeShape().withName("person")
    shape.withProperty("someString").withRange(s)
    shape.annotations += AVROSchemaType("record")

    val raw =
      """
        |{
        |  "type": "record",
        |  "name": "LongList",
        |  "namespace": "root",
        |  "aliases": ["LinkedLongs"],
        |  "fields": [
        |    {
        |      "name": "value",
        |      "type": "long"
        |    }
        |  ]
        |}
        """.stripMargin

    shape.annotations += AVRORawSchema(raw)

    val payload =
      """
        |{
        |  "someString": "invalid string value"
        |}
        """.stripMargin

    val validator = payloadValidator(shape, `application/json`)
    validator
      .validate(payload)
      .map { r =>
        println(r)
        assert(!r.conforms)
      }
  }

  test("valid avro int payload in JVM") {
    val shape = ScalarShape().withName("int")
    shape.annotations += AVROSchemaType("record")

    val raw =
      """
        |{
        |  "type": "int",
        |  "name": "this is an int"
        |}
        """.stripMargin

    shape.annotations += AVRORawSchema(raw)

    val payload = "1"

    val validator = payloadValidator(shape, `application/json`)
    validator
      .validate(payload)
      .map { r =>
        println(r)
        assert(r.conforms)
      }
  }
}
