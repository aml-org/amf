package amf.emit

import amf.core.emitter.RenderOptions
import amf.core.remote.Syntax.Yaml
import amf.core.remote.{Amf, AsyncApi20, AsyncYamlHint}
import amf.io.FunSuiteCycleTests

class Async20CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/validations/async20/"
  val upanddown: String         = "amf-client/shared/src/test/resources/upanddown/cycle/async20/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  cyclesAsyncAmf.foreach { f =>
    multiGoldenTest(s"${f.name} - async to amf", f.apiTo) { config =>
      cycle(f.apiFrom, config.golden, AsyncYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
    }
  }

  cyclesAsyncAsync.foreach { f =>
    test(s"${f.name} - async to async") {
      cycle(f.apiFrom, f.apiTo, AsyncYamlHint, target = AsyncApi20, directory = upanddown, syntax = Some(Yaml))
    }
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  def cyclesAsyncAmf: Seq[FixtureData] = Seq(
    FixtureData("Simple publish and subscribe", "publish-subscribe.yaml", "publish-subscribe.%s"),
    FixtureData("Message object", "message-obj.yaml", "message-obj.%s"),
    FixtureData("Draft 7 schemas", "draft-7-schemas.yaml", "draft-7-schemas.%s"),
    FixtureData("Parameters object", "channel-parameters.yaml", "channel-parameters.%s"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.%s"),
    FixtureData("Empty binding and annotations",
                "empty-binding-and-annotations.yaml",
                "empty-binding-and-annotations.%s"),
    FixtureData("Amqp 0.9.1 channel binding", "amqp-channel-binding.yaml", "amqp-channel-binding.%s"),
    FixtureData("Amqp 0.9.1 message binding", "amqp-message-binding.yaml", "amqp-message-binding.%s"),
    FixtureData("Amqp 0.9.1 operation binding", "amqp-operation-binding.yaml", "amqp-operation-binding.%s"),
    FixtureData("Http message binding", "http-message-binding.yaml", "http-message-binding.%s"),
    FixtureData("Http operation binding", "http-operation-binding.yaml", "http-operation-binding.%s"),
    FixtureData("Kafka message binding", "kafka-message-binding.yaml", "kafka-message-binding.%s"),
    FixtureData("Kafka operation binding", "kafka-operation-binding.yaml", "kafka-operation-binding.%s"),
    FixtureData("Mqtt message binding", "mqtt-message-binding.yaml", "mqtt-message-binding.%s"),
    FixtureData("Mqtt operation binding", "mqtt-operation-binding.yaml", "mqtt-operation-binding.%s"),
    FixtureData("Mqtt server binding", "mqtt-server-binding.yaml", "mqtt-server-binding.%s"),
    FixtureData("Websockets channel binding", "ws-channel-binding.yaml", "ws-channel-binding.%s"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.%s"),
    FixtureData("Components declarations and references",
                "components/async-components.yaml",
                "components/async-components.%s"),
    FixtureData("Operation traits", "components/operation-traits.yaml", "components/operation-traits.%s"),
    FixtureData("Operation with inline external traits",
                "components/external-operation-traits.yaml",
                "components/external-operation-traits.%s"),
    FixtureData("Message traits", "components/message-traits.yaml", "components/message-traits.%s")
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
    FixtureData("Empty and dynamic binding",
                "bindings/empty-binding-and-annotations.yaml",
                "bindings/empty-binding-and-annotations.yaml"),
    FixtureData("Amqp 0.9.1 channel binding",
                "bindings/amqp-channel-binding.yaml",
                "bindings/amqp-channel-binding.yaml"),
    FixtureData("Amqp 0.9.1 message binding",
                "bindings/amqp-message-binding.yaml",
                "bindings/amqp-message-binding.yaml"),
    FixtureData("Amqp 0.9.1 operation binding",
                "bindings/amqp-operation-binding.yaml",
                "bindings/amqp-operation-binding.yaml"),
    FixtureData("Http message binding", "bindings/http-message-binding.yaml", "bindings/http-message-binding.yaml"),
    FixtureData("Http operation binding",
                "bindings/http-operation-binding.yaml",
                "bindings/http-operation-binding.yaml"),
    FixtureData("Kafka message binding", "bindings/kafka-message-binding.yaml", "bindings/kafka-message-binding.yaml"),
    FixtureData("Kafka operation binding",
                "bindings/kafka-operation-binding.yaml",
                "bindings/kafka-operation-binding.yaml"),
    FixtureData("Mqtt message binding", "bindings/mqtt-message-binding.yaml", "bindings/mqtt-message-binding.yaml"),
    FixtureData("Mqtt operation binding",
                "bindings/mqtt-operation-binding.yaml",
                "bindings/mqtt-operation-binding.yaml"),
    FixtureData("Mqtt server binding", "bindings/mqtt-server-binding.yaml", "bindings/mqtt-server-binding.yaml"),
    FixtureData("Websockets channel binding", "bindings/ws-channel-binding.yaml", "bindings/ws-channel-binding.yaml"),
    FixtureData("RAML type - simple union", "raml-types/simple-union.yaml", "raml-types/simple-union.yaml"),
    FixtureData("RAML type - simple object", "raml-types/simple-object.yaml", "raml-types/simple-object.yaml"),
    FixtureData("RAML type - simple scalar", "raml-types/simple-scalar.yaml", "raml-types/simple-scalar.yaml"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.yaml"),
    FixtureData("Draft 7 schemas cycle", "draft-7-schemas-cycle.yaml", "draft-7-schemas-output.yaml"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.yaml"),
    FixtureData("Operation and message traits", "operation-message-traits.yaml", "operation-message-traits.yaml"),
    FixtureData("components emission", "components-cycle.yaml", "components-cycle.yaml")
//    FixtureData("Channel params with refs", "references/channel-params.yaml", "references/channel-params.yaml"),
  )
}
