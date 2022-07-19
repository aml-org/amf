package amf.validation

import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas30JsonHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json")
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json")
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json")
  }

  test("In body binding param") {
    checkValid("/parameters/binding-body.json")
  }

  test("Valid media types") {
    checkValid("/payloads/valid-media-types.json")
  }

  test("formData payload with ref") {
    checkValid("/payloads/form-data-with-ref.json")
  }

  test("Api with external docs") {
    checkValid("/apiWithExternalDocs.json")
  }

  test("Local references with same property name") {
    checkValid("/zenoti-reduced.json")
  }

  test("$ref to a swagger 2.0 document") {
    checkValid("/ref-to-doc/api.json")
  }

  test("oas document with empty produces") {
    checkValid("/oas-produces/api.json")
  }

  test("path parameter must have required set to true") {
    checkValid("/path-parameter-required/required-set-to-true.json")
  }

  test("parameters with same name and different binding") {
    checkValid("/duplicate-parameters/parameters-with-same-name.json")
  }

  test("When file parameter is defined, consumes must have specific value") {
    checkValid("/file-parameter-consumes/valid-consumes-for-file-parameter.json")
  }

  test("File parameter with correct binding and consumes") {
    checkValid("file-parameter/file-parameter-correct-properties.json")
  }

  test("multiple examples defined with valid mime type with produces general definition") {
    checkValid("examples-mime-type/valid-mime-type.json")
  }

  test("Callback definitions") {
    validate("../upanddown/oas3/one-subscription-multiple-callbacks.json")
  }

  test("Security requirements") {
    checkValid("oas-security/api-with-security-requirements.json")
  }

  test("Nested responses") {
    checkValid("oas2/nested-libraries/response-response/api.json")
  }

  test("Nested responses type") {
    checkValid("oas2/nested-libraries/response-type/api.json")
  }

  test("Parse amf union extension") {
    checkValid("oas2/amf-union-extension.json")
  }

  // Check http inner reference.
  ignore("Http with # reference") {
    checkValid("/http-with-hashtag/http-with-hashtag.json")
  }

}
