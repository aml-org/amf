package amf.validation

import amf.core.client.common.validation.Raml08Profile
import amf.core.internal.remote.{Hint, Raml08YamlHint, Raml10YamlHint}

class ValidRamlModelParserTest extends ValidModelTest {
  test("Valid baseUri validations test") {
    checkValid("webapi/valid_baseuri.raml")
  }

  test("Example validation of a resource type") {
    checkValid("webapi/valid_baseuri.raml")
  }

  test("Type inheritance with enum") {
    checkValid("types/enum-inheritance.raml")
  }

  test("External raml 0.8 fragment") {
    validate("08/external_fragment_test.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Headers example array validation") {
    checkValid("production/headers.raml")
  }

  test("Annotation target usage") {
    checkValid("annotations/target-annotations.raml")
  }

  test("Spec extension") {
    checkValid("extends/extension.raml")
  }

  test("Spec overlay 1") {
    checkValid("extends/overlay1.raml")
  }

  test("Spec overlay 2") {
    checkValid("extends/overlay2.raml")
  }

  test("Spec resource type fragment") {
    checkValid("resource_types/fragment.raml")
  }

  // what its testing this? the api is empty
  test("08 Validation") {
    validate("08/some.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  // this test has not sense for me. What is testing? parameter parsing? validation? it should not be here.
  // delete tck-examples folder
  test("Raml 0.8 Parameter") {
    validate("/tck-examples/query-parameter.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Empty parameter validation") {
    validate("/08/empty-param.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Empty describe by") {
    validate("security-schemes/empty-described-by.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Empty uri parameters") {
    checkValid("parameters/empty-uri-parameters.raml")
  }

  test("Date parameter validation") {
    validate("08/empty-param.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Recursive property") {
    checkValid("/recursive-property.raml")
  }

  test("Link to declared type with recursive optional properties") {
    checkValid("/shapes/link-declared-recursive-optional.raml")
  }

  test("Double link resource type used twice") {
    checkValid("/resource_types/double-linked-resourcetype-twice/api.raml")
  }

  test("Test optional node in resource type without var") {
    checkValid("/resource_types/optional-node.raml")
  }

  test("Test array without item type validation") {
    checkValid("/types/arrays/array-without-items.raml")
  }

  test("Test media type with + char in resource type") {
    validate("/resource_types/media-type-resource-type.raml",
             profile = Raml08Profile,
             overridedHint = Some(Raml08YamlHint))
  }

  test("Empty responses") {
    checkValid("/operation/empty-responses.raml")
  }

  test("Test recursive optional shape") {
    checkValid("/types/recursive-optional-property.raml")
  }

  test("Test valid recursive union recursive") {
    checkValid("/shapes/union-recursive.raml") // shapes and types? we should have only one folder
  }

  test("Test different declarations with same name") {
    validate("/declarations/api.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Test empty usage/uses entries") {
    checkValid("/empty-usage-uses.raml")
  }

  test("Object array") {
    checkValid("/types/object-array.raml")
  }

  test("Default type validation") {
    validate("shapes/default_type.raml")
  }

  test("Default string type validation") {
    validate("shapes/default-definition-type.raml")
  }

  test("Float validation") {
    validate("examples/float-validation/api.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Multiple example fragments") {
    validate("examples/multiple-example-fragments/api.raml")
  }

  test("Included json schema with ref to himself with file name") {
    validate("shapes/ref-recursive-samefilename/api.raml",
             profile = Raml08Profile,
             overridedHint = Some(Raml08YamlHint))
  }

  test("Included json schema with ref inner ref to another") {
    checkValid("shapes/inner-ref-from-jsonschema/api.raml")
  }

  test("Included json schema fragment with inner ref to another") {
    checkValid("shapes/inner-ref-from-jsonschema-fragment/input.raml")
  }

  test("Included path reference with inner ref to main api") {
    checkValid("shapes/inner-ref-from-jsonschema/api.raml")
  }

  test("Types with non string property names") {
    checkValid("types/non-string-property-names.raml")
  }

  test("date-time-only union with nil in type expression") {
    checkValid("date-time-only-union/date-time-only-union.raml")
  }

  test("Valid use of recursive shape in json schemas") {
    validate("valid-recursive/valid-recursive.raml", profile = Raml08Profile, overridedHint = Some(Raml08YamlHint))
  }

  test("Nil union with '?' reference to type") {
    checkValid("nil-union-reference/nil-union-reference.raml")
  }

  test("Multiple inheritance with complex case") {
    checkValid("multiple-inheritance-complex/api.raml")
  }

  test("MultipleOf with decimal precision") {
    checkValid("multipleOfDataType.raml")
  }

  test("MultipleOf with decimal limit precision") {
    checkValid("multipleOfDataTypeLimitPrecision.raml")
  }

  test("Declaration keys as scalars") {
    checkValid("scalar-declaration-keys.raml")
  }

  test("Valid XML attribute property scalar") {
    checkValid("validXmlAttributeScalar.raml")
  }

  test("Large int valid test") {
    checkValid("large-int/large-int.raml")
  }

  test("Escape characters in value of resource type") {
    checkValid("escaped-chars-resource-type/using-resource-type.raml")
  }

  test("Remove BOM character from files") {
    checkValid("remove-bom/remove-bom.raml")
  }

  test("Default facet defined in union type") {
    checkValid("valid-default-facet-of-union-type.raml")
  }

  test("Protocols with case insensitive values") {
    checkValid("protocols/valid-case-insensitive-values.raml")
  }

  test("Annotation with type inheritance and allowedTargets facet") {
    checkValid("annotations/inheritance-with-allowed-targets.raml")
  }

  test("Date only with fraction example") {
    validate("fraction-date-only/fraction-date-only.raml")
  }

  test("Annotation type definition forward referencing data type with same name") {
    checkValid("annotations/forward-ref-to-type.raml")
  }

  test("Using unknown format in json schema should not fail") {
    checkValid("date-format/api.raml")
  }

  test("Union with generic array") {
    checkValid("union-generic-array.raml")
  }

  test("Nested yaml aliases") {
    checkValid("yaml-nested-alias.raml")
  }

  test("Validating schemas with special chars") {
    checkValid("special-chars.raml")
  }

  test("Prioritize library alias defined in fragment over root file") {
    checkValid("library-alias-repeated-in-root/root.raml")
  }

  test("Resolve correct library alias defined in root file") {
    checkValid("libraries-resolve-correct-alias/promotions.raml")
  }

  test("Validate api that has schemas with missing closures which are handled in emission") {
    checkValid("complex-closure-case/api.raml")
  }

  test("Nested json schema references") {
    checkValid("json-schema-nested-refs/api.raml")
  }

  test("Valid recursive model defined in trait and operation") {
    checkValid("recursion-in-trait.raml")
  }

  test("Valid SecurityScheme scope for empty scopes in declaration") {
    checkValid("security-schema-scope.raml")
  }

  test("Referencing external example with @type key") {
    checkValid("external-example/api.raml")
  }

  test("example has same filename as a type filename") {
    checkValid("/raml/examples/same-filename-example.raml")
  }

  test("using number larger than long in schema definition") {
    checkValid("/raml/big-number-in-schema.raml")
  }

  test("Resolve !include with spaces") {
    checkValid("/raml/api-with-includes-with-spaces/api.raml")
  }

  test("Trait definition with numeric name") {
    checkValid("/raml/trait-definition-numeric-name.raml")
  }

  override val hint: Hint = Raml10YamlHint
}
