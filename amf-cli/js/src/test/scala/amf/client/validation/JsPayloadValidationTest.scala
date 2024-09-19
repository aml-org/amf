package amf.client.validation

import amf.cli.internal.convert.NativeOpsFromJs
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.scala.model.domain.NodeShape

class JsPayloadValidationTest extends PayloadValidationTest with NativeOpsFromJs {

  test("invalid avro with schema error in JS") {
    val schema     = AvroTestSchemas.invalidSchema
    val payload    = "{}"
    val avroSchema = makeAvroShape(schema, "record", NodeShape())

    val validator = payloadValidator(avroSchema, `application/json`)
    validator.validate(payload).map { report =>
      reportContainError(report, "Error in AVRO Schema")
      assert(!report.conforms)
    }
  }

  test("valid avro record payload in JS") {
    val schema = AvroTestSchemas.recordSchema
    val payload =
      """
        |{
        |  "a": "something"
        |}
        """.stripMargin

    val avroSchema = makeAvroShape(schema, "record", NodeShape())

    val validator = payloadValidator(avroSchema, `application/json`)
    validator.validate(payload).map { report => assert(report.conforms) }
  }

  test("invalid avro record payload in JS") {
    val schema = AvroTestSchemas.recordSchema
    val payload =
      """
        |{
        |  "a": 1234
        |}
        """.stripMargin

    val avroSchema = makeAvroShape(schema, "record", NodeShape())

    val validator = payloadValidator(avroSchema, `application/json`)
    validator.validate(payload).map { report =>
      reportContainError(report, "'1234' is not a valid value (of type '\"string\"') for 'a'")
      assert(!report.conforms)
    }
  }

}
