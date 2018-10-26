package amf.validation

import amf.Raml08Profile
import amf.core.remote.{Hint, RamlYamlHint}

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
    checkValid("08/external_fragment_test.raml")
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
    checkValid("08/some.raml", Raml08Profile)
  }

  // this test has not sense for me. What is testing? parameter parsing? validation? it should not be here.
  // delete tck-examples folder
  test("Raml 0.8 Parameter") {
    checkValid("/tck-examples/query-parameter.raml", Raml08Profile)
  }

  test("Empty parameter validation") {
    checkValid("/08/empty-param.raml", Raml08Profile)
  }

  test("Empty describe by") {
    checkValid("securitySchemes/empty-described-by.raml")
  }

  test("Empty uri parameters") {
    checkValid("parameters/empty-uri-parameters.raml")
  }

  test("Date parameter validation") {
    checkValid("08/empty-param.raml", Raml08Profile)
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
    checkValid("/resource_types/media-type-resource-type.raml")
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
    checkValid("/declarations/api.raml", profile = Raml08Profile)
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

  override val hint: Hint = RamlYamlHint
}
