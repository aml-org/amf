package amf.parser
import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, AmfJsonHint}
import amf.io.FunSuiteCycleTests

class GraphParsingTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/graphs/"

  test("Parse api with context with expanded term definitions") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.jsonld", "api.golden.jsonld", AmfJsonHint, Amf, renderOptions = Some(ro))
  }
}
