package amf.cycle

import amf.core.remote.{AsyncYamlHint, Vendor}
import amf.plugins.domain.webapi.models.Server

class Async20ElementCycleTest extends DomainElementCycleTest {

  override val basePath: String = "amf-client/shared/src/test/resources/cycle/async20/"
  val validationsPath: String   = "amf-client/shared/src/test/resources/validations/async20/"
  val vendor: Vendor            = Vendor.ASYNC20

  test("type - composition with refs and inlined") {
    renderElement(
      "type/draft-7-schemas.yaml",
      CommonExtractors.declaresIndex(0),
      "type/schema-emission.yaml",
      AsyncYamlHint
    )
  }

  test("parameter - using referenced schema") {
    renderElement(
      "parameter/parameter-definition.yaml",
      CommonExtractors.declaresIndex(1),
      "parameter/param.yaml",
      AsyncYamlHint
    )
  }

  test("message - declared message") {
    renderElement(
      "components/async-components.yaml",
      CommonExtractors.declaresIndex(16),
      "components/async-components-message-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("message - message defined in publish facet") {
    renderElement(
      "message-obj.yaml",
      CommonExtractors.firstRequest,
      "message-obj-single-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("correlationId - referenced correlationId") {
    renderElement(
      "api.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.correlationId)),
      "correlation-id-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath + "validations/nested-libraries/nested-correlationIds/"
    )
  }

  test("correlationId - inlined definition") {
    renderElement(
      "/correlation-id/api.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.correlationId)),
      "correlation-id/correlation-id-emission.yaml",
      AsyncYamlHint
    )
  }

  test("channel binding - websocket") {
    renderElement(
      "ws-channel-binding.yaml",
      CommonExtractors.firstEndpoint.andThen(_.map(_.bindings)),
      "ws-channel-binding-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("server binding - mqtt") {
    renderElement(
      "mqtt-server-binding.yaml",
      CommonExtractors.firstServer.andThen(_.map(_.bindings)),
      "mqtt-server-binding-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("operation binding - kafka") {
    renderElement(
      "kafka-operation-binding.yaml",
      CommonExtractors.firstOperation.andThen(_.map(_.bindings)),
      "kafka-operation-binding-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("message binding - http") {
    renderElement(
      "http-message-binding.yaml",
      CommonExtractors.firstRequest.andThen(_.map(_.bindings)),
      "http-message-binding-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath
    )
  }

  test("operation trait") {
    renderElement(
      "operation-traits.yaml",
      CommonExtractors.declaresIndex(0),
      "operation-traits-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath + "components/"
    )
  }

  test("message trait") {
    renderElement(
      "message-traits.yaml",
      CommonExtractors.declaresIndex(0),
      "message-traits-emission.yaml",
      AsyncYamlHint,
      directory = validationsPath + "components/"
    )
  }

}
