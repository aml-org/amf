package amf.emit

import amf.core.remote.{Amf, AsyncYamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.WebApi

class Async20CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/validations/async20/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cyclesAsyncAmf: Seq[FixtureData] = Seq(
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
    FixtureData("Websockets channel binding", "ws-channel-binding.yaml", "ws-channel-binding.jsonld")
  )
  cyclesAsyncAmf.foreach { f =>
    test(s"${f.name} - async to amf") {
      cycle(f.apiFrom, f.apiTo, AsyncYamlHint, Amf)
    }
  }
}
