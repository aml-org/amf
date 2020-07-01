package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.document.{WebApi => WebApiObject}
import org.scalatest.{FunSuite, Matchers}

class DomainModelTests extends FunSuite with Matchers {

  WebApiObject.register()

  val s                                    = "test string"
  val clientStringList: ClientList[String] = Seq(s).asClient
  val shape                                = new AnyShape()
  val parameters                           = Seq(new Parameter()._internal)
  val settings                             = new Settings()
  val creativeWork                         = new CreativeWork()

  test("test CreativeWork") {
    val creativeWork: CreativeWork = new CreativeWork()
      .withUrl(s)
      .withDescription(s)
      .withTitle(s)
    creativeWork.url.value() shouldBe s
    creativeWork.description.value() shouldBe s
    creativeWork.title.value() shouldBe s
  }

  test("test XMLSerializer") {
    val xmlSerialization = new XMLSerializer()
      .withAttribute(true)
      .withWrapped(false)
      .withName(s)
      .withNamespace(s)
      .withPrefix(s)
    xmlSerialization.attribute.value() shouldBe true
    xmlSerialization.wrapped.value() shouldBe false
    xmlSerialization.name.value() shouldBe s
    xmlSerialization.namespace.value() shouldBe s
    xmlSerialization.prefix.value() shouldBe s
  }

  test("test Example") {
    val scalarNode = new ScalarNode()

    val example = new Example()
      .withName(s)
      .withDisplayName(s)
      .withDescription(s)
      .withValue(s)
      .withStructuredValue(scalarNode)
      .withStrict(true)
      .withMediaType(s)
    example.name.value() shouldBe s
    example.displayName.value() shouldBe s
    example.description.value() shouldBe s
    example.value.value() shouldBe s
    example.structuredValue._internal should be(scalarNode._internal)
    example.strict.value() shouldBe true
    example.mediaType.value() shouldBe s
  }

  test("test Parameter") {
    val emptyPayloads = Seq(new Payload()._internal, new Payload()._internal)

    val parameter = new Parameter()
      .withName(s)
      .withParameterName(s)
      .withDescription(s)
      .withRequired(true)
      .withDeprecated(false)
      .withAllowEmptyValue(false)
      .withStyle(s)
      .withExplode(true)
      .withAllowReserved(false)
      .withBinding(s)
      .withSchema(shape)
      .withPayloads(emptyPayloads.asClient)
    parameter.name.value() shouldBe s
    parameter.parameterName.value() shouldBe s
    parameter.description.value() shouldBe s
    parameter.required.value() shouldBe true
    parameter.deprecated.value() shouldBe false
    parameter.allowEmptyValue.value() shouldBe false
    parameter.style.value() shouldBe s
    parameter.explode.value() shouldBe true
    parameter.allowReserved.value() shouldBe false
    parameter.binding.value() shouldBe s
    parameter.schema._internal should be(shape._internal)
    parameter.payloads.asInternal should be(emptyPayloads)
  }

  test("test Encoding") {
    val encoding = new Encoding()
      .withPropertyName(s)
      .withContentType(s)
      .withHeaders(parameters.asClient)
      .withStyle(s)
      .withExplode(true)
      .withAllowReserved(true)
    encoding.propertyName.value() shouldBe s
    encoding.contentType.value() shouldBe s
    encoding.headers.asInternal shouldBe parameters
    encoding.style.value() shouldBe s
    encoding.explode.value() shouldBe true
    encoding.allowReserved.value() shouldBe true
  }

  test("test Payload") {
    val examples  = Seq(new Example()._internal)
    val encodings = Seq(new Encoding()._internal)

    val payload = new Payload()
      .withName(s)
      .withMediaType(s)
      .withSchemaMediaType(s)
      .withSchema(shape)
      .withExamples(examples.asClient)
      .withEncodings(encodings.asClient)
    payload.name.value() shouldBe s
    payload.mediaType.value() shouldBe s
    payload.schemaMediaType.value() shouldBe s
    payload.schema._internal should be(shape._internal)
    payload.examples.asInternal shouldBe examples
    payload.encodings.asInternal shouldBe encodings
  }

