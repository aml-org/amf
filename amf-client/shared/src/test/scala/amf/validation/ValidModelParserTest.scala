package amf.validation

import amf.ProfileNames

class ValidModelParserTest extends ValidModelTest {
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
    checkValid("08/some.raml", ProfileNames.RAML08)
  }

  // this test has not sense for me. What is testing? parameter parsing? validation? it should not be here.
  // delete tck-examples folder
  test("Raml 0.8 Parameter") {
    checkValid("/tck-examples/query-parameter.raml", ProfileNames.RAML08)
  }

}
