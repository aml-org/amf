package amf.cycle

import amf.core.internal.remote.{Async20YamlHint, Vendor}

class Async20ElementCycleTest extends DomainElementCycleTest {

  override val basePath: String = "amf-cli/shared/src/test/resources/cycle/async20/"
  val validationsPath: String   = "amf-cli/shared/src/test/resources/validations/async20/"
  val upanddownPath: String     = "amf-cli/shared/src/test/resources/upanddown/cycle/async20/"
  val vendor: Vendor            = Vendor.ASYNC20

  test("type - composition with refs and inlined") {
    renderElement(
      "type/draft-7-schemas.yaml",
      CommonExtractors.declaresIndex(0),
      "type/schema-emission.yaml",
      Async20YamlHint
    )
  }

  test("parameter - using referenced schema") {
    renderElement(
      "parameter/parameter-definition.yaml",
      CommonExtractors.declaresIndex(1),
      "parameter/param.yaml",
      Async20YamlHint
    )
  }

  test("message - declared message") {
    renderElement(
      "components/async-components.yaml",
      CommonExtractors.declaresIndex(16),
      "components/async-components-message-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("message - message defined in publish facet") {
    renderElement(
      "message-obj.yaml",
      CommonExtractors.firstRequest,
      "message-obj-single-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("correlationId - referenced correlationId") {
    renderElement(
      "api.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.correlationId)),
      "correlation-id-emission.yaml",
      Async20YamlHint,
      directory = validationsPath + "validations/nested-libraries/nested-correlationIds/"
    )
  }

  test("correlationId - inlined definition") {
    renderElement(
      "/correlation-id/api.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.correlationId)),
      "correlation-id/correlation-id-emission.yaml",
      Async20YamlHint
    )
  }

  test("channel binding - websocket") {
    renderElement(
      "ws-channel-binding.yaml",
      CommonExtractors.firstEndpoint.andThen(_.map(_.bindings)),
      "ws-channel-binding-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("server binding - mqtt") {
    renderElement(
      "mqtt-server-binding.yaml",
      CommonExtractors.firstServer.andThen(_.map(_.bindings)),
      "mqtt-server-binding-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("operation binding - kafka") {
    renderElement(
      "kafka-operation-binding.yaml",
      CommonExtractors.firstOperation.andThen(_.map(_.bindings)),
      "kafka-operation-binding-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("message binding - http") {
    renderElement(
      "http-message-binding.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.bindings)),
      "http-message-binding-emission.yaml",
      Async20YamlHint,
      directory = validationsPath
    )
  }

  test("operation trait") {
    renderElement(
      "operation-traits.yaml",
      CommonExtractors.declaresIndex(0),
      "operation-traits-emission.yaml",
      Async20YamlHint,
      directory = validationsPath + "components/"
    )
  }

  test("message trait") {
    renderElement(
      "message-traits.yaml",
      CommonExtractors.declaresIndex(0),
      "message-traits-emission.yaml",
      Async20YamlHint,
      directory = validationsPath + "components/"
    )
  }

  test("server") {
    renderElement(
      "server.yaml",
      CommonExtractors.webapi.andThen(_.map(_.servers.head)),
      "server-emission.yaml",
      Async20YamlHint,
      directory = upanddownPath
    )
  }

  test("security scheme") {
    renderElement(
      "security-schemes.yaml",
      CommonExtractors.declaresIndex(8),
      "scheme-emission.yaml",
      Async20YamlHint,
      directory = upanddownPath
    )
  }

  test("example") {
    renderElement(
      "type/draft-7-schemas.yaml",
      CommonExtractors.declaresIndex(0)(_).map(_.asInstanceOf[AnyShape].examples.head),
      "type/example-emission.yaml",
      Async20YamlHint
    )
  }

  test("endpoint") {
    renderElement(
      "publish-subscribe.yaml",
      CommonExtractors.firstEndpoint,
      "publish-subscribe-emission.yaml",
      Async20YamlHint,
      directory = upanddownPath
    )
  }

}
