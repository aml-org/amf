package amf.avro

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.AsyncAPIConfiguration.Async20
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint}
import amf.io.FunSuiteCycleTests

class AvroSchemaLinkerCycleTest extends FunSuiteCycleTests with AvroSchemaDocumentTest {

  override val basePath: String = "amf-cli/shared/src/test/resources/avro/doc/"

  test("ASYNC to JSON-LD - AVRO Schema JSON include") {
    withAvroSchema("schemas/schema-1.9.0.json", config(Async20())).flatMap { case (config, _) =>
      cycle(
        "apis/async-basic-json.yaml",
        "cycled/async-basic-json.yaml.jsonld",
        Async20YamlHint,
        AmfJsonHint,
        amfConfig = Some(config)
      )
    }
  }

  test("ASYNC to JSON-LD - AVRO Schema AVSC include") {
    withAvroSchema("schemas/schema-1.9.0.avsc", config(Async20())).flatMap { case (config, _) =>
      cycle(
        "apis/async-basic-avsc.yaml",
        "cycled/async-basic-avsc.yaml.jsonld",
        Async20YamlHint,
        AmfJsonHint,
        amfConfig = Some(config)
      )
    }
  }

  private def config(base: AMFConfiguration): AMFConfiguration = {
    base.withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris.withSourceMaps)
  }
}
