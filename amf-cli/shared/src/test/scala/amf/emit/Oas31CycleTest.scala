package amf.emit

import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

// TODO: it's not rendering new 'examples' facet
class Oas31CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-cli/shared/src/test/resources/upanddown/oas31/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cyclesOas31Json: Seq[FixtureData] = Seq(
    FixtureData(
      "OAS 3.1 PetStore API in JSON",
      "oas-petstore-31.json",
      "oas-petstore-31.dumped.json"
    )
  )

  cyclesOas31Json.foreach { f =>
    test(s"${f.name} - oas31 to oas31") {
      cycle(f.apiFrom, f.apiTo, Oas31JsonHint, Oas31JsonHint)
    }
  }

  val cyclesOas31Yaml: Seq[FixtureData] = Seq(
    FixtureData(
      "OAS 3.1 PetStore API in YAML",
      "oas-petstore-31.yaml",
      "oas-petstore-31.dumped.yaml"
    ),
    FixtureData(
      "OAS 3.1 annotation in discriminator field",
      "oas-31-discriminator-ann.yaml",
      "oas-31-discriminator-ann.dumped.yaml"
    )
  )

  cyclesOas31Yaml.foreach { f =>
    test(s"${f.name} - oas31 to oas31") {
      cycle(f.apiFrom, f.apiTo, Oas31YamlHint, Oas31YamlHint)
    }
  }
}
