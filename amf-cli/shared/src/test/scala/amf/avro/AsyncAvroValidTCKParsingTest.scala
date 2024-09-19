package amf.avro

import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint, AvroHint}

class AsyncAvroValidTCKParsingTest extends AsyncAvroCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/avro/tck/apis/valid/"

  // Test valid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".yaml") && !api.endsWith(".dumped.yaml")) {
      test(s"Avro TCK > Apis > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".yaml", ".jsonld"), Async20YamlHint, AmfJsonHint)
      }

      test(s"Avro TCK > Apis > Valid > $api: dumped YAML matches golden") {
        cycle(api, api.replace(".yaml", ".dumped.yaml"), Async20YamlHint, Async20YamlHint)
      }
    }
  }

  test("test async with an AVRO Schema parsing") {
    cycle("avro-map-values-union.yaml", "avro-map-values-union.dumped.yaml", Async20YamlHint, Async20YamlHint)
  }
}
