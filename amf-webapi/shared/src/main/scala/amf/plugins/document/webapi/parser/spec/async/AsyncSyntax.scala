package amf.plugins.document.webapi.parser.spec.async

import amf.plugins.document.webapi.parser.spec.SpecSyntax

object Async20Syntax extends SpecSyntax {

  private val shapeFacets = Set(
    "id",
    "$schema",
    "title",
    "displayName",
    "description",
    "default",
    "example",
    "examples",
    "enum",
    "type",
    "allOf",
    "anyOf",
    "oneOf",
    "not",
    "deprecationMessage",
    "required",
    "readOnly",
    "writeOnly",
    "const"
  )

  override val nodes: Map[String, Set[String]] = Map(
    "shape" -> shapeFacets,
    "nodeShape" -> (shapeFacets ++ Set(
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "additionalProperties",
      "patternProperties",
      "dependencies"
    )),
    "arrayShape" -> (shapeFacets ++ Set(
      "uniqueItems",
      "items",
      "minItems",
      "maxItems",
      "additionalItems"
    )),
    "stringScalarShape" -> (shapeFacets ++ Set(
      "pattern",
      "minLength",
      "maxLength",
      "format"
    )),
    "numberScalarShape" -> (shapeFacets ++ Set(
      "minimum",
      "maximum",
      "exclusiveMaximum",
      "exclusiveMinimum",
      "format",
      "multipleOf"
    )),
    "dateScalarShape" -> (shapeFacets + "format"),
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
    "amqpQueueChannelBinding" -> Set(
      "name",
      "durable",
      "exclusive",
      "autoDelete",
      "vhost"
    ),
    "amqpIsQueueChannelBinding" -> Set(
      "is",
      "queue",
      "bindingVersion"
    ),
    "amqpExchangeChannelBinding" -> Set(
      "name",
      "type",
      "durable",
      "autoDelete",
      "vhost"
    ),
    "amqpIsExchangeChannelBinding" -> Set(
      "is",
      "exchange",
      "bindingVersion"
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
      "retain"
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
    )
  )
}
