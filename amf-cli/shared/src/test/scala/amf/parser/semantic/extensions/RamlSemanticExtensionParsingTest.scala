package amf.parser.semantic.extensions

class RamlSemanticExtensionParsingTest extends SemanticExtensionParsingTest {
  override val basePath: String = "amf-cli/shared/src/test/resources/semantic-extensions/raml"

  test("Parse simple semantic extension test") {
    cycle("api.raml", "api.jsonld", "extension-dialect.yaml", "simple", "application/ld+json")
  }

}
