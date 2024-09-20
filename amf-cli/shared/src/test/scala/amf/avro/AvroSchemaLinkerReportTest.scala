package amf.avro

import amf.apicontract.client.scala.AsyncAPIConfiguration.Async20
import amf.apicontract.client.scala.RAMLConfiguration.RAML10
import amf.core.internal.unsafe.PlatformSecrets
import amf.validation.UniquePlatformReportGenTest

class AvroSchemaLinkerReportTest extends UniquePlatformReportGenTest with AvroSchemaDocumentTest with PlatformSecrets {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/avro/doc/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/avro/doc/reports/"

  test("invalid - RAML including AVRO Schema Document") {
    withAvroSchema("schemas/schema-1.9.0.json", RAML10()).flatMap { case (config, _) =>
      validate(
        "apis/invalid-avro-ref.raml",
        Some("invalid-avro-ref.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("invalid - ASYNC referencing AVRO Schema Document invalid inner path") {
    withAvroSchema("schemas/schema-1.9.0.json", Async20()).flatMap { case (config, _) =>
      validate(
        "apis/invalid-async-ref.yaml",
        Some("invalid-async-ref.yaml.report"),
        configOverride = Some(config)
      )
    }
  }
}
