package amf.validation
import amf.{Oas20Profile, Raml08Profile}
import amf.core.remote.{Hint, Raml08YamlHint, Raml10YamlHint}

class ResolutionReportTest extends ResolutionForUniquePlatformReportTest {

  override val basePath = "file://amf-cli/shared/src/test/resources/validations"

  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/resolution/"

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
    checkReport("/types/unresolve-inherits-sameclass.raml")
  }

  test("Unresolve inheritance from different clases") {
    checkReport("/types/unresolve-inherits-differentclass.raml", Some("unresolve-inherits-differentclass.report"))
  }

  test("Valid recursive optional property with items recursive") {
    checkReport("/types/optional-prop-item-recursive.raml")
  }

  test("USed from propr recursive optional property with items recursive") {
    checkReport("/types/optional-prop-item-recursive-used.raml")
  }

  test("Valid type with array property with items recursive") {
    checkReport("/types/recursive-optional-array-item-type.raml")
  }

  test("Items recursive without min items") {
    checkReport("/types/arrays/items-recursive-allowed.raml")
  }

  test("Items recursive with min items") {
    checkReport("/types/arrays/recursive-items.raml", Some("recursive-items.report"))
  }

  test("Test direct link to future reference") {
    checkReport("/unresolve.raml")
  }

  test("Test recursives valid") {
    checkReport("/shapes/valid-recursives.raml")
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
    checkReport("/overlays/valid-insertion-declaration-annotation/overlay.raml")
  }

  test("Valid declaration insertion in overlay") {
    checkReport("/overlays/valid-insertion-declaration-type/overlay.raml")
  }

  test("Valid documentation insertion in overlay") {
    checkReport("/overlays/valid-insertion-documentation/overlay.raml")
  }

  test("Valid documentation insertion in overlay 2") {
    checkReport("/overlays/valid-insertion-documentation-2/overlay.raml")
  }

  test("Valid operation example insertion in overlay") {
    checkReport("/overlays/valid-insertion-operation-example/overlay.raml")
  }

  test("Valid type example insertion in overlay") {
    checkReport("/overlays/valid-insertion-type-example/overlay.raml")
  }

  test("Valid override title insertion in overlay") {
    checkReport("/overlays/valid-override-title/overlay.raml")
  }

  test("Valid complex overlay") {
    checkReport("/overlays/valid-complex/overlay2.raml")
  }

  test("Valid insertion example of a type overlay") {
    checkReport("/overlays/valid-insertion-example-type-object/overlay.raml")
  }

  test("Valid insertion operation description") {
    checkReport("/overlays/valid-insertion-operation-description/overlay.raml")
  }

  test("Inline single prop inheritance from union") {
    checkReport("/types/inline-union-inheritance.raml")
  }

  test("Inline single prop inheritance from complex") {
    checkReport("/types/inline-union-inheritance2.raml")
  }

  test("Test inner reference inside json schema") {
    validate("/json-inner-ref/case1/api.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Test example and jsonschema with same name in diff folder") {
    checkReport("/json-inner-ref/repeat-file-folder/api.raml")
  }

  test("Test ref in root at json schema with other ref") {
    checkReport("/json-inner-ref/ref-in-root-jsonschema/api.raml")
  }

  test("Test reference to property and items at declared type oas") {
    checkReport("/reference-jsonschema-property/api.raml", profile = Oas20Profile)
  }

  test("Test reference to non existing entry at oas definitions") {
    checkReport("/reference-jsonschema-property/bad-link.raml",
                profile = Oas20Profile,
                golden = Some("ref-jsonschema-bad-link.report"))
  }

  test("Test resolve double var at resource type (resolved link replacement)") {
    checkReport("/resource_types/resolved-link-replacement/api.raml")
  }

  test("Test dataType fragment with nested type fragment in library") {
    checkReport("/library/nested-dataType/main.raml")
  }

  test("Same type inheritance recursion") {
    checkReport("/same-type-inheritance-recursion.raml", Some("same-type-inheritance-recursion.report"))
  }

  override val hint: Hint = Raml10YamlHint
}
