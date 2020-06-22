package amf.validation

import amf.Async20Profile
import amf.core.remote.{AsyncYamlHint, Hint}
import org.scalatest.Matchers

class Async20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/async20/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/async20/"
  override val hint: Hint          = AsyncYamlHint

  test("Required channel object") {
    validate("required-channels.yaml", Some("required-channels.report"), Async20Profile)
  }

  test("Required title in info object") {
    validate("required-info-title.yaml", Some("required-info-title.report"), Async20Profile)
  }

  test("Required version in info object") {
    validate("required-info-version.yaml", Some("required-info-version.report"), Async20Profile)
  }

  test("Required license name") {
    validate("required-license-name.yaml", Some("required-license-name.report"), Async20Profile)
  }

  test("Server name must comply with pattern") {
    validate("server-name-pattern.yaml", Some("server-name-pattern.report"), Async20Profile)
  }

  test("Mandatory server url") {
    validate("mandatory-server-url.yaml", Some("mandatory-server-url.report"), Async20Profile)
  }

  test("Mandatory server protocol") {
    validate("mandatory-server-protocol.yaml", Some("mandatory-server-protocol.report"), Async20Profile)
  }

  test("Channel name must conform with RFC 6570 URI template") {
    validate("channel-name-format.yaml", Some("channel-name-format.report"), Async20Profile)
  }

  test("OperationId must be unique") {
    validate("duplicate-operation-id.yaml", Some("duplicate-operation-id.report"), Async20Profile)
  }

  test("Parameter name must comply with regex") {
    validate("parameter-name-regex.yaml", Some("parameter-name-regex.report"), Async20Profile)
  }

  test("Required tag name") {
    validate("required-tag-name.yaml", Some("required-tag-name.report"), Async20Profile)
  }

  test("Required external documentation url") {
    validate("required-documentation-url.yaml", Some("required-documentation-url.report"), Async20Profile)
  }

  test("Required security scheme type") {
    validate("required-security-scheme-type.yaml", Some("required-security-scheme-type.report"), Async20Profile)
  }

  test("Security scheme type must meet valid values") {
    validate("security-scheme-valid-types.yaml", Some("security-scheme-valid-types.report"), Async20Profile)
  }

  test("Required correlation id location") {
    validate("required-correlation-id-location.yaml", Some("required-correlation-id-location.report"), Async20Profile)
  }

  test("Required httpApiKey scheme name value") {
    validate("required-httpApiKey-name.yaml", Some("required-httpApiKey-name.report"), Async20Profile)
  }

  test("Required openIdConnect url") {
    validate("required-openIdConnect-url.yaml", Some("required-openIdConnect-url.report"), Async20Profile)
  }

  test("Required OAuth2 flows") {
    validate("required-oauth2-flows.yaml", Some("required-oauth2-flows.report"), Async20Profile)
  }

  test("Required http scheme field") {
    validate("required-http-scheme.yaml", Some("required-http-scheme.report"), Async20Profile)
  }

  test("Required OAuth2 authorizatinUrl for implicit and authorizationCode") {
    validate("required-oauth2-authorizationUrl.yaml", Some("required-oauth2-authorizationUrl.report"), Async20Profile)
  }

  test("Required OAuth2 tokenUrl for all but implicit") {
    validate("required-oauth2-tokenUrl.yaml", Some("required-oauth2-tokenUrl.report"), Async20Profile)
  }

  test("Required OAuth2 scopes for all") {
    validate("required-oauth2-scopes.yaml", Some("required-oauth2-scopes.report"), Async20Profile)
  }

  test("Required in field for apiKey and httpApiKey scheme") {
    validate("required-in-field.yaml", Some("required-in-field.report"), Async20Profile)
  }

  test("Required httpOperationBinding type") {
    validate("required-httpOperationBinding-type.yaml",
             Some("required-httpOperationBinding-type.report"),
             Async20Profile)
  }

  test("HttpOperationBinding type must be request or response") {
    validate("required-httpOperationBinding-type-values.yaml",
             Some("required-httpOperationBinding-type-values.report"),
             Async20Profile)
  }

  test("HttpOperationBinding method must be an HTTP operation") {
    validate("required-httpOperationBinding-method-values.yaml",
             Some("required-httpOperationBinding-method-values.report"),
             Async20Profile)
  }

  test("WebSocketChannelBinding method must be GET or POST") {
    validate("ws-channel-binding-valid-method.yaml", Some("ws-channel-binding-valid-method.report"), Async20Profile)
  }

  test("AmqpChannelBinding is field must be request or response") {
    validate("amqp-channel-binding-is-value.yaml", Some("amqp-channel-binding-is-value.report"), Async20Profile)
  }

  test("AmqpChannelBinding name max length of 255") {
    validate("amqp-channel-binding-name-max-length.yaml",
             Some("amqp-channel-binding-name-max-length.report"),
             Async20Profile)
  }

  test("LastWill binding Qos field value must be 0, 1 or 2") {
    validate("last-will-qos.yaml", Some("last-will-qos.report"), Async20Profile)
  }

  test("MqttOperationBinding Qos field value must be 0, 1 or 2") {
    validate("mqtt-operation-binding-qos.yaml", Some("mqtt-operation-binding-qos.report"), Async20Profile)
  }

  test("AmqpOperationBinding deliveryMode field value must be 1 or 2") {
    validate("amqp-operation-binding-deliveryMode.yaml",
             Some("amqp-operation-binding-deliveryMode.report"),
             Async20Profile)
  }

  test("AmqpOperationBinding expiration field value must greater than or equal to 0") {
    validate("amqp-operation-binding-expiration.yaml",
             Some("amqp-operation-binding-expiration.report"),
             Async20Profile)
  }

  test("WsSocketChannelBinding query and header field must be an object type and have properties key") {
    validate("ws-channel-binding-header-query.yaml", Some("ws-channel-binding-header-query.report"), Async20Profile)
  }

  test("HttpOperationBinding query field must be an object type and have properties key") {
    validate("http-operation-query.yaml", Some("http-operation-query.report"), Async20Profile)
  }

  test("HttpMessageBinding headers field must be an object type and have properties key") {
    validate("http-message-headers.yaml", Some("http-message-headers.report"), Async20Profile)
  }

  ignore("Nested external correlationId refs") {
    validate("nested-libraries/nested-correlationIds/api.yaml", None, Async20Profile)
  }

  test("Nested external operation refs") {
    validate("nested-libraries/nested-operation-traits/api.yaml",
             Some("nested-libraries/nested-external-operation-trait-refs.report"),
             Async20Profile)
  }

  test("Valid message trait node") {
    validate("valid-messageTrait-node.yaml", None, Async20Profile)
  }

  test("httpApiKey and apiKey 'in' facet validation") {
    validate("security-scheme-in-facet.yaml", Some("invalid-in-facet-security-scheme.report"), Async20Profile)
  }

  test("async runtime expression validations") {
    validate("invalid-runtime-expressions.yaml", Some("invalid-runtime-expressions.report"), Async20Profile)
  }

  test("JsonReference is invalid with '#' only") {
    validate("json-reference/invalid-json-reference-format.yaml",
             Some("invalid-json-reference-format.report"),
             Async20Profile)
  }

  test("Several url formats") {
    validate("several-url-formats.yaml", Some("several-url-formats.report"), Async20Profile)
  }

  test("Invalid Id uri format") {
    validate("invalid-id-uri-format.yaml", Some("invalid-id-uri-format.report"), Async20Profile)
  }

  test("Valid Id uri format") {
    validate("valid-id-uri-format.yaml", None, Async20Profile)
  }

  test("Contact email format") {
    validate("contact-email-format.yaml", Some("contact-email-format.report"), Async20Profile)
  }

  test("Message headers must type object") {
    validate("message-headers-object.yaml", Some("message-headers-object.report"), Async20Profile)
  }

  test("Empty binding validation report should have location") {
    validate("empty-binding.yaml", Some("empty-binding.report"), Async20Profile)
  }

  test("Invalid query parameter defined in channel") {
    validate("invalid-query-param-in-channel.yaml",
             Some("invalid-query-param-in-channel.report"),
             profile = Async20Profile)
  }
}
