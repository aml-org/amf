package amf.validation

import amf.core.client.common.validation.{Oas20Profile, Oas30Profile}
import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas30JsonHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json", Oas20Profile)
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json", Oas20Profile)
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json", Oas20Profile)
  }

  test("In body binding param") {
    checkValid("/parameters/binding-body.json", Oas20Profile)
  }

  test("Valid media types") {
    checkValid("/payloads/valid-media-types.json", Oas20Profile)
  }

  test("formData payload with ref") {
    checkValid("/payloads/form-data-with-ref.json", Oas20Profile)
  }

  test("Api with external docs") {
    checkValid("/apiWithExternalDocs.json", Oas20Profile)
  }

  test("Local references with same property name") {
    checkValid("/zenoti-reduced.json", Oas20Profile)
  }

  test("$ref to a swagger 2.0 document") {
    checkValid("/ref-to-doc/api.json", Oas20Profile)
  }

  test("oas document with empty produces") {
    checkValid("/oas-produces/api.json", Oas20Profile)
  }

  test("path parameter must have required set to true") {
    checkValid("/path-parameter-required/required-set-to-true.json", Oas20Profile)
  }

  test("parameters with same name and different binding") {
    checkValid("/duplicate-parameters/parameters-with-same-name.json", Oas20Profile)
  }

  test("When file parameter is defined, consumes must have specific value") {
    checkValid("/file-parameter-consumes/valid-consumes-for-file-parameter.json", Oas20Profile)
  }

  test("File parameter with correct binding and consumes") {
    checkValid("file-parameter/file-parameter-correct-properties.json", Oas20Profile)
  }

  test("multiple examples defined with valid mime type with produces general definition") {
    checkValid("examples-mime-type/valid-mime-type.json", Oas20Profile)
  }

  test("Callback definitions") {
    validate("../upanddown/oas3/one-subscription-multiple-callbacks.json",
             profile = Oas30Profile,
             overridedHint = Some(Oas30JsonHint))
  }

  test("Security requirements") {
    checkValid("oas-security/api-with-security-requirements.json", Oas20Profile)
  }

  test("Nested responses") {
    checkValid("oas2/nested-libraries/response-response/api.json", Oas20Profile)
  }

  test("Nested responses type") {
    checkValid("oas2/nested-libraries/response-type/api.json", Oas20Profile)
  }

  test("Parse amf union extension") {
    checkValid("oas2/amf-union-extension.json", Oas20Profile)
  }

  // Check http inner reference.
  ignore("Http with # reference") {
    checkValid("/http-with-hashtag/http-with-hashtag.json", Oas20Profile)
  }

  override val hint: Hint = Oas20JsonHint
}
