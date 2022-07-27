package amf.jsonschema

import amf.apicontract.client.scala.OASConfiguration.OAS20
import amf.apicontract.client.scala.RAMLConfiguration.RAML10
import amf.validation.UniquePlatformReportGenTest

class JsonSchemaLinkerReportTest extends UniquePlatformReportGenTest with JsonSchemaDocumentTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/jsonschema/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/jsonschema/reports/"

  test("invalid - RAML include not to definitions or $defs to 2019-09 document") {
    withJsonSchema("schemas/simple-2019.json", RAML10()).flatMap { case (config, _) =>
      validate(
        "apis/invalid-include-api-2019.raml",
        Some("invalid-include-api-2019.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - RAML include not to definitions to draft-7 document") {
    withJsonSchema("schemas/simple.json", RAML10()).flatMap { case (config, _) =>
      validate("apis/invalid-include-api.raml", Some("invalid-include-api.raml.report"), configOverride = Some(config))
    }
  }

  test("invalid - RAML include not to schema in definitions to draft-7 document") {
    withJsonSchema("schemas/simple.json", RAML10()).flatMap { case (config, _) =>
      validate(
        "apis/api-include-to-nowhere.raml",
        Some("api-include-to-nowhere.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - RAML include not to schema in definitions or $defs to 2019-09 document") {
    withJsonSchema("schemas/simple-2019.json", RAML10()).flatMap { case (config, _) =>
      validate(
        "apis/api-include-to-nowhere-2019.raml",
        Some("api-include-to-nowhere-2019.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - OAS $ref not to definitions or $defs to 2019-09 document") {
    withJsonSchema("schemas/simple-2019.json", OAS20()).flatMap { case (config, _) =>
      validate(
        "apis/invalid-ref-api-2019.yaml",
        Some("invalid-ref-api-2019.yaml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - OAS $ref not to definitions or $defs to draft-7 document") {
    withJsonSchema("schemas/simple.json", OAS20()) flatMap { case (config, _) =>
      validate("apis/invalid-ref-api.yaml", Some("invalid-ref-api.yaml.report"), configOverride = Some(config))
    }
  }

  test("invalid - OAS $ref not to schema in definitions to draft-7 document") {
    withJsonSchema("schemas/simple.json", OAS20()) flatMap { case (config, _) =>
      validate(
        "apis/invalid-ref-to-nowhere-api.yaml",
        Some("invalid-ref-to-nowhere-api.yaml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - OAS $ref not to schema in $defs or definitions to draft-2019 document") {
    withJsonSchema("schemas/simple-2019.json", OAS20()) flatMap { case (config, _) =>
      validate(
        "apis/invalid-ref-to-nowhere-2019-api.yaml",
        Some("invalid-ref-to-nowhere-2019-api.yaml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - OAS $ref to definitions in draft-2019 document when declarations key is $defs") {
    withJsonSchema("schemas/simple-2019.json", OAS20()) flatMap { case (config, _) =>
      validate(
        "apis/incorrect-def-key-ref.yaml",
        Some("incorrect-def-key-ref.yaml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - RAML $ref to definitions in draft-2019 document when declarations key is $defs") {
    withJsonSchema("schemas/simple-2019.json", RAML10()) flatMap { case (config, _) =>
      validate(
        "apis/incorrect-def-key-ref.raml",
        Some("incorrect-def-key-ref.raml.report"),
        configOverride = Some(config)
      )
    }
  }
}
