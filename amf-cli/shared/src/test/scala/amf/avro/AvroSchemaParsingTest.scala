package amf.avro

class AvroSchemaParsingTest extends AvroSchemaCycleTest {
  test("Can parse an array") {
    cycle("array.json", "array.jsonld")
  }

  test("Can parse an enum") {
    cycle("enum.json", "enum.jsonld")
  }

  test("Can parse a fixed shape") {
    cycle("fixed.json", "fixed.jsonld")
  }

  test("Can parse a map") {
    cycle("map.json", "map.jsonld")
  }

  test("Can parse a record with a recursive reference") {
    cycle("record.json", "record.jsonld")
  }
}
