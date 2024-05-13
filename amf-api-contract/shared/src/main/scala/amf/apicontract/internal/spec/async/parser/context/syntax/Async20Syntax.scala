package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.async.parser.Async20ShapeSyntax
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async20Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Async20ShapeSyntax.nodes ++ Map(
    "webApi" -> Set(
      "asyncapi",
      "id",
      "info",
      "servers",
      "channels",
      "components",
      "tags",
      "externalDocs",
      "defaultContentType"
    ),
    "components" -> Set(
      "schemas",
      "messages",
      "securitySchemes",
      "parameters",
      "correlationIds",
      "operationTraits",
      "messageTraits",
      "serverBindings",
      "channelBindings",
      "operationBindings",
      "messageBindings"
    ),
    "server" -> Set(
      "url",
      "protocol",
      "protocolVersion",
      "description",
      "variables",
      "security",
      "bindings"
    ),
    "serverVariable" -> Set(
      "default",
      "description",
      "enum",
      "examples"
    ),
    "wsChannelBinding" -> Set(
      "method",
      "query",
      "headers",
      "bindingVersion"
    ),
    "amqpChannelBinding" -> Set(
      "is",
      "queue",
      "bindingVersion",
      "exchange"
    ),
    "amqpQueueChannelBinding010" -> Set(
      "name",
      "durable",
      "exclusive",
      "autoDelete"
    ),
    "amqpQueueChannelBinding020" -> Set(
      "name",
      "durable",
      "exclusive",
      "autoDelete",
      "vhost"
    ),
    "amqpExchangeChannelBinding010" -> Set(
      "name",
      "type",
      "durable",
      "autoDelete"
    ),
    "amqpExchangeChannelBinding020" -> Set(
      "name",
      "type",
      "durable",
      "autoDelete",
      "vhost"
    ),
    "httpOperationBinding010" -> Set(
      "type",
      "method",
      "query",
      "bindingVersion"
    ),
    "httpOperationBinding020" -> Set(
      "method",
      "query",
      "bindingVersion"
    ),
    "amqpOperationBinding010" -> Set(
      "expiration",
      "userId",
      "cc",
      "priority",
      "deliveryMode",
      "mandatory",
      "bcc",
      "replyTo",
      "timestamp",
      "ack",
      "bindingVersion"
    ),
    "amqpOperationBinding030" -> Set(
      "expiration",
      "userId",
      "cc",
      "priority",
      "deliveryMode",
      "mandatory",
      "bcc",
      "timestamp",
      "ack",
      "bindingVersion"
    ),
    "amqpMessageBinding" -> Set(
      "contentEncoding",
      "messageType",
      "bindingVersion"
    ),
    "mqttOperationBinding010" -> Set(
      "qos",
      "retain",
      "bindingVersion"
    ),
    "mqttOperationBinding020" -> Set(
      "qos",
      "retain",
      "messageExpiryInterval",
      "bindingVersion"
    ),
    "mqttServerBinding010" -> Set(
      "clientId",
      "cleanSession",
      "lastWill",
      "keepAlive",
      "bindingVersion"
    ),
    "mqttServerBinding020" -> Set(
      "clientId",
      "cleanSession",
      "lastWill",
      "keepAlive",
      "sessionExpiryInterval",
      "maximumPacketSize",
      "bindingVersion"
    ),
    "mqttServerLastWill" -> Set(
      "topic",
      "qos",
      "retain",
      "message"
    ),
    "mqttMessageBinding" -> Set(
      "bindingVersion"
    ),
    "httpMessageBinding020" -> Set(
      "headers",
      "bindingVersion"
    ),
    "httpMessageBinding030" -> Set(
      "headers",
      "statusCode",
      "bindingVersion"
    ),
    "kafkaMessageBinding010" -> Set(
      "key",
      "bindingVersion"
    ),
    "kafkaMessageBinding030" -> Set(
      "key",
      "schemaIdLocation",
      "schemaIdPayloadEncoding",
      "schemaLookupStrategy",
      "bindingVersion"
    ),
    "kafkaOperationBinding" -> Set(
      "groupId",
      "clientId",
      "bindingVersion"
    ),
    "kafkaServerBinding" -> Set(
      "schemaRegistryUrl",
      "schemaRegistryVendor",
      "bindingVersion"
    ),
    "kafkaChannelBinding030" -> Set(
      "topic",
      "partitions",
      "replicas",
      "bindingVersion"
    ),
    "kafkaChannelBinding040" -> Set(
      "topic",
      "partitions",
      "replicas",
      "topicConfiguration",
      "bindingVersion"
    ),
    "kafkaTopicConfiguration040" -> Set(
      "cleanup.policy",
      "retention.ms",
      "retention.bytes",
      "delete.retention.ms",
      "max.message.bytes"
    ),
    "kafkaTopicConfiguration050" -> Set(
      "cleanup.policy",
      "retention.ms",
      "retention.bytes",
      "delete.retention.ms",
      "max.message.bytes",
      "confluent.key.schema.validation",
      "confluent.key.subject.name.strategy",
      "confluent.value.schema.validation",
      "confluent.value.subject.name.strategy"
    ),
    "message" -> Set(
      "headers",
      "payload",
      "correlationId",
      "schemaFormat",
      "contentType",
      "name",
      "title",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings",
      "examples",
      "traits"
    ),
    "message examples" -> Set(
      "headers",
      "payload"
    ),
    "correlationId" -> Set(
      "description",
      "location"
    ),
    "parameter" -> Set(
      "description",
      "schema",
      "location"
    ),
    "pathItem" -> Set(
      "description",
      "subscribe",
      "publish",
      "parameters",
      "bindings"
    ),
    "operation" -> Set(
      "operationId",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings",
      "traits",
      "message"
    ),
    "operationTrait" -> Set(
      "operationId",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings"
    ),
    "messageTrait" -> Set(
      "headers",
      "correlationId",
      "schemaFormat",
      "contentType",
      "name",
      "title",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings",
      "examples"
    ),
    "info" -> Set(
      "title",
      "description",
      "termsOfService",
      "contact",
      "license",
      "version"
    ),
    "securityScheme" -> Set(
      "type",
      "description",
      "name",
      "in",
      "scheme",
      "bearerFormat",
      "flows",
      "openIdConnectUrl"
    ),
    "bindings" -> Set(
      "http",
      "ws",
      "kafka",
      "amqp",
      "amqp1",
      "mqtt",
      "mqtt5",
      "nats",
      "jms",
      "sns",
      "sqs",
      "stomp",
      "redis"
    )
  )
}
