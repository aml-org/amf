package amf.validation

import amf.{Oas30Profile, OasProfile}
import amf.core.remote.{Hint, OasJsonHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json", OasProfile)
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json", OasProfile)
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json", OasProfile)
  }

  test("In body binding param") {
    checkValid("/parameters/binding-body.json", OasProfile)
  }

  test("Valid media types") {
    checkValid("/payloads/valid-media-types.json", OasProfile)
  }

  test("formData payload with ref") {
    checkValid("/payloads/form-data-with-ref.json", OasProfile)
  }

  test("Api with external docs") {
    checkValid("/apiWithExternalDocs.json", OasProfile)
  }

  test("Local references with same property name") {
    checkValid("/zenoti-reduced.json", OasProfile)
  }

  test("$ref to a swagger 2.0 document") {
    checkValid("/ref-to-doc/api.json", OasProfile)
  }

  test("oas document with empty produces") {
    checkValid("/oas-produces/api.json", OasProfile)
  }

  test("path parameter must have required set to true") {
    checkValid("/path-parameter-required/required-set-to-true.json", OasProfile)
  }

  test("parameters with same name and different binding") {
    checkValid("/duplicate-parameters/parameters-with-same-name.json", OasProfile)
  }

  test("When file parameter is defined, consumes must have specific value") {
    checkValid("/file-parameter-consumes/valid-consumes-for-file-parameter.json", OasProfile)
  }

  test("File parameter with correct binding and consumes") {
    checkValid("file-parameter/file-parameter-correct-properties.json", OasProfile)
  }

  test("multiple examples defined with valid mime type with produces general definition") {
    checkValid("examples-mime-type/valid-mime-type.json", OasProfile)
  }

  test("Callback definitions") {
    checkValid("../upanddown/oas3/one-subscription-multiple-callbacks.json", Oas30Profile)
  }

  test("Security requirements") {
    checkValid("oas-security/api-with-security-requirements.json", OasProfile)
  }

  test("Nested responses") {
    checkValid("oas2/nested-libraries/response-response/api.json", OasProfile)
  }

  test("Nested responses type") {
    checkValid("oas2/nested-libraries/response-type/api.json", OasProfile)
  }

  // Check http inner reference.
  ignore("Http with # reference") {
    checkValid("/http-with-hashtag/http-with-hashtag.json", OasProfile)
  }

  override val hint: Hint = OasJsonHint
}
