package amf.validation

import amf.Async20Profile
import amf.core.remote.{AsyncYamlHint, Hint}

class ValidAsyncModelParserTest extends ValidModelTest {

  test("Full message object") {
    checkValid("message-obj.yaml", Async20Profile)
  }

  test("Draft 7 schemas") {
    checkValid("draft-7-schemas.yaml", Async20Profile)
  }

  test("Channel parameters") {
    checkValid("channel-parameters.yaml", Async20Profile)
  }

  test("Empty and dynamic binding") {
    checkValid("empty-dynamic-binding.yaml", Async20Profile)
  }

  test("Amqp 0.9.1 exchange channel binding") {
    checkValid("amqp-exchange-channel-binding.yaml", Async20Profile)
  }

  test("Amqp 0.9.1 queue channel binding") {
    checkValid("amqp-queue-channel-binding.yaml", Async20Profile)
  }

  test("Amqp 0.9.1 message binding") {
    checkValid("amqp-message-binding.yaml", Async20Profile)
  }

  test("Amqp 0.9.1 operation binding") {
    checkValid("amqp-operation-binding.yaml", Async20Profile)
  }

  test("Http message binding") {
    checkValid("http-message-binding.yaml", Async20Profile)
  }

  test("Http operation binding") {
    checkValid("http-operation-binding.yaml", Async20Profile)
  }

  test("Kafka message binding") {
    checkValid("kafka-message-binding.yaml", Async20Profile)
  }

  test("Kafka operation binding") {
    checkValid("kafka-operation-binding.yaml", Async20Profile)
  }

  test("Mqtt message binding") {
    checkValid("mqtt-message-binding.yaml", Async20Profile)
  }

  test("Mqtt operation binding") {
    checkValid("mqtt-operation-binding.yaml", Async20Profile)
  }

  test("Mqtt server binding") {
    checkValid("mqtt-server-binding.yaml", Async20Profile)
  }

  test("Websockets channel binding") {
    checkValid("ws-channel-binding.yaml", Async20Profile)
  }

  test("Rpc server example") {
    checkValid("rpc-server.yaml", Async20Profile)
  }

  override val basePath: String = "file://amf-client/shared/src/test/resources/validations/async20/"
  override val hint: Hint       = AsyncYamlHint
}
