package amf.apicontract.internal.configuration

object AwsOasDialect {
  val content =
    """
      |#%Dialect 1.0
      |dialect: AWS extensions
      |version: 1.0
      |
      |external:
      |  shacl: http://www.w3.org/ns/shacl#
      |  security: http://a.ml/vocabularies/security#
      |  data: http://a.ml/vocabularies/data#
      |  doc: http://a.ml/vocabularies/document#
      |  apiContract: http://a.ml/vocabularies/apiContract#
      |  core: http://a.ml/vocabularies/core#
      |  aws: http://a.ml/vocabularies/aws#
      |
      |documents: { }
      |
      |annotationMappings:
      |  IntegrationAnnotationMapping:
      |    domain: apiContract.Operation
      |    propertyTerm: aws.integration
      |    range: IntegrationNodeMapping
      |
      |  RequestValidatorsAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.requestValidators
      |    range: RequestValidatorNodeMapping
      |    mapKey: name
      |    allowMultiple: true
      |
      |  IntegrationsAnnotationMapping:
      |    domain: doc.Unit
      |    propertyTerm: aws.integrations
      |    range: IntegrationNodeMapping
      |    mapKey: name
      |
      |  ApiKeySourceAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.apiKeySource
      |    range: string
      |
      |  AuthTypeAnnotationMapping:
      |    domain: security.SecurityScheme
      |    propertyTerm: aws.authType
      |    range: string
      |
      |  RequestValidatorAnnotationMapping:
      |    domain: doc.DomainElement # should be apiContract.WebAPI & apiContract.Operation
      |    propertyTerm: aws.requestValidator
      |    range: string
      |
      |  ImportExportVersionAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.importExportVersion
      |    range: string
      |
      |  MinimumCompressionSizeAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.minimumCompressionSize
      |    range: integer
      |
      |  TagValueAnnotationMapping:
      |    domain: apiContract.Tag
      |    propertyTerm: aws.tag
      |    range: string
      |
      |  CorsAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.cors
      |    range: CorsNodeMapping
      |
      |  AuthAnnotationMapping:
      |    domain: apiContract.Operation
      |    propertyTerm: aws.auth
      |    range: AuthNodeMapping
      |
      |  AuthorizerAnnotationMapping:
      |    domain: security.SecurityScheme
      |    propertyTerm: aws.authorizer
      |    range: AuthorizerNodeMapping
      |
      |  BinaryMediaTypesAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.binaryMediaTypes
      |    range: string
      |    allowMultiple: true
      |
      |  GatewayResponsesAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.gatewayResponses
      |    range: GatewayResponseNodeMapping
      |    allowMultiple: true
      |    mapKey: name
      |
      |  DocumentationAnnotationMapping:
      |    domain: apiContract.WebAPI
      |    propertyTerm: aws.documentation
      |    range: DocumentationNodeMapping
      |
      |  EndpointConfigurationAnnotationMapping:
      |    domain: doc.DomainElement # should be OAS 2.0 -> apiContract.WebAPI & OAS 3.0 -> apiContract.Server
      |    propertyTerm: aws.endpointConfiguration
      |    range: EndpointConfigurationNodeMapping
      |
      |#  PolicyAnnotationMapping:
      |#    domain: doc.DomainElement # doc does not specify domain
      |#    propertyTerm: aws.policy
      |#    range: PolicyNodeMapping
      |
      |nodeMappings:
      |  CorsNodeMapping:
      |    classTerm: aws.Cors
      |    mapping:
      |      allowOrigins:
      |        propertyTerm: aws.allowOrigins
      |        range: string
      |        allowMultiple: true
      |      allowCredentials:
      |        propertyTerm: aws.allowCredentials
      |        range: boolean
      |      exposeHeaders:
      |        propertyTerm: aws.exposeHeaders
      |        range: string
      |        allowMultiple: true
      |      maxAge:
      |        propertyTerm: aws.maxAge
      |        range: integer
      |      allowMethods:
      |        propertyTerm: aws.allowMethods
      |        range: string
      |        allowMultiple: true
      |      allowHeaders:
      |        propertyTerm: aws.allowHeaders
      |        range: string
      |        allowMultiple: true
      |
      |  AuthNodeMapping:
      |    classTerm: aws.Auth
      |    mapping:
      |      type:
      |        propertyTerm: aws.type
      |        range: string
      |
      |  AuthorizerNodeMapping:
      |    classTerm: aws.Authorizer
      |    mapping:
      |      type:
      |        propertyTerm: aws.type
      |        range: string
      |      authorizerUri:
      |        propertyTerm: aws.authorizerUri
      |        range: string
      |      authorizerCredentials:
      |        propertyTerm: aws.authorizerCredentials
      |        range: string
      |      authorizerPayloadFormatVersion:
      |        propertyTerm: aws.authorizerPayloadFormatVersion
      |        range: string
      |      enableSimpleResponses:
      |        propertyTerm: aws.enableSimpleResponses
      |        range: boolean
      |      identitySource:
      |        propertyTerm: aws.identitySource
      |        range: string
      |      jwtConfiguration:
      |        propertyTerm: aws.jwtConfiguration
      |        range: JwtConfigurationNodeMapping
      |      identityValidationExpression:
      |        propertyTerm: aws.identityValidationExpression
      |        range: string
      |      authorizerResultTtlInSeconds:
      |        propertyTerm: aws.authorizerResultTtlInSeconds
      |        range: integer # docs says string but is wrong
      |
      |  JwtConfigurationNodeMapping:
      |    classTerm: aws.JwtConfiguration
      |    mapping:
      |      audience:
      |        propertyTerm: aws.audience
      |        range: string
      |        allowMultiple: true
      |      issuer:
      |        propertyTerm: aws.issuer
      |        range: string
      |
      |  DocumentationNodeMapping:
      |    classTerm: aws.Documentation
      |    mapping:
      |      documentationParts:
      |        propertyTerm: aws.documentationParts
      |        range: DocumentationPartNodeMapping
      |        allowMultiple: true
      |      version:
      |        propertyTerm: aws.version
      |        range: string
      |
      |  DocumentationPartNodeMapping:
      |    classTerm: aws.DocumentationPart
      |    mapping:
      |      location:
      |        propertyTerm: aws.location
      |        range: DocumentationPartLocationNodeMapping
      |      properties:
      |        propertyTerm: aws.properties
      |        range: DocumentationPartPropertiesNodeMapping
      |
      |  DocumentationPartLocationNodeMapping:
      |    classTerm: aws.DocumentationPartLocationNodeMapping
      |    mapping:
      |      type:
      |        propertyTerm: aws.type
      |        range: string
      |
      |  DocumentationPartPropertiesNodeMapping:
      |    classTerm: aws.DocumentationPartProperties
      |    mapping:
      |      description:
      |        propertyTerm: aws.description
      |        range: string
      |      info:
      |        propertyTerm: aws.info
      |        range: DocumentationPartPropertyInfoNodeMapping
      |
      |  DocumentationPartPropertyInfoNodeMapping:
      |    classTerm: aws.DocumentationPartPropertyInfo
      |    mapping:
      |      description:
      |        propertyTerm: aws.description
      |        range: string
      |      version:
      |        propertyTerm: aws.version
      |        range: string
      |
      |  EndpointConfigurationNodeMapping:
      |    classTerm: aws.EndpointConfiguration
      |    mapping:
      |      disableExecuteApiEndpoint:
      |        propertyTerm: aws.disableExecuteApiEndpoint
      |        range: boolean
      |      vpcEndpointIds:
      |        propertyTerm: aws.vpcEndpointIds
      |        range: string
      |        allowMultiple: true
      |
      |  GatewayResponseNodeMapping:
      |    classTerm: aws.GatewayResponse
      |    mapping:
      |      name:
      |        propertyTerm: aws.name
      |        range: string
      |      responseParameters:
      |        propertyTerm: aws.responseParameters
      |        range: GatewayResponseParameterNodeMapping
      |        mapKey: gatewayResponseParameter
      |        mapValue: requestParameter
      |      responseTemplates:
      |        propertyTerm: aws.responseTemplates
      |        range: GatewayResponseTemplatesNodeMapping
      |        mapKey: contentType
      |        mapValue: mappingTemplate
      |      statusCode:
      |        propertyTerm: aws.statusCode
      |        range: string
      |
      |  # Defines a string-to-string map of key-value pairs to generate gateway response parameters from the incoming request
      |  # parameters or using literal strings
      |  GatewayResponseParameterNodeMapping:
      |    classTerm: aws.GatewayResponseParameter
      |    mapping:
      |      gatewayResponseParameter:
      |        propertyTerm: aws.gatewayResponseParameter
      |        range: string
      |      requestParameter:
      |        propertyTerm: aws.requestParameter
      |        range: string
      |
      |  # Defines GatewayResponse mapping templates, as a string-to-string map of key-value pairs, for a given gateway
      |  # response. For each key-value pair, the key is the content type. For example, "application/json" and the value is a
      |  # stringified mapping template for simple variable substitutions
      |  GatewayResponseTemplatesNodeMapping:
      |    classTerm: aws.GatewayResponseTemplate
      |    mapping:
      |      contentType:
      |        propertyTerm: aws.contentType
      |        range: string
      |      mappingTemplate:
      |        propertyTerm: aws.mappingTemplate
      |        range: string
      |
      |  IntegrationNodeMapping:
      |    classTerm: aws.Integration
      |    mapping:
      |      $ref:
      |        propertyTerm: aws.ref # hack to allow $ref to reference integration
      |        range: string
      |      name:
      |        propertyTerm: aws.name
      |        range: string
      |      cacheKeyParameters:
      |        propertyTerm: aws.cacheKeyParameters
      |        range: string
      |        allowMultiple: true
      |      cacheNamespace:
      |        propertyTerm: aws.cacheNamespace
      |        range: string
      |      connectionId:
      |        propertyTerm: aws.connectionId
      |        range: string
      |      connectionType:
      |        propertyTerm: aws.connectionType
      |        range: string
      |      credentials:
      |        propertyTerm: aws.credentials
      |        range: string
      |      contentHandling:
      |        propertyTerm: aws.contentHandling
      |        range: string
      |      httpMethod:
      |        propertyTerm: aws.httpMethod
      |        range: string
      |      integrationSubtype:
      |        propertyTerm: aws.integrationSubtype
      |        range: string
      |      passthroughBehavior:
      |        propertyTerm: aws.passthroughBehavior
      |        range: string
      |      payloadFormatVersion:
      |        propertyTerm: aws.payloadFormatVersion
      |        range: string
      |      requestParameters:
      |        propertyTerm: aws.requestParameters
      |        range: IntegrationRequestParametersNodeMapping
      |        mapKey: integrationRequestParameter
      |        mapValue: methodRequestParameter
      |      requestTemplates:
      |        propertyTerm: aws.requestTemplates
      |        range: IntegrationRequestTemplatesNodeMapping
      |        mapKey: mimeType
      |        mapValue: mappingTemplate
      |      responses:
      |        propertyTerm: aws.responses
      |        range: IntegrationResponseNodeMapping
      |        mapKey: statusPattern
      |      timeoutInMillis:
      |        propertyTerm: aws.timeoutInMillis
      |        range: integer
      |      type:
      |        propertyTerm: aws.type
      |        range: string
      |      tlsConfig:
      |        propertyTerm: aws.tlsConfig
      |        range: IntegrationTlsConfigNodeMapping
      |      uri:
      |        propertyTerm: aws.uri
      |        range: string
      |
      |  # Specifies mapping templates for a request payload of the specified MIME types
      |  IntegrationRequestTemplatesNodeMapping:
      |    classTerm: aws.IntegrationRequestTemplate
      |    mapping:
      |      mimeType:
      |        propertyTerm: aws.mimeType
      |        range: string
      |      mappingTemplate:
      |        propertyTerm: aws.mappingTemplate
      |        range: string
      |
      |  # For REST APIs, specifies mappings from named method request parameters to integration request parameters. The method
      |  # request parameters must be defined before being referenced.
      |  #
      |  # For HTTP APIs, specifies parameters that are passed to AWS_PROXY integrations with a specified integrationSubtype
      |  IntegrationRequestParametersNodeMapping:
      |    classTerm: aws.IntegrationRequestParameter
      |    mapping:
      |      methodRequestParameter:
      |        propertyTerm: aws.methodRequestParameter
      |        range: string
      |      integrationRequestParameter:
      |        propertyTerm: aws.integrationRequestParameter
      |        range: string
      |
      |  IntegrationResponseNodeMapping:
      |    classTerm: aws.IntegrationResponse
      |    mapping:
      |      statusPattern:
      |        propertyTerm: aws.statusPattern
      |        range: string
      |      statusCode:
      |        propertyTerm: aws.statusCode
      |        range: string
      |      responseTemplates:
      |        propertyTerm: aws.responseTemplates
      |        range: IntegrationResponseTemplatesNodeMapping
      |        mapKey: mimeType
      |        mapValue: mappingTemplate
      |      responseParameters:
      |        propertyTerm: aws.responseParameters
      |        range: IntegrationResponseParameterNodeMapping
      |        mapKey: methodResponseParameter
      |        mapValue: integrationResponseParameter
      |      contentHandling:
      |        propertyTerm: aws.contentHandling
      |        range: string
      |
      |  # Specifies mappings from integration method response parameters to method response parameters
      |  IntegrationResponseParameterNodeMapping:
      |    classTerm: aws.IntegrationResponseParameter
      |    mapping:
      |      methodResponseParameter:
      |        propertyTerm: aws.methodResponseParameter
      |        range: string
      |      integrationResponseParameter:
      |        propertyTerm: aws.integrationResponseParameter
      |        range: string
      |
      |  # Specifies mapping templates for a response payload of the specified MIME types.
      |  IntegrationResponseTemplatesNodeMapping:
      |    classTerm: aws.IntegrationResponseTemplates
      |    mapping:
      |      mimeType:
      |        propertyTerm: aws.mimeType
      |        range: string
      |      mappingTemplate:
      |        propertyTerm: aws.mappingTemplate
      |        range: string
      |
      |  IntegrationTlsConfigNodeMapping:
      |    classTerm: aws.IntegrationTlsConfig
      |    mapping:
      |      insecureSkipVerification:
      |        propertyTerm: aws.insecureSkipVerification
      |        range: boolean
      |      serverNameToVerify:
      |        propertyTerm: aws.serverNameToVerify
      |        range: string
      |
      |  # Docs dot specify a schema, deducing it from examples
      |  #  PolicyNodeMapping:
      |  #    classTerm: aws.Policy
      |  #    mapping:
      |  #      Version:
      |  #        propertyTerm: aws.version
      |  #        range: string
      |  #      Statement:
      |  #        propertyTerm: aws.statement
      |  #        range: PolicyStatementNodeMapping
      |  #        allowMultiple: true
      |  #
      |  #  PolicyStatementNodeMapping:
      |  #    classTerm: aws.PolicyStatement
      |  #    mapping:
      |  #      Effect:
      |  #        propertyTerm: aws.effect
      |  #        range: string
      |  #      Principal:
      |  #        propertyTerm: aws.principal
      |  #        range: [ string, PrincipalValueNodeMapping ] # TODO cannot have unions of string | object
      |  #        mapKey: principalType
      |  #      Action:
      |  #        propertyTerm: aws.action
      |  #        range: string
      |  #      Resource:
      |  #        propertyTerm: aws.resource
      |  #        range: string
      |  #        allowMultiple: true
      |  #      Condition:
      |  #        propertyTerm: aws.condition
      |  #        range: ConditionNodeMapping
      |  #        mapKey: conditionName
      |  #        mapValue: conditionParameter # TODO mapValue does not work with objects
      |  #
      |  #    PrincipalValueNodeMapping:
      |  #      classTerm: aws.PrincipalValue
      |  #      mapping:
      |  #        principalType:
      |  #          propertyTerm: aws.principalType
      |  #          range: string
      |  #        principalValue:
      |  #          propertyTerm: aws.principalValue
      |  #          range: string
      |  #          allowMultiple: true
      |  #
      |  #    ConditionNodeMapping:
      |  #      classTerm: aws.Condition
      |  #      mapping:
      |  #        conditionName:
      |  #          propertyTerm: aws.conditionName
      |  #          range: string
      |  #        conditionParameter:
      |  #          propertyTerm: aws.conditionParameter
      |  #          range: string
      |  #          mapKey: parameterName
      |  #          mapValue: parameterValue
      |  #          allowMultiple: true
      |  #
      |  #    ConditionParameterNodeMapping:
      |  #      classTerm: aws.ConditionParameter
      |  #      mapping:
      |  #        parameterName:
      |  #          propertyTerm: aws.parameterName
      |  #          range: string
      |  #        parameterValue:
      |  #          propertyTerm: aws.parameterValue
      |  #          range: string
      |
      |
      |
      |  RequestValidatorNodeMapping:
      |    classTerm: aws.RequestValidator
      |    mapping:
      |      name:
      |        propertyTerm: aws.name
      |        range: string
      |      validateRequestBody:
      |        propertyTerm: aws.validateRequestBody
      |        range: boolean
      |      validateRequestParameters:
      |        propertyTerm: aws.validateRequestParameters
      |        range: boolean
      |
      |extensions:
      |  amazon-apigateway-integration: IntegrationAnnotationMapping
      |  amazon-apigateway-request-validators: RequestValidatorsAnnotationMapping
      |  amazon-apigateway-integrations: IntegrationsAnnotationMapping
      |  amazon-apigateway-api-key-source: ApiKeySourceAnnotationMapping
      |  amazon-apigateway-authtype: AuthTypeAnnotationMapping
      |  amazon-apigateway-request-validator: RequestValidatorAnnotationMapping
      |  amazon-apigateway-importexport-version: ImportExportVersionAnnotationMapping
      |  amazon-apigateway-minimum-compression-size: MinimumCompressionSizeAnnotationMapping
      |  amazon-apigateway-tag-value: TagValueAnnotationMapping
      |  amazon-apigateway-cors: CorsAnnotationMapping
      |  amazon-apigateway-auth: AuthAnnotationMapping
      |  amazon-apigateway-authorizer: AuthorizerAnnotationMapping
      |  amazon-apigateway-binary-media-types: BinaryMediaTypesAnnotationMapping
      |  amazon-apigateway-gateway-responses: GatewayResponsesAnnotationMapping
      |  amazon-apigateway-documentation: DocumentationAnnotationMapping
      |  amazon-apigateway-endpoint-configuration: EndpointConfigurationAnnotationMapping
      |""".stripMargin

}
