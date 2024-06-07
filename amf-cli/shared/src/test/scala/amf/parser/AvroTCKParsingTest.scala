package amf.parser

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, AsyncApiYamlHint}
import amf.cycle.AvroFunSuiteCycleTests

class AvroTCKParsingTest extends AvroFunSuiteCycleTests {
  override def basePath: String = s"amf-cli/shared/src/test/resources/avro/"

  // Test valid APIs
  fs.syncFile(s"$basePath/async-avro-valid").list.foreach { api =>
    if (api.endsWith(".yaml") && !api.endsWith(".dumped.yaml")) {
      test(s"Avro TCK > Apis > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".yaml", ".jsonld"), AsyncApiYamlHint, AmfJsonHint)
      }

      test(s"Avro TCK > Apis > Valid > $api: dumped YAML matches golden") {
        cycle(api, api.replace(".yaml", ".dumped.yaml"), AsyncApiYamlHint, AsyncApiYamlHint)
      }
    }

    if (api.endsWith(".json") && !api.endsWith(".dumped.json")) {
      test(s"Avro TCK > Schemas > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".json", ".jsonld"), AvroHint, AmfJsonHint)
      }

      test(s"Avro TCK > Schemas > Valid > $api: dumped JSON matches golden") {
        cycle(api, api.replace(".json", ".dumped.json"), AvroHint, AvroHint)
      }
    }
  }

  // Test invalid APIs
  fs.syncFile(s"$basePath/async-avro-valid").list.foreach { api =>
    if (api.endsWith(".yaml") && !api.endsWith(".dumped.yaml")) {
      test(s"Avro TCK > Apis > Invalid > $api: parsing fails as expected") {
        intercept[Exception] {
          cycle(api, api.replace(".yaml", ".jsonld"), AsyncApiYamlHint, AmfJsonHint)
        }
      }
    }

    if (api.endsWith(".json") && !api.endsWith(".dumped.json")) {
      test(s"Avro TCK > Schemas > Invalid > $api: parsing fails as expected") {
        intercept[Exception] {
          cycle(api, api.replace(".json", ".jsonld"), AvroHint, AmfJsonHint)
        }
      }
    }
  }

  ignore("specific avro api dumped JSON matches golden") {
    val api = "specific-invalid.api.yaml"
    intercept[Exception] {
      cycle(api, api.replace(".yaml", ".jsonld"), AsyncApiYamlHint, AmfJsonHint)
    }
  }

  test("Avro TCK > Apis > Valid > example-valid.yaml: dumped Flattened JSON matches golden") {
    cycle("example-valid.yaml", "example-valid.flattened.jsonld", AsyncApiYamlHint, AmfJsonHint, renderOptions = Some(RenderOptions().withFlattenedJsonLd.withPrettyPrint))
  }

  override def renderOptions(): RenderOptions = RenderOptions().withoutFlattenedJsonLd.withPrettyPrint
}
