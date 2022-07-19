package amf.validation

import amf.core.internal.remote.{Async20JsonHint, Async20YamlHint, Hint}
import org.scalatest.matchers.should.Matchers

class Async20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {

  val asyncPath: String            = "file://amf-cli/shared/src/test/resources/validations/async20/"
  override val basePath: String    = asyncPath + "validations/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/async20/"

  test("Required channel object") {
    validate("required-channels.yaml", Some("required-channels.report"))
  }

  test("Required title in info object") {
    validate("required-info-title.yaml", Some("required-info-title.report"))
  }

  test("Required version in info object") {
    validate("required-info-version.yaml", Some("required-info-version.report"))
  }

  test("Required license name") {
    validate("required-license-name.yaml", Some("required-license-name.report"))
  }

  test("Server name must comply with pattern") {
    validate("server-name-pattern.yaml", Some("server-name-pattern.report"))
  }

  test("Mandatory server url") {
    validate("mandatory-server-url.yaml", Some("mandatory-server-url.report"))
  }

  test("Mandatory server protocol") {
    validate("mandatory-server-protocol.yaml", Some("mandatory-server-protocol.report"))
  }

  test("Channel name must conform with RFC 6570 URI template") {
    validate("channel-name-format.yaml", Some("channel-name-format.report"))
  }

  test("OperationId must be unique") {
    validate("duplicate-operation-id.yaml", Some("duplicate-operation-id.report"))
  }

  test("Parameter name must comply with regex") {
    validate("parameter-name-regex.yaml", Some("parameter-name-regex.report"))
  }

  test("Required tag name") {
    validate("required-tag-name.yaml", Some("required-tag-name.report"))
  }

  test("Required external documentation url") {
    validate("required-documentation-url.yaml", Some("required-documentation-url.report"))
  }

  test("Required security scheme type") {
    validate("required-security-scheme-type.yaml", Some("required-security-scheme-type.report"))
  }

  test("Security scheme type must meet valid values") {
    validate("security-scheme-valid-types.yaml", Some("security-scheme-valid-types.report"))
  }

  test("Required correlation id location") {
    validate("required-correlation-id-location.yaml", Some("required-correlation-id-location.report"))
  }

  test("Required httpApiKey scheme name value") {
    validate("required-httpApiKey-name.yaml", Some("required-httpApiKey-name.report"))
  }

  test("Required openIdConnect url") {
    validate("required-openIdConnect-url.yaml", Some("required-openIdConnect-url.report"))
  }

  test("Required OAuth2 flows") {
    validate("required-oauth2-flows.yaml", Some("required-oauth2-flows.report"))
  }

  test("Required http scheme field") {
    validate("required-http-scheme.yaml", Some("required-http-scheme.report"))
  }

  test("Required OAuth2 authorizatinUrl for implicit and authorizationCode") {
    validate("required-oauth2-authorizationUrl.yaml", Some("required-oauth2-authorizationUrl.report"))
  }

  test("Required OAuth2 tokenUrl for all but implicit") {
    validate("required-oauth2-tokenUrl.yaml", Some("required-oauth2-tokenUrl.report"))
  }

  test("Required OAuth2 scopes for all") {
    validate("required-oauth2-scopes.yaml", Some("required-oauth2-scopes.report"))
  }

  test("Required in field for apiKey and httpApiKey scheme") {
    validate("required-in-field.yaml", Some("required-in-field.report"))
  }

  test("Required httpOperationBinding type") {
    validate("required-httpOperationBinding-type.yaml", Some("required-httpOperationBinding-type.report"))
  }

  test("HttpOperationBinding type must be request or response") {
    validate("required-httpOperationBinding-type-values.yaml", Some("required-httpOperationBinding-type-values.report"))
  }

  test("HttpOperationBinding method must be an HTTP operation") {
    validate(
      "required-httpOperationBinding-method-values.yaml",
      Some("required-httpOperationBinding-method-values.report")
    )
  }

  test("WebSocketChannelBinding method must be GET or POST") {
    validate("ws-channel-binding-valid-method.yaml", Some("ws-channel-binding-valid-method.report"))
  }

  test("AmqpChannelBinding is field must be request or response") {
    validate("amqp-channel-binding-is-value.yaml", Some("amqp-channel-binding-is-value.report"))
  }

  test("AmqpChannelBinding name max length of 255") {
    validate("amqp-channel-binding-name-max-length.yaml", Some("amqp-channel-binding-name-max-length.report"))
  }

  test("LastWill binding Qos field value must be 0, 1 or 2") {
    validate("last-will-qos.yaml", Some("last-will-qos.report"))
  }

  test("MqttOperationBinding Qos field value must be 0, 1 or 2") {
    validate("mqtt-operation-binding-qos.yaml", Some("mqtt-operation-binding-qos.report"))
  }

  test("AmqpOperationBinding deliveryMode field value must be 1 or 2") {
    validate("amqp-operation-binding-deliveryMode.yaml", Some("amqp-operation-binding-deliveryMode.report"))
  }

  test("AmqpOperationBinding expiration field value must greater than or equal to 0") {
    validate("amqp-operation-binding-expiration.yaml", Some("amqp-operation-binding-expiration.report"))
  }

  test("WsSocketChannelBinding query and header field must be an object type and have properties key") {
    validate("ws-channel-binding-header-query.yaml", Some("ws-channel-binding-header-query.report"))
  }