  test("test Request") {
    val parameters = Seq(new Parameter()._internal)

    val request = new Request()
      .withRequired(true)
      .withQueryParameters(parameters.asClient)
      .withHeaders(parameters.asClient)
      .withQueryString(shape)
      .withUriParameters(parameters.asClient)
      .withCookieParameters(parameters.asClient)
    request.required.value() shouldBe true
    request.queryParameters.asInternal shouldBe parameters
    request.headers.asInternal shouldBe parameters
    request.queryString._internal shouldBe shape._internal
    request.uriParameters.asInternal shouldBe parameters
    request.cookieParameters.asInternal shouldBe parameters
  }

  test("test IriTemplateMapping") {
    val iriTemplateMapping = new IriTemplateMapping()
      .withTemplateVariable(s)
      .withLinkExpression(s)
    iriTemplateMapping.templateVariable.value() shouldBe s
    iriTemplateMapping.linkExpression.value() shouldBe s
  }

  test("test TemplatedLink") {
    val mapping = Seq(new IriTemplateMapping()._internal)
    val server  = new Server()

    val templatedLink = new TemplatedLink()
      .withName(s)
      .withDescription(s)
      .withTemplate(s)
      .withOperationId(s)
      .withOperationRef(s)
      .withMapping(mapping.asClient)
      .withRequestBody(s)
      .withServer(server)
    templatedLink.name.value() shouldBe s
    templatedLink.description.value() shouldBe s
    templatedLink.template.value() shouldBe s
    templatedLink.operationId.value() shouldBe s
    templatedLink.mapping.asInternal shouldBe mapping
    templatedLink.requestBody.value() shouldBe s
    templatedLink.server._internal shouldBe server._internal
  }

  test("test Response") {
    val parameters     = Seq(new Parameter()._internal)
    val templatedLinks = Seq(new TemplatedLink()._internal)

    val response = new Response()
      .withStatusCode(s)
      .withHeaders(parameters.asClient)
      .withLinks(templatedLinks.asClient)
    response.statusCode.value() shouldBe s
    response.headers.asInternal shouldBe parameters
    response.links.asInternal shouldBe templatedLinks
  }

  test("test SecurityScheme") {
    val settings  = new Settings()
    val responses = Seq(new Response()._internal)

    val securityScheme = new SecurityScheme()
      .withName(s)
      .withType(s)
      .withDisplayName(s)
      .withDescription(s)
      .withHeaders(parameters.asClient)
      .withQueryParameters(parameters.asClient)
      .withResponses(responses.asClient)
      .withSettings(settings)
      .withQueryString(shape)
    securityScheme.name.value() shouldBe s
    securityScheme.`type`.value() shouldBe s
    securityScheme.displayName.value() shouldBe s
    securityScheme.description.value() shouldBe s
    securityScheme.headers.asInternal shouldBe parameters
    securityScheme.queryParameters.asInternal shouldBe parameters
    securityScheme.responses.asInternal shouldBe responses
    securityScheme.settings._internal shouldBe settings._internal
    securityScheme.queryString._internal shouldBe shape._internal
  }

  test("test ParametrizedSecurityScheme") {
    val securityScheme = new SecurityScheme()

    val parametrizedSecurityScheme = new ParametrizedSecurityScheme()
      .withName(s)
      .withDescription(s)
      .withScheme(securityScheme)
      .withSettings(settings)
    parametrizedSecurityScheme.name.value() shouldBe s
    parametrizedSecurityScheme.description.value() shouldBe s
    parametrizedSecurityScheme.scheme._internal shouldBe securityScheme._internal
    parametrizedSecurityScheme.settings._internal shouldBe settings._internal
  }

  test("test SecurityRequirement") {
    val schemes = Seq(new ParametrizedSecurityScheme()._internal)

    val securityRequirement = new SecurityRequirement()
      .withName(s)
      .withSchemes(schemes.asClient)
    securityRequirement.name.value() shouldBe s
    securityRequirement.schemes.asInternal shouldBe schemes
  }

  test("test Tag") {
    val tag = new Tag()
      .withName(s)
      .withDescription(s)
      .withVariables(creativeWork)
    tag.name.value() shouldBe s
    tag.description.value() shouldBe s
    tag.documentation._internal shouldBe creativeWork._internal
  }

  test("test Callback") {
    val endpoint = new EndPoint()

    val callback = new Callback()
      .withName(s)
      .withExpression(s)
      .withEndpoint(endpoint)
    callback.name.value() shouldBe s
    callback.expression.value() shouldBe s
    callback.endpoint._internal shouldBe endpoint._internal
  }

