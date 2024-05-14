package amf.emit

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint}
import amf.io.FunSuiteCycleTests

class Async20CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-cli/shared/src/test/resources/validations/async20/"
  val upanddown: String         = "amf-cli/shared/src/test/resources/upanddown/cycle/async20/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  cyclesAsyncAmf.foreach { f =>
    multiGoldenTest(s"${f.name} - async to amf", f.apiTo) { config =>
      cycle(f.apiFrom, config.golden, Async20YamlHint, target = AmfJsonHint, renderOptions = Some(config.renderOptions))
    }
  }

  cyclesAsyncAsync.foreach { f =>
    test(s"${f.name} - async to async") {
      cycle(f.apiFrom, f.apiTo, Async20YamlHint, target = Async20YamlHint, directory = upanddown)
    }
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  def cyclesAsyncAmf: Seq[FixtureData] = Seq(
    FixtureData("Simple publish and subscribe", "publish-subscribe.yaml", "publish-subscribe.%s"),
    FixtureData("Message object", "message-obj.yaml", "message-obj.%s"),
    FixtureData("Draft 7 schemas", "draft-7-schemas.yaml", "draft-7-schemas.%s"),
    FixtureData("Parameters object", "channel-parameters.yaml", "channel-parameters.%s"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.%s"),
    FixtureData(
      "Empty binding and annotations",
      "empty-binding-and-annotations.yaml",
      "empty-binding-and-annotations.%s"
    ),
    FixtureData(
      "Amqp 0.9.1 channel binding version 0.1.0",
      "amqp-channel-binding-010.yaml",
      "amqp-channel-binding-010.%s"
    ),
    FixtureData(
      "Amqp 0.9.1 channel binding version 0.2.0",
      "amqp-channel-binding-020.yaml",
      "amqp-channel-binding-020.%s"
    ),
    FixtureData("Amqp 0.9.1 message binding", "amqp-message-binding.yaml", "amqp-message-binding.%s"),
    FixtureData("Amqp 0.9.1 operation binding", "amqp-operation-binding.yaml", "amqp-operation-binding.%s"),
    FixtureData("Http message binding", "http-message-binding.yaml", "http-message-binding.%s"),
    FixtureData("Http operation binding", "http-operation-binding.yaml", "http-operation-binding.%s"),
    FixtureData("Kafka message binding", "kafka-message-binding-010.yaml", "kafka-message-binding.%s"),
    FixtureData("Kafka operation binding", "kafka-operation-binding.yaml", "kafka-operation-binding.%s"),
    FixtureData(
      "Mqtt message binding",
      "mqtt-message-binding.yaml",
      "mqtt-message-binding.%s"
    ),
    FixtureData(
      "Mqtt operation binding",
      "mqtt-operation-binding.yaml",
      "mqtt-operation-binding.%s"
    ),
    FixtureData("Mqtt server binding", "mqtt-server-binding.yaml", "mqtt-server-binding.%s"),
    FixtureData("Websockets channel binding", "ws-channel-binding.yaml", "ws-channel-binding.%s"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.%s"),
    FixtureData(
      "Components declarations and references",
      "components/async-components.yaml",
      "components/async-components.%s"
    ),
    FixtureData("Operation traits", "components/operation-traits.yaml", "components/operation-traits.%s"),
    FixtureData(
      "Operation with inline external traits",
      "components/external-operation-traits.yaml",
      "components/external-operation-traits.%s"
    ),
    FixtureData("Message traits", "components/message-traits.yaml", "components/message-traits.%s"),
    FixtureData("Draft-7 external reference", "draft-7/references.yaml", "draft-7/references.%s"),
    FixtureData("Async 2.1 all", "asyncApi-2.1-all.yaml", "asyncApi-2.1-all.%s"),
    FixtureData("Async 2.2 all", "asyncApi-2.2-all.yaml", "asyncApi-2.2-all.%s"),
    FixtureData("Async 2.3 all", "asyncApi-2.3-all.yaml", "asyncApi-2.3-all.%s"),
    FixtureData("Async 2.4 all", "asyncApi-2.4-all.yaml", "asyncApi-2.4-all.%s"),
    FixtureData("Async 2.5 all", "asyncApi-2.5-all.yaml", "asyncApi-2.5-all.%s"),
    FixtureData("Async 2.6 all", "asyncApi-2.6-all.yaml", "asyncApi-2.6-all.%s")
  )

  def cyclesAsyncAsync: Seq[FixtureData] = Seq(
    FixtureData("Empty cycle", "empty.yaml", "empty.yaml"),
    FixtureData("Default Content Type", "default-content-type.yaml", "default-content-type.yaml"),
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
    FixtureData(
      "Empty and dynamic binding",
      "bindings/empty-binding-and-annotations.yaml",
      "bindings/empty-binding-and-annotations.yaml"
    ),
    FixtureData(
      "Amqp 0.9.1 channel binding version 0.1.0",
      "bindings/amqp-channel-binding-010.yaml",
      "bindings/amqp-channel-binding-010.yaml"
    ),
    FixtureData(
      "Amqp 0.9.1 channel binding version 0.2.0",
      "bindings/amqp-channel-binding-020.yaml",
      "bindings/amqp-channel-binding-020.yaml"
    ),
    FixtureData(
      "Amqp 0.9.1 message binding",
      "bindings/amqp-message-binding.yaml",
      "bindings/amqp-message-binding.yaml"
    ),
    FixtureData(
      "Amqp 0.9.1 operation binding",
      "bindings/amqp-operation-binding.yaml",
      "bindings/amqp-operation-binding.yaml"
    ),
    FixtureData(
      "Http message binding",
      "bindings/http-message-binding.yaml",
      "bindings/http-message-binding.yaml"
    ),
    FixtureData(
      "Http operation binding",
      "bindings/http-operation-binding.yaml",
      "bindings/http-operation-binding.yaml"
    ),
    FixtureData(
      "Kafka message binding",
      "bindings/kafka-message-binding.yaml",
      "bindings/kafka-message-binding.yaml.yaml"
    ),
    FixtureData(
      "Kafka operation binding",
      "bindings/kafka-operation-binding.yaml",
      "bindings/kafka-operation-binding.yaml.yaml"
    ),
    FixtureData("Mqtt message binding", "bindings/mqtt-message-binding.yaml", "bindings/mqtt-message-binding.yaml"),
    FixtureData(
      "Mqtt operation binding",
      "bindings/mqtt-operation-binding.yaml",
      "bindings/mqtt-operation-binding.yaml.yaml"
    ),
    FixtureData(
      "Mqtt server binding",
      "bindings/mqtt-server-binding.yaml",
      "bindings/mqtt-server-binding.yaml.yaml"
    ),
    FixtureData("Websockets channel binding", "bindings/ws-channel-binding.yaml", "bindings/ws-channel-binding.yaml"),
    FixtureData("RAML type - simple union", "raml-types/simple-union.yaml", "raml-types/simple-union.yaml"),
    FixtureData("RAML type - simple object", "raml-types/simple-object.yaml", "raml-types/simple-object.yaml"),
    FixtureData("RAML type - simple scalar", "raml-types/simple-scalar.yaml", "raml-types/simple-scalar.yaml"),
    FixtureData("Rpc server example", "rpc-server.yaml", "rpc-server.yaml"),
    FixtureData("Draft 7 schemas cycle", "draft-7-schemas-cycle.yaml", "draft-7-schemas-output.yaml"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.yaml"),
    FixtureData("Operation and message traits", "operation-message-traits.yaml", "operation-message-traits.yaml"),
    FixtureData("components emission", "components/components-cycle.yaml", "components/components-cycle.yaml"),
    // TODO: fill async 2.x with each spec new features
    FixtureData("Async 2.1 doc - empty", "empty-async21.yaml", "empty-async21.yaml"),
    FixtureData("Async 2.2 doc - empty", "empty-async22.yaml", "empty-async22.yaml"),
    FixtureData("Async 2.3 doc - empty", "empty-async23.yaml", "empty-async23.yaml"),
    FixtureData("Async 2.4 doc - empty", "empty-async24.yaml", "empty-async24.yaml"),
    FixtureData("Async 2.5 doc - empty", "empty-async25.yaml", "empty-async25.yaml"),
    FixtureData("Async 2.6 doc - empty", "empty-async26.yaml", "empty-async26.yaml"),
    FixtureData(
      "mercure binding",
      "bindings/mercure-binding.yaml",
      "bindings/mercure-binding.yaml"
    ),
    FixtureData("message example", "sumary-name-example-message.yaml", "sumary-name-example-message.yaml"),
    FixtureData(
      "message example in 2.0.0",
      "sumary-name-example-message-2-0.yaml",
      "sumary-name-example-message-2-0-compare.yaml"
    ),
    FixtureData("messageId in 2.4", "messageId.yaml", "messageId.yaml"),
    FixtureData(
      "ibmmq binding",
      "bindings/ibmmq-binding.yaml",
      "bindings/ibmmq-binding.yaml"
    ),
    FixtureData("Async specific channel servers", "channel-servers.yaml", "channel-servers.yaml"),
    FixtureData("Async servers tags", "server-tags.yaml", "server-tags.yaml"),
    FixtureData("Async 2.3 components", "components/components-2.3.yaml", "components/components-2.3.yaml"),
    FixtureData(
      "anypointMQ binding",
      "bindings/anypoint-binding.yaml",
      "bindings/anypoint-binding.yaml"
    ),
    FixtureData("Async serverVariable", "server-variable.yaml", "server-variable.yaml"),
    FixtureData(
      "solace binding",
      "bindings/solace-binding.yaml",
      "bindings/solace-binding.yaml"
    ),
    FixtureData(
      "pulsar binding",
      "bindings/pulsar-binding.yaml",
      "bindings/pulsar-binding.yaml"
    ),
    FixtureData(
      "pulsar binding only required keys",
      "bindings/pulsar-binding-only-required.yaml",
      "bindings/pulsar-binding-only-required.yaml"
    ),
    FixtureData(
      "GooglePubSub message and channel binding",
      "bindings/google-pub-sub-binding.yaml",
      "bindings/google-pub-sub-binding.yaml"
    ),
    FixtureData(
      "async 2.4+ explicit security field in operation bindings",
      "operation-security-explicit.yaml",
      "operation-security-explicit.yaml"
    ),
    FixtureData(
      "async 2.4+ implicit security field in operation bindings",
      "operation-security-implicit.yaml",
      "operation-security-implicit.yaml"
    ),
    FixtureData(
      "Async 2.1 all",
      "asyncApi-2.1-all.yaml",
      "asyncApi-2.1-all.yaml"
    ),
    FixtureData(
      "Async 2.2 all",
      "asyncApi-2.2-all.yaml",
      "asyncApi-2.2-all.yaml"
    ),
    FixtureData(
      "Async 2.3 all",
      "asyncApi-2.3-all.yaml",
      "asyncApi-2.3-all.yaml"
    ),
    FixtureData(
      "Async 2.4 all",
      "asyncApi-2.4-all.yaml",
      "asyncApi-2.4-all.yaml"
    ),
    FixtureData(
      "Async 2.5 all",
      "asyncApi-2.5-all.yaml",
      "asyncApi-2.5-all.yaml"
    ),
    FixtureData(
      "Async 2.6 all",
      "asyncApi-2.6-all.yaml",
      "asyncApi-2.6-all.yaml"
    ),
    FixtureData(
      "Kafka server binding",
      "bindings/kafka-server-binding.yaml",
      "bindings/kafka-server-binding.yaml"
    ),
    FixtureData(
      "Kafka channel binding",
      "bindings/kafka-channel-binding.yaml",
      "bindings/kafka-channel-binding.yaml"
    )

// TODO: figure out why this test is commented out
//    FixtureData("Channel params with refs", "references/channel-params.yaml", "references/channel-params.yaml"),
  )
}
