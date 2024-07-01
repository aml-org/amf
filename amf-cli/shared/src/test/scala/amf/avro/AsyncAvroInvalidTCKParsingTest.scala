package amf.avro

import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint, AvroHint}


class AsyncAvroInvalidTCKParsingTest extends AsyncAvroCycleTest {
  override def basePath: String = s"amf-cli/shared/src/test/resources/avro/tck/apis/invalid/"

  // Test invalid APIs
  fs.syncFile(s"$basePath").list.foreach { api =>
    if (api.endsWith(".yaml") && !api.endsWith(".dumped.yaml")) {
      test(s"Avro TCK > Apis > Invalid > $api: parsing fails as expected") {
        cycle(api, api.replace(".yaml", ".jsonld"), Async20YamlHint, AmfJsonHint)
      }
    }

    if (api.endsWith(".json") && !api.endsWith(".dumped.json")) {
      test(s"Avro TCK > Schemas > Invalid > $api: parsing fails as expected") {
        cycle(api, api.replace(".json", ".jsonld"), AvroHint, AmfJsonHint)
      }
    }
  }
}