  test("test MqttServerLastWil") {
    val mqttServerLastWill = new MqttServerLastWill()
      .withTopic(s)
      .withQos(2)
      .withRetain(true)
    mqttServerLastWill.topic.value() shouldBe s
    mqttServerLastWill.qos.value() shouldBe 2
    mqttServerLastWill.retain.value() shouldBe true
  }

  test("test Server") {
    val serverBindings = new ServerBindings()
    val requirements   = Seq(new SecurityRequirement()._internal)

    val server = new Server()
      .withUrl(s)
      .withDescription(s)
      .withVariables(parameters.asClient)
      .withProtocol(s)
      .withProtocolVersion(s)
      .withSecurity(requirements.asClient)
      .withBindings(serverBindings)
    server.url.value() shouldBe s
    server.description.value() shouldBe s
    server.variables.asInternal shouldBe parameters
    server.protocol.value() shouldBe s
    server.protocolVersion.value() shouldBe s
    server.security.asInternal shouldBe requirements
    server.bindings._internal shouldBe serverBindings._internal
  }

  test("test Operation") {
    val operationBindings = new OperationBindings()
    val request           = new Request()
    val responses         = Seq(new Response()._internal)
    val requirements      = Seq(new SecurityRequirement()._internal)
    val tags              = Seq(new Tag()._internal)
    val callbacks         = Seq(new Callback()._internal)
    val servers           = Seq(new Server()._internal)

    val operation = new Operation()
      .withMethod(s)
      .withName(s)
      .withDescription(s)
      .withDeprecated(false)
      .withSummary(s)
      .withDocumentation(creativeWork)
      .withSchemes(clientStringList)
      .withAccepts(clientStringList)
      .withContentType(clientStringList)
      .withRequest(request)
      .withResponses(responses.asClient)
      .withSecurity(requirements.asClient)
      .withTags(tags.asClient)
      .withCallbacks(callbacks.asClient)
      .withServers(servers.asClient)
      .withAbstract(false)
      .withBindings(operationBindings)
      .withOperationId(s)
    operation.method.value() shouldBe s
    operation.name.value() shouldBe s
    operation.description.value() shouldBe s
    operation.deprecated.value() shouldBe false
    operation.summary.value() shouldBe s
    operation.documentation should be(creativeWork)
    operation.schemes.toString shouldBe clientStringList.toString
    operation.accepts.toString shouldBe clientStringList.toString
    operation.contentType.toString shouldBe clientStringList.toString
    operation.request._internal shouldBe request._internal
    operation.responses.asInternal shouldBe responses
    operation.security.asInternal shouldBe requirements
    operation.tags.asInternal shouldBe tags
    operation.callbacks.asInternal shouldBe callbacks
    operation.servers.asInternal shouldBe servers
    operation.isAbstract.value() shouldBe false
    operation.bindings._internal shouldBe operationBindings._internal
    operation.operationId.value() shouldBe s
  }

  test("test EndPoint") {
    val operations      = Seq(new Operation()._internal)
    val parameters      = Seq(new Parameter()._internal)
    val payloads        = Seq(new Payload()._internal)
    val servers         = Seq(new Server()._internal)
    val requirements    = Seq(new SecurityRequirement()._internal)
    val channelBindings = new ChannelBindings()

    val endpoint = new EndPoint()
      .withName(s)
      .withDescription(s)
      .withSummary(s)
      .withPath(s)
      .withOperations(operations.asClient)
      .withParameters(parameters.asClient)
      .withPayloads(payloads.asClient)
      .withServers(servers.asClient)
      .withSecurity(requirements.asClient)
      .withBindings(channelBindings)
    endpoint.name.value() shouldBe s
    endpoint.description.value() shouldBe s
    endpoint.summary.value() shouldBe s
    endpoint.path.value() shouldBe s
    endpoint.operations.asInternal shouldBe operations
    endpoint.parameters.asInternal shouldBe parameters
    endpoint.payloads.asInternal shouldBe payloads
    endpoint.servers.asInternal shouldBe servers
    endpoint.security.asInternal shouldBe requirements
    endpoint.bindings._internal shouldBe channelBindings._internal
  }

