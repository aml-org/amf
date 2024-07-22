package amf.avro

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, AvroHint}

class AvroSchemaValidTCKTest extends AvroSchemaCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/upanddown/cycle/avro/valid/"

  fs.syncFile(s"$basePath").list.foreach { schema =>
    if (schema.endsWith(".json") && !schema.endsWith(".dumped.json")) {

      test(s"Avro Schema TCK > Schemas > Valid > $schema: JSON to JSON-LD matches golden") {
        cycle(schema, schema.replace(".json", ".jsonld"), AvroHint, AmfJsonHint, amfConfig = Some(avroConfiguration()))
      }
      // Todo: des-ingnore in emission
      test(s"Avro Schema TCK > Schemas > Valid > $schema: JSON to dumped JSON matches golden") {
        cycle(
          schema,
          schema.replace(".json", ".dumped.json"),
          AvroHint,
          AvroHint,
          amfConfig = Some(avroConfiguration())
        )
      }
    }
  }

  test("Test specific emission") {
    cycle(
      "record.json",
      "record.dumped.json",
      AvroHint,
      AvroHint,
      amfConfig = Some(avroConfiguration())
    )
  }

  test("Test specific parsing") {
    cycle(
      "map-with-values-schema.json",
      "map-with-values-schema.jsonld",
      AvroHint,
      AmfJsonHint,
      amfConfig = Some(avroConfiguration())
    )
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}

// todo: TCK invalid avro schemas. Uncomment to test validation when it's available
//class AvroSchemaInvalidTCKTest extends AvroSchemaCycleTest {
//  override def basePath: String = s"amf-cli/shared/src/test/resources/upandown/cycle/avro/invalid/"
//
//
//  fs.syncFile(s"$basePath/invalid").list.foreach { schema =>
//    if (schema.endsWith(".json") && !schema.endsWith(".dumped.json")){
//      ignore(s"Avro Schema TCK > Schemas > Invalid > $schema: JSON to JSON-LD matches golden") {
//          cycle(schema, schema.replace(".json", ".jsonld"), AvroHint, AmfJsonHint)
//      }
//      //Todo: des-ingnore in emission
//      ignore(s"Avro Schema TCK > Schemas > Valid > $schema: JSON to dumped JSON matches golden") {
//        cycle(schema, schema.replace(".json", ".dumped.json"), AvroHint, AvroHint)
//      }
//    }
//  }
//
//  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
//}
