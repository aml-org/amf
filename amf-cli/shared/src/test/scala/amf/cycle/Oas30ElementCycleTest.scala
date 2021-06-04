package amf.cycle

import amf.core.remote.{Oas20JsonHint, Oas30JsonHint, Vendor}

class Oas30ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-cli/shared/src/test/resources/cycle/oas30/"
  val upanddownPath: String     = "amf-cli/shared/src/test/resources/upanddown/oas3/"
  val vendor: Vendor            = Vendor.OAS30

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/login-response-emission.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - cookie parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/cookie-param.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - explicit header") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(5),
      "parameter/explicit-header.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - response header") {
    renderElement(
      "basic-headers-response.json",
      CommonExtractors.firstResponse.andThen(_.map(_.headers.head)),
      "basic-headers-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("parameter - header parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(3),
      "parameter/header-param.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - path parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(4),
      "parameter/path-param.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - query parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(1),
      "parameter/query-param.yaml",
      Oas30JsonHint
    )
  }

  test("parameter - external ref") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(0),
      "parameter/external-ref.yaml",
      Oas30JsonHint
    )
  }

  test("response - response with headers") {
    renderElement(
      "basic-headers-response.json",
      CommonExtractors.firstResponse,
      "reponse-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("example - full example") {
    renderElement(
      "basic-content.json",
      CommonExtractors.firstExample,
      "basic-content-example-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("example - external reference") {
    renderElement(
      "example/reference-external-example.json",
      CommonExtractors.firstExample,
      "example/external-example-emission.yaml",
      Oas30JsonHint
    )
  }

  test("link - full templated link") {
    renderElement(
      "basic-links.json",
      CommonExtractors.firstTemplatedLink,
      "basic-links-link-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("link - external reference") {
    renderElement(
      "link/external-reference-link.json",
      CommonExtractors.firstTemplatedLink,
      "link/external-link-emission.yaml",
      Oas30JsonHint
    )
  }

  test("callback - full callback") {
    renderElement(
      "basic-callbacks.json",
      CommonExtractors.firstCallback,
      "basic-callbacks-single-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("callback - external callback reference") {
    renderElement(
      "callback/callback-external-reference.json",
      CommonExtractors.firstCallback,
      "callback/external-reference-emission.yaml",
      Oas30JsonHint
    )
  }

  test("request - request body") {
    renderElement(
      "basic-request-body.json",
      CommonExtractors.firstRequest,
      "basic-request-body-single-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("server") {
    renderElement(
      "basic-servers.json",
      CommonExtractors.webapi.andThen(_.map(_.servers.head)),
      "basic-servers-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("security scheme") {
    renderElement(
      "basic-security-types.json",
      CommonExtractors.declaresIndex(0),
      "basic-security-scheme-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("security scheme external reference") {
    renderElement(
      "security-scheme/reference-external-scheme.json",
      CommonExtractors.declaresIndex(0),
      "security-scheme/external-reference-emission.yaml",
      Oas30JsonHint
    )
  }

  test("payload") {
    renderElement(
      "basic-encoding.json",
      CommonExtractors.firstResponse.andThen(_.map(_.payloads.head)),
      "basic-encoding-emission.yaml",
      Oas30JsonHint,
      directory = upanddownPath
    )
  }

  test("response external reference") {
    renderElement(
      "response/reference-external-response.json",
      CommonExtractors.firstResponse,
      "response/external-response-link-emission.yaml",
      Oas30JsonHint
    )
  }

}