  test("test CorrelationId") {
    val correlationId = new CorrelationId()
      .withDescription(s)
      .withIdLocation(s)
    correlationId.description.value() shouldBe s
    correlationId.idLocation.value() shouldBe s
  }

  test("test Scope") {
    val scope = new Scope()
      .withName(s)
      .withDescription(s)
    scope.name.value() shouldBe s
    scope.description.value() shouldBe s
  }

  test("test PropertyDependencies") {
    val propertyDepencencies = new PropertyDependencies()
      .withPropertySource(s)
      .withPropertyTarget(clientStringList)
    propertyDepencencies.source.value() shouldBe s
    propertyDepencencies.target.toString shouldBe clientStringList.toString
  }

  test("test WebApi") {
    val endpoints      = Seq(new EndPoint()._internal)
    val org            = new Organization()
    val license        = new License()
    val documentations = Seq(creativeWork._internal)
    val servers        = Seq(new Server()._internal)
    val requirements   = Seq(new SecurityRequirement()._internal)

    val webapi = new WebApi()
      .withName(s)
      .withDescription(s)
      .withIdentifier(s)
      .withSchemes(clientStringList)
      .withEndPoints(endpoints.asClient)
      .withAccepts(clientStringList)
      .withContentType(clientStringList)
      .withVersion(s)
      .withTermsOfService(s)
      .withProvider(org)
      .withLicense(license)
      .withDocumentation(documentations.asClient)
      .withServers(servers.asClient)
      .withSecurity(requirements.asClient)
    webapi.name.value() shouldBe s
    webapi.description.value() shouldBe s
    webapi.identifier.value() shouldBe s
    webapi.schemes.toString shouldBe clientStringList.toString
    webapi.endPoints.asInternal shouldBe endpoints
    webapi.accepts.toString shouldBe clientStringList.toString
    webapi.contentType.toString shouldBe clientStringList.toString
    webapi.version.value() shouldBe s
    webapi.termsOfService.value() shouldBe s
    webapi.provider shouldBe org
    webapi.license shouldBe license
    webapi.documentations.asInternal shouldBe documentations
    webapi.servers.asInternal shouldBe servers
    webapi.security.asInternal shouldBe requirements
  }

  test("test License") {
    val license = new License()
      .withUrl(s)
      .withName(s)
    license.url.value() shouldBe s
    license.name.value() shouldBe s
  }

  test("test Message") {
    val tags          = Seq(new Tag()._internal)
    val examples      = Seq(new Example()._internal)
    val payloads      = Seq(new Payload()._internal)
    val correlationId = new CorrelationId()
    val bindings      = new MessageBindings()

    val message = new Message()
      .withName(s)
      .withDescription(s)
      .withAbstract(false)
      .withDocumentation(creativeWork)
      .withTags(tags.asClient)
      .withExamples(examples.asClient)
      .withPayloads(payloads.asClient)
      .withCorrelationId(correlationId)
      .withDisplayName(s)
      .withTitle(s)
      .withSummary(s)
      .withBindings(bindings)
    message.name.value() shouldBe s
    message.description.value() shouldBe s
    message.isAbstract.value() shouldBe false
    message.documentation shouldBe creativeWork
    message.tags.asInternal shouldBe tags
    message.examples.asInternal shouldBe examples
    message.payloads.asInternal shouldBe payloads
    message.correlationId shouldBe correlationId
    message.displayName.value() shouldBe s
    message.title.value() shouldBe s
    message.summary.value() shouldBe s
    message.bindings shouldBe bindings
  }

  test("test OAuth2Flow") {
    val scopes = Seq(new Scope()._internal)

    val flow = new OAuth2Flow()
      .withAuthorizationUri(s)
      .withAccessTokenUri(s)
      .withRefreshUri(s)
      .withScopes(scopes.asClient)
      .withFlow(s)
    flow.authorizationUri.value() shouldBe s
    flow.accessTokenUri.value() shouldBe s
    flow.refreshUri.value() shouldBe s
    flow.scopes.asInternal shouldBe scopes
    flow.flow.value() shouldBe s
  }

  test("test Organization") {
    val org = new Organization()
      .withUrl(s)
      .withName(s)
      .withEmail(s)
    org.url.value() shouldBe s
    org.name.value() shouldBe s
    org.email.value() shouldBe s
  }
}
