package amf.plugins.document.apicontract.parser.spec.async

import amf.plugins.document.apicontract.parser.spec.SpecSyntax

object Async20Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
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
    "amqpQueueChannelBinding" -> Set(
      "name",
      "durable",
      "exclusive",
      "autoDelete",
      "vhost"
    ),
    "amqpExchangeChannelBinding" -> Set(
      "name",
      "type",
      "durable",
      "autoDelete",
      "vhost"
    ),
    "httpOperationBinding" -> Set(
      "type",
      "method",
      "query",
      "bindingVersion"
    ),
    "amqpOperationBinding" -> Set(
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
    "amqpMessageBinding" -> Set(
      "contentEncoding",
      "messageType",
      "bindingVersion"
    ),
    "mqttOperationBinding" -> Set(
      "qos",
      "retain",
      "bindingVersion"
    ),
    "mqttServerBinding" -> Set(
      "clientId",
      "cleanSession",
      "lastWill",
      "keepAlive",
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
    "httpMessageBinding" -> Set(
      "headers",
      "bindingVersion"
    ),
    "kafkaMessageBinding" -> Set(
      "key",
      "bindingVersion"
    ),
    "kafkaOperationBinding" -> Set(
      "groupId",
      "clientId",
      "bindingVersion"
    ),
    // Async Schema Object
    "schema" -> Set(
      "$ref",
      "$schema",
      "$comment",
      "$id",
      "format",
      "title",
      "description",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "pattern",
      "maxItems",
      "minItems",
      "uniqueItems",
      "maxProperties",
      "minProperties",
      "required",
      "enum",
      "type",
      "items",
      "additionalItems",
      "collectionFormat",
      "allOf",
      "properties",
      "additionalProperties",
      "propertyNames",
      "discriminator",
      "readOnly",
      "writeOnly",
      "deprecated",
      "externalDocs",
      "allOf",
      "anyOf",
      "oneOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "examples",
      "if",
      "then",
      "else",
      "const",
      "contains",
      "name",
      "patternProperties"
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
