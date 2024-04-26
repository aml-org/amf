package amf.client.model.domain

import amf.apicontract.client.platform.model.domain.bindings._
import amf.apicontract.client.platform.model.domain.bindings.amqp._
import amf.apicontract.client.platform.model.domain.bindings.http._
import amf.apicontract.client.platform.model.domain.bindings.kafka._
import amf.apicontract.client.platform.model.domain.bindings.mqtt._
import amf.apicontract.client.platform.model.domain.bindings.websockets._
import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.shapes.client.platform.model.domain.AnyShape
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BindingsTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  val s                                    = "test string"
  val stringSeq                            = Seq(s)
  val clientStringList: ClientList[String] = stringSeq.asClient
  val shape                                = new AnyShape()

  override protected def beforeAll(): Unit = {
    APIConfiguration.API() // TODO: ARM remove after wrappers are deleted
  }

  test("test Amqp091ChannelExchange010") {
    val exchange = new Amqp091ChannelExchange010()
      .withType(s)
      .withDurable(true)
      .withAutoDelete(false)
    exchange.`type`.value() shouldBe s
    exchange.durable.value() shouldBe true
    exchange.autoDelete.value() shouldBe false
  }

  test("test Amqp091ChannelExchange020") {
    val exchange = new Amqp091ChannelExchange020()
      .withType(s)
      .withDurable(true)
      .withAutoDelete(false)
      .withVHost(s)
    exchange.`type`.value() shouldBe s
    exchange.durable.value() shouldBe true
    exchange.autoDelete.value() shouldBe false
    exchange.vHost.value() shouldBe s
  }

  test("test Amqp091Queue010") {
    val queue = new Amqp091Queue010()
      .withExclusive(false)
      .withDurable(true)
      .withAutoDelete(false)
    queue.exclusive.value() shouldBe false
    queue.durable.value() shouldBe true
    queue.autoDelete.value() shouldBe false
  }

  test("test Amqp091Queue020") {
    val queue = new Amqp091Queue020()
      .withExclusive(false)
      .withDurable(true)
      .withAutoDelete(false)
      .withVHost(s)
    queue.exclusive.value() shouldBe false
    queue.durable.value() shouldBe true
    queue.autoDelete.value() shouldBe false
    queue.vHost.value() shouldBe s
  }

  test("test Amqp091ChannelBinding010") {
    val exchange = new Amqp091ChannelExchange010()
    val queue    = new Amqp091Queue010()
    val amqpChannelBinding = new Amqp091ChannelBinding010()
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

  test("test Amqp091ChannelBinding020") {
    val exchange = new Amqp091ChannelExchange020()
    val queue    = new Amqp091Queue020()
    val amqpChannelBinding = new Amqp091ChannelBinding020()
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

  test("test Amqp091OperationBinding010") {
    val operationBinding = new Amqp091OperationBinding010()
      .withExpiration(1)
      .withUserId(s)
      .withCc(stringSeq.asClient)
      .withPriority(1)
      .withDeliveryMode(1)
      .withMandatory(true)
      .withBcc(stringSeq.asClient)
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

  test("test Amqp091OperationBinding030") {
    val operationBinding = new Amqp091OperationBinding030()
      .withExpiration(1)
      .withUserId(s)
      .withCc(stringSeq.asClient)
      .withPriority(1)
      .withDeliveryMode(1)
      .withMandatory(true)
      .withBcc(stringSeq.asClient)
      .withTimestamp(true)
      .withAck(true)
    operationBinding.expiration.value() shouldBe 1
    operationBinding.userId.value() shouldBe s
    operationBinding.cc.toString shouldBe clientStringList.toString
    operationBinding.bcc.toString shouldBe clientStringList.toString
    operationBinding.priority.value() shouldBe 1
    operationBinding.deliveryMode.value() shouldBe 1
    operationBinding.mandatory.value() shouldBe true
    operationBinding.timestamp.value() shouldBe true
    operationBinding.ack.value() shouldBe true
  }

  test("test ChannelBindings") {
    val internalChannelBindings: Seq[amf.apicontract.client.scala.model.domain.bindings.ChannelBinding] =
      Seq(new Amqp091ChannelBinding010()._internal, new Amqp091ChannelBinding020()._internal)
    val clientChannelBindings = internalChannelBindings.asClient

    val channelBindings = new ChannelBindings()
      .withName(s)
      .withBindings(clientChannelBindings)
    channelBindings.name.value() shouldBe s
    channelBindings.bindings.asInternal shouldBe clientChannelBindings.asInternal
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
    val binding01 = new HttpOperationBinding010()
      .withBindingVersion(s)
      .withMethod(s)
      .withQuery(shape)
      .withOperationType(s)
    binding01.method.value() shouldBe s
    binding01.operationType.value() shouldBe s
    binding01.query._internal shouldBe shape._internal

    val binding02 = new HttpOperationBinding020()
      .withBindingVersion(s)
      .withMethod(s)
      .withQuery(shape)
    binding02.method.value() shouldBe s
    binding02.query._internal shouldBe shape._internal
  }

  test("test KafkaMessageBinding") {
    val binding010 = new KafkaMessageBinding010()
      .withBindingVersion(s)
      .withKey(shape)

    val binding030 = new KafkaMessageBinding030()
      .withBindingVersion(s)
      .withKey(shape)
      .withSchemaIdLocation(s)
      .withSchemaIdPayloadEncoding(s)
      .withSchemaLookupStrategy(s)

    binding010.messageKey._internal shouldBe shape._internal
    binding030.messageKey._internal shouldBe shape._internal
    binding030.schemaIdLocation.value() shouldBe s
    binding030.schemaIdPayloadEncoding.value() shouldBe s
    binding030.schemaLookupStrategy.value() shouldBe s
  }

  test("test KafkaOperationBinding") {
    val binding = new KafkaOperationBinding()
      .withBindingVersion(s)
      .withClientId(shape)
      .withGroupId(shape)
    binding.clientId._internal shouldBe shape._internal
    binding.groupId._internal shouldBe shape._internal
  }

  test("test KafkaServerBinding") {
    val binding = new KafkaServerBinding()
      .withBindingVersion(s)
      .withSchemaRegistryUrl(s)
      .withSchemaRegistryVendor(s)
    binding.schemaRegistryUrl.value() shouldBe s
    binding.schemaRegistryVendor.value() shouldBe s
  }

  test("test KafkaChannelBinding") {
    val binding030 = new KafkaChannelBinding030()
      .withBindingVersion(s)
      .withTopic(s)
      .withPartitions(123)
      .withReplicas(123)
    binding030.topic.value() shouldBe s
    binding030.partitions.value() shouldBe 123
    binding030.replicas.value() shouldBe 123

    val topicConfiguration040 = new KafkaTopicConfiguration040()
      .withCleanupPolicy(stringSeq.asClient)
      .withRetentionMs(123)
      .withRetentionBytes(123)
      .withDeleteRetentionMs(123)
      .withMaxMessageBytes(123)
    topicConfiguration040.cleanupPolicy.toString.contains(s) shouldBe true
    topicConfiguration040.retentionMs.value() shouldBe 123
    topicConfiguration040.retentionBytes.value() shouldBe 123
    topicConfiguration040.deleteRetentionMs.value() shouldBe 123
    topicConfiguration040.maxMessageBytes.value() shouldBe 123

    val binding040 = new KafkaChannelBinding040()
      .withBindingVersion(s)
      .withTopic(s)
      .withPartitions(123)
      .withReplicas(123)
      .withTopicConfiguration(topicConfiguration040)
    binding040.topic.value() shouldBe s
    binding040.partitions.value() shouldBe 123
    binding040.replicas.value() shouldBe 123
    binding040.topicConfiguration._internal shouldBe topicConfiguration040._internal

    val topicConfiguration050 = new KafkaTopicConfiguration050()
      .withCleanupPolicy(stringSeq.asClient)
      .withRetentionMs(123)
      .withRetentionBytes(123)
      .withDeleteRetentionMs(123)
      .withMaxMessageBytes(123)
      .withConfluentKeySchemaValidation(true)
      .withConfluentKeySubjectNameStrategy(s)
      .withConfluentValueSchemaValidation(false)
      .withConfluentValueSubjectNameStrategy(s)
    topicConfiguration050.cleanupPolicy.toString.contains(s) shouldBe true
    topicConfiguration050.retentionMs.value() shouldBe 123
    topicConfiguration050.retentionBytes.value() shouldBe 123
    topicConfiguration050.deleteRetentionMs.value() shouldBe 123
    topicConfiguration050.maxMessageBytes.value() shouldBe 123
    topicConfiguration050.confluentKeySchemaValidation.value() shouldBe true
    topicConfiguration050.confluentKeySubjectNameStrategy.value() shouldBe s
    topicConfiguration050.confluentValueSchemaValidation.value() shouldBe false
    topicConfiguration050.confluentValueSubjectNameStrategy.value() shouldBe s

    val binding050 = new KafkaChannelBinding050()
      .withBindingVersion(s)
      .withTopic(s)
      .withPartitions(123)
      .withReplicas(123)
      .withTopicConfiguration(topicConfiguration050)
    binding050.topic.value() shouldBe s
    binding050.partitions.value() shouldBe 123
    binding050.replicas.value() shouldBe 123
    binding050.topicConfiguration._internal shouldBe topicConfiguration050._internal
  }

  test("test MessageBindings") {
    val bindings: Seq[amf.apicontract.client.scala.model.domain.bindings.MessageBinding] =
      Seq(new Amqp091MessageBinding()._internal)
    val clientBindings = bindings.asClient
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
    val OperationBindings: Seq[amf.apicontract.client.scala.model.domain.bindings.OperationBinding] =
      Seq(new MqttOperationBinding()._internal)
    val clientOperationBindings = OperationBindings.asClient
    val operationBindings = new OperationBindings()
      .withName(s)
      .withBindings(clientOperationBindings)
    operationBindings.name.value() shouldBe s
    operationBindings.bindings.asInternal shouldBe clientOperationBindings.asInternal
  }

  test("test ServerBindings") {
    val internalServerBindings: Seq[amf.apicontract.client.scala.model.domain.bindings.ServerBinding] =
      Seq(new MqttServerBinding()._internal)
    val clientServerBindings: ClientList[ServerBinding] = internalServerBindings.asClient
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
