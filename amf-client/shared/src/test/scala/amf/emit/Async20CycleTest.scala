package amf.emit

import amf.core.remote.Syntax.Yaml
import amf.core.remote.{Amf, AsyncApi20, AsyncYamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.WebApi

class Async20CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/validations/async20/"
  val upanddown: String         = "amf-client/shared/src/test/resources/upanddown/cycle/async20/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  cyclesAsyncAmf.foreach { f =>
    test(s"${f.name} - async to amf") {
      cycle(f.apiFrom, f.apiTo, AsyncYamlHint, Amf)
    }
  }

  cyclesAsyncAsync.foreach { f =>
    test(s"${f.name} - async to async") {
      cycle(f.apiFrom, f.apiTo, AsyncYamlHint, AsyncApi20, directory = upanddown, syntax = Some(Yaml))
    }
  }

  def cyclesAsyncAmf: Seq[FixtureData] = Seq(
    FixtureData("Simple publish and subscribe", "publish-subscribe.yaml", "publish-subscribe.jsonld"),
    FixtureData("Message object", "message-obj.yaml", "message-obj.jsonld"),
    FixtureData("Draft 7 schemas", "draft-7-schemas.yaml", "draft-7-schemas.jsonld"),
    FixtureData("Parameters object", "channel-parameters.yaml", "channel-parameters.jsonld"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.jsonld"),
    FixtureData("Empty and dynamic binding", "empty-dynamic-binding.yaml", "empty-dynamic-binding.jsonld"),
    FixtureData("Amqp 0.9.1 exchange channel binding",
                "amqp-exchange-channel-binding.yaml",
                "amqp-exchange-channel-binding.jsonld"),
    FixtureData("Amqp 0.9.1 queue channel binding",
                "amqp-queue-channel-binding.yaml",
                "amqp-queue-channel-binding.jsonld"),
    FixtureData("Amqp 0.9.1 message binding", "amqp-message-binding.yaml", "amqp-message-binding.jsonld"),
    FixtureData("Amqp 0.9.1 operation binding", "amqp-operation-binding.yaml", "amqp-operation-binding.jsonld"),
    FixtureData("Http message binding", "http-message-binding.yaml", "http-message-binding.jsonld"),
    FixtureData("Http operation binding", "http-operation-binding.yaml", "http-operation-binding.jsonld"),
    FixtureData("Kafka message binding", "kafka-message-binding.yaml", "kafka-message-binding.jsonld"),
    FixtureData("Kafka operation binding", "kafka-operation-binding.yaml", "kafka-operation-binding.jsonld"),
    FixtureData("Mqtt message binding", "mqtt-message-binding.yaml", "mqtt-message-binding.jsonld"),
    FixtureData("Mqtt operation binding", "mqtt-operation-binding.yaml", "mqtt-operation-binding.jsonld"),
    FixtureData("Mqtt server binding", "mqtt-server-binding.yaml", "mqtt-server-binding.jsonld"),
    FixtureData("Websockets channel binding", "ws-channel-binding.yaml", "ws-channel-binding.jsonld"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.jsonld"),
    FixtureData("Components declarations and references",
                "components/async-components.yaml",
                "components/async-components.jsonld")
  )

  def cyclesAsyncAsync: Seq[FixtureData] = Seq(
    FixtureData("Empty cycle", "empty.yaml", "empty.yaml"),
    FixtureData("Info cycle", "info.yaml", "info.yaml"),
    FixtureData("Tags cycle", "tags.yaml", "tags.yaml"),
    FixtureData("Documentation cycle", "documentation.yaml", "documentation.yaml"),
    FixtureData("Single server cycle", "server.yaml", "server.yaml"),
    FixtureData("Multiple server cycle", "servers.yaml", "servers.yaml"),
    FixtureData("Server variable cycle", "server-variables.yaml", "server-variables.yaml"),
    FixtureData("Server security cycle", "server-security.yaml", "server-security.yaml"),
    FixtureData("Single channel cycle", "single-channel.yaml", "single-channel.yaml"),
    FixtureData("Multiple messages cycle", "multiple-messages.yaml", "multiple-messages.yaml"),
    FixtureData("Channel params cycle", "channel-params.yaml", "channel-params.yaml"),
    FixtureData("Message examples cycle", "message-examples.yaml", "message-examples.yaml"),
    FixtureData("Message headers cycle", "message-headers.yaml", "message-headers.yaml"),
    FixtureData("Simple publish and subscribe", "publish-subscribe.yaml", "publish-subscribe.yaml"),
    FixtureData("Empty and dynamic binding", "bindings/empty-dynamic-binding.yaml", "bindings/empty-dynamic-binding.yaml"),
    FixtureData("Amqp 0.9.1 exchange channel binding",
      "bindings/amqp-exchange-channel-binding.yaml",
      "bindings/amqp-exchange-channel-binding.yaml"),
    FixtureData("Amqp 0.9.1 queue channel binding",
      "bindings/amqp-queue-channel-binding.yaml",
      "bindings/amqp-queue-channel-binding.yaml"),
    FixtureData("Amqp 0.9.1 message binding", "bindings/amqp-message-binding.yaml", "bindings/amqp-message-binding.yaml"),
    FixtureData("Amqp 0.9.1 operation binding", "bindings/amqp-operation-binding.yaml", "bindings/amqp-operation-binding.yaml"),
    FixtureData("Http message binding", "bindings/http-message-binding.yaml", "bindings/http-message-binding.yaml"),
    FixtureData("Http operation binding", "bindings/http-operation-binding.yaml", "bindings/http-operation-binding.yaml"),
    FixtureData("Kafka message binding", "bindings/kafka-message-binding.yaml", "bindings/kafka-message-binding.yaml"),
    FixtureData("Kafka operation binding", "bindings/kafka-operation-binding.yaml", "bindings/kafka-operation-binding.yaml"),
    FixtureData("Mqtt message binding", "bindings/mqtt-message-binding.yaml", "bindings/mqtt-message-binding.yaml"),
    FixtureData("Mqtt operation binding", "bindings/mqtt-operation-binding.yaml", "bindings/mqtt-operation-binding.yaml"),
    FixtureData("Mqtt server binding", "bindings/mqtt-server-binding.yaml", "bindings/mqtt-server-binding.yaml"),
    FixtureData("Websockets channel binding", "bindings/ws-channel-binding.yaml", "bindings/ws-channel-binding.yaml"),
    FixtureData("RAML type - simple union", "raml-types/simple-union.yaml", "raml-types/simple-union.yaml"),
    FixtureData("RAML type - simple object", "raml-types/simple-object.yaml", "raml-types/simple-object.yaml"),
    FixtureData("RAML type - simple scalar", "raml-types/simple-scalar.yaml", "raml-types/simple-scalar.yaml"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.yaml"),
//    FixtureData("Channel params with refs", "references/channel-params.yaml", "references/channel-params.yaml"),
  )
}
