package amf.parser
import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, AmfJsonHint}
import amf.io.FunSuiteCycleTests

class GraphParsingTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/graphs/"
  val upanddownPath: String     = "amf-client/shared/src/test/resources/upanddown/"

  test("Parse api with context with expanded term definitions") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.jsonld", "api.golden.jsonld", AmfJsonHint, Amf, renderOptions = Some(ro))
  }

  test("Conserve id values when parsing to maintain consistency with recursive fixpoints - flattened") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle("recursive-api.flattened.jsonld",
          "recursive-api.flattened.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro))
  }

  test("Conserve id values when parsing to maintain consistency with recursive fixpoints - expanded") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint
    cycle("recursive-api.expanded.jsonld", "recursive-api.expanded.jsonld", AmfJsonHint, Amf, renderOptions = Some(ro))
  }

  test("Parse compacted id fields correctly applying base - flattened source") {
    val ro = RenderOptions().withPrettyPrint
    cycle("recursive-api.flattened.jsonld",
          "recursive-api-full-uris.expanded.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro))
  }

  test("Parse compacted id fields correctly applying base - expanded source") {
    val ro = RenderOptions().withPrettyPrint
    cycle("recursive-api.expanded.jsonld",
          "recursive-api-full-uris.expanded.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro))
  }

  test("Parse expanded uri fields") {
    val ro = RenderOptions().withPrettyPrint
    cycle("recursive-api-full-uris.expanded.jsonld",
          "recursive-api-full-uris.expanded.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro))
  }

  test("Maintain precision in 16 digit long value when parsing") {
    val ro = RenderOptions().withPrettyPrint
    cycle(
      "large-integer-values.expanded.jsonld",
      "large-integer-values.expanded.jsonld",
      AmfJsonHint,
      Amf,
      directory = upanddownPath + "raml10/",
      renderOptions = Some(ro)
    )
  }

}
