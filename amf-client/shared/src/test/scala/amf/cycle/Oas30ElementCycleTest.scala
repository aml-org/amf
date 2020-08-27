package amf.cycle

import amf.core.remote.{OasJsonHint, Vendor}

class Oas30ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas30/"
  val upanddownPath: String     = "amf-client/shared/src/test/resources/upanddown/oas3/"
  val vendor: Vendor            = Vendor.OAS30

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/login-response-emission.yaml",
      OasJsonHint
    )
  }

  test("parameter - cookie parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(3),
      "parameter/cookie-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - explicit header") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(4),
      "parameter/explicit-header.yaml",
      OasJsonHint
    )
  }

  test("parameter - response header") {
    renderElement(
      "basic-headers-response.json",
      CommonExtractors.firstResponse.andThen(_.map(_.headers.head)),
      "basic-headers-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("parameter - header parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(0),
      "parameter/header-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - path parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/path-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - query parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(1),
      "parameter/query-param.yaml",
      OasJsonHint
    )
  }

  test("response - response with headers") {
    renderElement(
      "basic-headers-response.json",
      CommonExtractors.firstResponse,
      "reponse-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("example - full example") {
    renderElement(
      "basic-content.json",
      CommonExtractors.firstExample,
      "basic-content-example-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("link - full templated link") {
    renderElement(
      "basic-links.json",
      CommonExtractors.firstTemplatedLink,
      "basic-links-link-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("callback - full callback") {
    renderElement(
      "basic-callbacks.json",
      CommonExtractors.firstCallback,
      "basic-callbacks-single-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("request - request body") {
    renderElement(
      "basic-request-body.json",
      CommonExtractors.firstRequest,
      "basic-request-body-single-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("server") {
    renderElement(
      "basic-servers.json",
      CommonExtractors.webapi.andThen(_.map(_.servers.head)),
      "basic-servers-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("security scheme") {
    renderElement(
      "basic-security-types.json",
      CommonExtractors.declaresIndex(0),
      "basic-security-scheme-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

  test("payload") {
    renderElement(
      "basic-encoding.json",
      CommonExtractors.firstResponse.andThen(_.map(_.payloads.head)),
      "basic-encoding-emission.yaml",
      OasJsonHint,
      directory = upanddownPath
    )
  }

}
