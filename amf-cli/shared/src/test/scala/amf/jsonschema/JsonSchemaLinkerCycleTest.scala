package amf.jsonschema

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.OASConfiguration.OAS20
import amf.apicontract.client.scala.RAMLConfiguration.RAML10
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class JsonSchemaLinkerCycleTest extends FunSuiteCycleTests with JsonSchemaDocumentTest {

  override val basePath: String = "amf-cli/shared/src/test/resources/jsonschema/"

  test("RAML to JSON-LD - draft-7 definitions include") {
    withJsonSchema("schemas/simple.json", config(RAML10())).flatMap { case (config, _) =>
      cycle("apis/api.raml", "cycled/api.raml.jsonld", Raml10YamlHint, AmfJsonHint, amfConfig = Some(config))
    }
  }

  test("RAML to JSON-LD - draft-2019 $defs include") {
    withJsonSchema("schemas/simple-2019.json", config(RAML10())).flatMap { case (config, _) =>
      cycle("apis/api-2019.raml", "cycled/api-2019.raml.jsonld", Raml10YamlHint, AmfJsonHint, amfConfig = Some(config))
    }
  }

  test("OAS to JSON-LD - draft-7 definitions $ref") {
    withJsonSchema("schemas/simple.json", config(OAS20())).flatMap { case (config, _) =>
      cycle("apis/oas20.yaml", "cycled/oas20.yaml.jsonld", Raml10YamlHint, AmfJsonHint, amfConfig = Some(config))
    }
  }

  test("OAS to JSON-LD - draft-2019 $defs $ref") {
    withJsonSchema("schemas/simple-2019.json", config(OAS20())).flatMap { case (config, _) =>
      cycle(
        "apis/oas20-2019.yaml",
        "cycled/oas20-2019.yaml.jsonld",
        Raml10YamlHint,
        AmfJsonHint,
        amfConfig = Some(config)
      )
    }
  }

  private def config(base: AMFConfiguration) = {
    base.withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris.withSourceMaps)
  }
}
