package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.document.{WebApi => WebApiObject}
import amf.plugins.domain.webapi.models.{bindings => InternalBindings}
import org.scalatest.{FunSuite, Matchers}

class BindingsTest extends FunSuite with Matchers {
  WebApiObject.register()

  val s                                    = "test string"
  val stringSeq                            = Seq(s)
  val clientStringList: ClientList[String] = stringSeq.asClient
  val shape                                = new AnyShape()

  test("test Amqp091ChannelExchange") {
    val exchange = new Amqp091ChannelExchange()
      .withType(s)
      .withDurable(true)
      .withAutoDelete(false)
      .withVHost(s)
    exchange.`type`.value() shouldBe s
    exchange.durable.value() shouldBe true
    exchange.autoDelete.value() shouldBe false
    exchange.vHost.value() shouldBe s
  }

  test("test Amqp091Queue") {
    val queue = new Amqp091Queue()
      .withExclusive(false)
      .withDurable(true)
      .withAutoDelete(false)
      .withVHost(s)
    queue.exclusive.value() shouldBe false
    queue.durable.value() shouldBe true
    queue.autoDelete.value() shouldBe false
    queue.vHost.value() shouldBe s
  }

  test("test Amqp091ChannelBinding") {
    val exchange = new Amqp091ChannelExchange()
    val queue    = new Amqp091Queue()
    val amqpChannelBinding = new Amqp091ChannelBinding()
      .withIs(s)
      .withExchange(exchange)
      .withQueue(queue)
      .withBindingVersion(s)
      .withId(s)
    amqpChannelBinding.is.value() shouldBe s
    amqpChannelBinding.id shouldBe s
    amqpChannelBinding.exchange shouldBe exchange
    amqpChannelBinding.queue shouldBe queue
  }

  test("test Amqp091MessageBinding") {
    val messageBinding = new Amqp091MessageBinding()
      .withContentEncoding(s)
      .withMessageType(s)
    messageBinding.contentEncoding.value() shouldBe s
    messageBinding.messageType.value() shouldBe s
  }

  test("test Amqp091OperationBinding") {
    val operationBinding = new Amqp091OperationBinding()
      .withExpiration(1)
      .withUserId(s)
      .withCc(stringSeq)
      .withPriority(1)
      .withDeliveryMode(1)
      .withMandatory(true)
      .withBcc(stringSeq)
      .withReplyTo(s)
      .withTimestamp(true)
      .withAck(true)
    operationBinding.expiration.value() shouldBe 1
    operationBinding.userId.value() shouldBe s
    operationBinding.cc.toString shouldBe clientStringList.toString
    operationBinding.bcc.toString shouldBe clientStringList.toString
    operationBinding.priority.value() shouldBe 1
    operationBinding.deliveryMode.value() shouldBe 1
    operationBinding.mandatory.value() shouldBe true
    operationBinding.replyTo.value() shouldBe s
    operationBinding.timestamp.value() shouldBe true
    operationBinding.ack.value() shouldBe true
  }

  test("test ChannelBindings") {
    val internalChannelBindings: Seq[InternalBindings.ChannelBinding] = Seq(new Amqp091ChannelBinding()._internal)
    val clientChannelBindings                                         = internalChannelBindings.asClient

    val channelBindings = new ChannelBindings()
      .withName(s)
      .withBindings(clientChannelBindings)
    channelBindings.name.value() shouldBe s
    channelBindings.bindings.asInternal shouldBe clientChannelBindings.asInternal
  }

  test("test DynamicBinding") {
    val node = new ScalarNode()
    val binding = new DynamicBinding()
      .withType(s)
      .withDefinition(node)
    binding.`type`.value() shouldBe s
    binding.definition shouldBe node
  }

  test("test EmptyBinding") {
    val binding = new EmptyBinding()
      .withType(s)
    binding.`type`.value() shouldBe s
  }

  test("test HttpMessageBinding") {
    val binding = new HttpMessageBinding()
      .withBindingVersion(s)
      .withHeaders(shape)
    binding.headers._internal shouldBe shape._internal
  }

  test("test HttpOperationBinding") {
    val binding = new HttpOperationBinding()
      .withBindingVersion(s)
      .withMethod(s)
      .withQuery(shape)
      .withOperationType(s)
    binding.method.value() shouldBe s
    binding.operationType.value() shouldBe s
    binding.query._internal shouldBe shape._internal
  }

  test("test KafkaMessageBinding") {
    val binding = new KafkaMessageBinding()
      .withBindingVersion(s)
      .withKey(s)
    binding.messageKey.value() shouldBe s
  }

  test("test KafkaOperationBinding") {
    val binding = new KafkaOperationBinding()
      .withBindingVersion(s)
      .withClientId(s)
      .withGroupId(s)
    binding.clientId.value() shouldBe s
    binding.groupId.value() shouldBe s
  }

  test("test MessageBindings") {
    val bindings: Seq[InternalBindings.MessageBinding] = Seq(new Amqp091MessageBinding()._internal)
    val clientBindings                                 = bindings.asClient
    val binding = new MessageBindings()
      .withName(s)
      .withBindings(clientBindings)
    binding.name.value() shouldBe s
    binding.bindings.asInternal shouldBe clientBindings.asInternal
  }

  test("test MqttMessageBinding") {
    val binding = new MqttMessageBinding()
      .withBindingVersion(s)
    binding._internal.componentId shouldBe "/mqtt-message"
  }

  test("test MqttOperationBinding") {
    val mqttOperationBinding = new MqttOperationBinding()
      .withQos(2)
      .withRetain(true)
    mqttOperationBinding.qos.value() shouldBe 2
    mqttOperationBinding.retain.value() shouldBe true
  }

  test("test MqttServerBinding") {
    val mqttServerLastWill = new MqttServerLastWill()
    val mqttServerBinding = new MqttServerBinding()
      .withClientId(s)
      .withCleanSession(true)
      .withLastWill(mqttServerLastWill)
      .withKeepAlive(2)
    mqttServerBinding.clientId.value() shouldBe s
    mqttServerBinding.cleanSession.value() shouldBe true
    mqttServerBinding.lastWill shouldBe mqttServerLastWill
    mqttServerBinding.keepAlive.value() shouldBe 2
  }

  test("test OperationBindings") {
    val OperationBindings: Seq[InternalBindings.OperationBinding] = Seq(new MqttOperationBinding()._internal)
    val clientOperationBindings                                   = OperationBindings.asClient
    val operationBindings = new OperationBindings()
      .withName(s)
      .withBindings(clientOperationBindings)
    operationBindings.name.value() shouldBe s
    operationBindings.bindings.asInternal shouldBe clientOperationBindings.asInternal
  }

  test("test ServerBindings") {
    val internalServerBindings: Seq[InternalBindings.ServerBinding] = Seq(new MqttServerBinding()._internal)
    val clientServerBindings: ClientList[ServerBinding]             = internalServerBindings.asClient
    val serverBindings = new ServerBindings()
      .withName(s)
      .withBindings(clientServerBindings)
    serverBindings.name.value() shouldBe s
    serverBindings.bindings.asInternal shouldBe clientServerBindings.asInternal
  }

  test("test WebSocketsChannelBinding") {
    val binding = new WebSocketsChannelBinding()
      .withBindingVersion(s)
      .withHeaders(shape)
      .withMethod(s)
      .withQuery(shape)
      .withType(s)
    binding.headers._internal shouldBe shape._internal
    binding.method.value() shouldBe s
    binding.query._internal shouldBe shape._internal
    binding.`type`.value() shouldBe s
  }
}