  test("HttpOperationBinding query field must be an object type and have properties key") {
    validate("http-operation-query.yaml", Some("http-operation-query.report"))
  }

  test("HttpMessageBinding headers field must be an object type and have properties key") {
    validate("http-message-headers.yaml", Some("http-message-headers.report"))
  }

  ignore("Nested external correlationId refs") {
    validate("nested-libraries/nested-correlationIds/api.yaml")
  }

  test("Nested external operation refs") {
    validate(
      "nested-libraries/nested-operation-traits/api.yaml",
      Some("nested-libraries/nested-external-operation-trait-refs.report")
    )
  }

  test("Valid message trait node") {
    validate("valid-messageTrait-node.yaml")
  }

  test("httpApiKey and apiKey 'in' facet validation") {
    validate("security-scheme-in-facet.yaml", Some("invalid-in-facet-security-scheme.report"))
  }

  test("async runtime expression validations") {
    validate("invalid-runtime-expressions.yaml", Some("invalid-runtime-expressions.report"))
  }

  test("JsonReference is invalid with '#' only") {
    validate("json-reference/invalid-json-reference-format.yaml", Some("invalid-json-reference-format.report"))
  }

  test("Several url formats") {
    validate("several-url-formats.yaml", Some("several-url-formats.report"))
  }

  test("Invalid Id uri format") {
    validate("invalid-id-uri-format.yaml", Some("invalid-id-uri-format.report"))
  }

  test("Valid Id uri format") {
    validate("valid-id-uri-format.yaml")
  }

  test("Contact email format") {
    validate("contact-email-format.yaml", Some("contact-email-format.report"))
  }

  test("Message headers must type object") {
    validate("message-headers-object.yaml", Some("message-headers-object.report"))
  }

  test("Empty binding validation report should have location") {
    validate("empty-binding.yaml", Some("empty-binding.report"))
  }

  test("Invalid query parameter defined in channel uri") {
    validate("invalid-query-param-in-channel.yaml", Some("invalid-query-param-in-channel.report"))
  }

  test("Invalid fragment defined in channel uri") {
    validate("invalid-fragment-in-uri.yaml", Some("invalid-fragment-in-uri.report"))
  }

  test("Invalid yaml tags") {
    validate("invalid-yaml-tags.yaml", Some("invalid-yaml-tags.report"))
  }

  test("Valid header binding names according to RFC-7230") {
    validate("invalid-header-names.yaml", Some("invalid-header-names.report"))
  }

  test("Invalid binding names") {
    validate("invalid-binding-names.yaml", Some("invalid-binding-names.report"))
  }

  test("Discriminator property has to be included in required properties") {
    validate("discriminator-in-required-fields.yaml", Some("discriminator-in-required-fields.report"))
  }

  test("JSON with duplicate keys") {
    validate(
      "duplicate-keys.json",
      Some("duplicate-keys.report")
    )
  }

  test("Components must use keys with certain regex") {
    validate("invalid-component-names.yaml", Some("invalid-component-names.report"))
  }

  test("Closed shape in components object") {
    validate("components-closed-shape.yaml", Some("components-closed-shape.report"))
  }

  test("Using $ref within inlined raml content") {
    validate(
      "invalid-inlined-ref.yaml",
      Some("invalid-inlined-ref.report"),
      directory = asyncPath + "raml-data-type-references/"
    )
  }

  test("Invalid relative pointer to raml library content") {
    validate(
      "invalid-relative-pointer-to-lib.yaml",
      Some("invalid-relative-pointer-to-lib.report"),
      directory = asyncPath + "raml-data-type-references/"
    )
  }

  test("Verify isolated raml context in inlined raml content") {
    validate("invalid-ref-to-async-type.yaml", Some("invalid-ref-to-async-type.report"))
  }

  test("Verify isolated raml context for raml content in external yaml") {
    validate(
      "ref-invalid-external-yaml.yaml",
      Some("ref-invalid-external-yaml.report"),
      directory = asyncPath + "raml-data-type-references/"
    )
  }

  test("Invalid reference to type defined in raml api") {
    validate(
      "invalid-ref-to-raml-api.yaml",
      Some("invalid-ref-to-raml-api.report"),
      directory = asyncPath + "raml-data-type-references/"
    )
  }

  test("Reference to invalid library type") {
    validate(
      "ref-type-in-library-invalid.yaml",
      Some("ref-type-in-library-invalid.report"),
      directory = asyncPath + "raml-data-type-references/"
    )
  }

  test("Closed shape in keys of message examples") {
    validate("invalid-keys-message-examples.yaml", Some("invalid-keys-message-examples.report"))
  }

  test("Validate ref key in operation object") {
    validate("invalid-ref-key-operation.yaml", Some("invalid-ref-key-operation.report"))
  }

  test("Valid ref key in message trait defined in components") {
    validate("external-reference/valid-external-ref-message-trait.yaml")
  }

  test("Valid ref key in operation trait defined in components") {
    validate("external-reference/valid-external-ref-operation-trait.yaml")
  }

  test("Valid ref to message with another ref") {
    validate("double-references/valid-ref-to-message-with-ref.yaml")
  }

  test("Valid ref to operation with another ref") {
    validate("double-references/valid-ref-to-operation-with-ref.yaml")
  }

  test("Valid ref to parameter with another ref") {
    validate("double-references/valid-ref-to-parameter-with-ref.yaml")
  }

  test("Valid ref to bindings with another ref") {
    validate("double-references/valid-ref-to-bindings-with-ref.yaml")
  }

}
