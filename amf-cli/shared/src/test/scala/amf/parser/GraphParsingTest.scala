package amf.parser
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.remote.{Amf, AmfJsonHint}
import amf.io.FunSuiteCycleTests

class GraphParsingTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/graphs/"

  test("Parse api with context with expanded term definitions") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.jsonld", "api.golden.jsonld", AmfJsonHint, Amf, renderOptions = Some(ro))
  }

  test("Parse api with link target maps") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.jsonld",
          "api.golden.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro),
          directory = s"${basePath}link-target-map/")
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

  test("Parse api with @base and absolute IRIs - flattened") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.flattened.jsonld",
          "api.golden.flattened.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro),
          directory = s"$basePath/base-and-absolute-iris/")
  }

  test("Parse api with @base and absolute IRIs - expanded") {
    val ro = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd
    cycle("api.source.expanded.jsonld",
          "api.golden.expanded.jsonld",
          AmfJsonHint,
          Amf,
          renderOptions = Some(ro),
          directory = s"$basePath/base-and-absolute-iris/")
  }

}
