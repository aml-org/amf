package amf.emit

import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

class Oas31CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-cli/shared/src/test/resources/upanddown/oas31/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cyclesOas31Json: Seq[FixtureData] = Seq(
    FixtureData(
      "OAS 3.1 license identifier field in JSON",
      "oas-31-license-identifier.json",
      "oas-31-license-identifier.dumped.json"
    ),
    FixtureData(
      "OAS 3.1 info summary field in JSON",
      "oas-31-info-summary.json",
      "oas-31-info-summary.dumped.json"
    ),
    FixtureData(
      "OAS 3.1 full in JSON",
      "oas-31-full.json",
      "oas-31-full.dumped.json"
    )
  )

  cyclesOas31Json.foreach { f =>
    test(s"${f.name} - oas31 to oas31") {
      cycle(f.apiFrom, f.apiTo, Oas31JsonHint, Oas31JsonHint)
    }
  }

  val cyclesOas31Yaml: Seq[FixtureData] = Seq(
    FixtureData(
      "OAS 3.1 annotation in discriminator field",
      "oas-31-discriminator-ann.yaml",
      "oas-31-discriminator-ann.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 $ref object summary and description",
      "oas-31-ref-fields.yaml",
      "oas-31-ref-fields.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 license identifier field in YAML",
      "oas-31-license-identifier.yaml",
      "oas-31-license-identifier.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 info summary field in YAML",
      "oas-31-info-summary.yaml",
      "oas-31-info-summary.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 security schemes in YAML",
      "oas-31-security-schemes.yaml",
      "oas-31-security-schemes.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 full in YAML",
      "oas-31-full.yaml",
      "oas-31-full.dumped.yaml"
    )
  )

  cyclesOas31Yaml.foreach { f =>
    test(s"${f.name} - oas31 to oas31") {
      cycle(f.apiFrom, f.apiTo, Oas31YamlHint, Oas31YamlHint)
    }
  }

  test(s"OAS 3.1 full to jsonLD") {
    cycle("oas-31-full.yaml", "oas-31-full.jsonld", Oas31YamlHint, AmfJsonHint)
  }
}
