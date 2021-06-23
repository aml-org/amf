package amf.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{Amf, Oas20YamlHint, Oas30YamlHint, PayloadYamlHint}
import amf.io.FunSuiteCycleTests

class OasRecursiveFilesCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/references/oas/oas-references/"

  override def renderOptions() = RenderOptions().withSourceMaps.withPrettyPrint.withoutFlattenedJsonLd
  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-2-root.yaml", "oas-2-root.jsonld", Oas20YamlHint, Amf)
  }

  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-2-ref.yaml", "oas-2-ref.jsonld", Oas20YamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-3-root.yaml", "oas-3-root.jsonld", Oas30YamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-3-ref.yaml", "oas-3-ref.jsonld", PayloadYamlHint, Amf)
  }
}
