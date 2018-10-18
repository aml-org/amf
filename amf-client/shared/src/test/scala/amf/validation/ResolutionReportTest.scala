package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class ResolutionReportTest extends ResolutionForUniquePlatformReportTest {

  override val basePath = "file://amf-client/shared/src/test/resources/validations"

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/resolution/"

  test("Complex union resolution") {
    checkReport("/../resolution/empty_union/api.raml", Some("empty_union.report"))
  }

  test("Invalid property overriding") {
    checkReport("/types/invalid-property-overriding.raml", Some("invalid-property-overriding.report"))
  }

  test("Invalid recursive shape") {
    checkReport("/types/recursive-shape.raml", Some("recursive-shape.report"))
  }

  test("Inheritance facets validations become warnings") {
    checkReport("/types/inheritance-facets.raml", Some("inheritance-facets-warnings.report"))
  }

  test("Unresolve inheritance from same class") {
    checkReport("/types/unresolve-inherits-sameclass.raml", None)
  }

  test("Unresolve inheritance from different clases") {
    checkReport("/types/unresolve-inherits-differentclass.raml", Some("unresolve-inherits-differentclass.report"))
  }

  test("Valid recursive optional property with items recursive") {
    checkReport("/types/optional-prop-item-recursive.raml", None)
  }

  test("USed from propr recursive optional property with items recursive") {
    checkReport("/types/optional-prop-item-recursive-used.raml", None)
  }

  test("Valid type with array property with items recursive") {
    checkReport("/types/recursive-optional-array-item-type.raml", None)
  }

  test("Items recursive without min items") {
    checkReport("/types/arrays/items-recursive-allowed.raml", None)
  }

  test("Items recursive with min items") {
    checkReport("/types/arrays/recursive-items.raml", Some("recursive-items.report"))
  }

  test("Test direct link to future reference") {
    checkReport("/unresolve.raml", None)
  }

  test("Test recursives valid") {
    checkReport("/shapes/valid-recursives.raml", None)
  }

  test("Test recursives invalid") {
    checkReport("/shapes/invalid-recursives.raml", Some("invalid-recursives.report"))
  }

  test("Test unresolved shape in fragment") {
    validate("/shapes/frag-future-ref/api.raml")
  }

  test("Invalid insertion baseuri in overlay") {
    checkReport("/overlays/invalid-insertion-baseuri/overlay.raml", Some("overlay-invalid-insert-baseuri.report"))
  }

  test("Invalid insertion endpoint in overlay") {
    checkReport("/overlays/invalid-insertion-endpoint/overlay.raml", Some("overlay-invalid-insert-endpoint.report"))
  }

  test("Invalid insertion node in overlay") {
    checkReport("/overlays/invalid-insertion-node/overlay.raml", Some("overlay-invalid-insert-node.report"))
  }

  test("Invalid annotation override in overlay") {
    checkReport("/overlays/invalid-override-annotation/overlay.raml",
                Some("overlay-invalid-override-annotation.report"))
  }

  test("Invalid baseuri override in overlay") {
    checkReport("/overlays/invalid-override-baseuri/overlay.raml", Some("overlay-invalid-override-baseuri.report"))
  }

  test("Invalid declaration override in overlay") {
    checkReport("/overlays/invalid-override-declaration/overlay.raml",
                Some("overlay-invalid-override-declaration.report"))
  }

  test("Valid annotation insertion in overlay") {
    checkReport("/overlays/valid-insertion-declaration-annotation/overlay.raml", None)
  }

  test("Valid declaration insertion in overlay") {
    checkReport("/overlays/valid-insertion-declaration-type/overlay.raml", None)
  }

  test("Valid documentation insertion in overlay") {
    checkReport("/overlays/valid-insertion-documentation/overlay.raml", None)
  }

  test("Valid documentation insertion in overlay 2") {
    checkReport("/overlays/valid-insertion-documentation-2/overlay.raml", None)
  }

  test("Valid operation example insertion in overlay") {
    checkReport("/overlays/valid-insertion-operation-example/overlay.raml", None)
  }

  test("Valid type example insertion in overlay") {
    checkReport("/overlays/valid-insertion-type-example/overlay.raml", None)
  }

  test("Valid override title insertion in overlay") {
    checkReport("/overlays/valid-override-title/overlay.raml", None)
  }

  test("Valid complex overlay") {
    checkReport("/overlays/valid-complex/overlay2.raml", None)
  }

  test("Inline single prop inheritance from union") {
    checkReport("/types/inline-union-inheritance.raml", None)
  }

  test("Inline single prop inheritance from complex") {
    checkReport("/types/inline-union-inheritance2.raml", None)
  }

  test("Test inner reference inside json schema") {
    checkReport("/json-inner-ref/case1/api.raml", None)
  }

  test("Test example and jsonschema with same name in diff folder") {
    checkReport("/json-inner-ref/repeat-file-folder/api.raml", None)
  }

  test("Test ref in root at json schema with other ref") {
    checkReport("/json-inner-ref/ref-in-root-jsonschema/api.raml", None)
  }

  override val hint: Hint = RamlYamlHint
}
