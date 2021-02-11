
AMF Model Documentation
---
## Table of Contents
* [AbstractDeclaration](#abstractdeclaration)
* [Amqp091ChannelBinding](#amqp091channelbinding)
* [Amqp091ChannelExchange](#amqp091channelexchange)
* [Amqp091MessageBinding](#amqp091messagebinding)
* [Amqp091OperationBinding](#amqp091operationbinding)
* [Amqp091Queue](#amqp091queue)
* [AnnotationTypeDeclarationFragment](#annotationtypedeclarationfragment)
* [AnyShape](#anyshape)
* [ApiKeySettings](#apikeysettings)
* [ArrayNode](#arraynode)
* [ArrayShape](#arrayshape)
* [AsyncApi](#asyncapi)
* [BaseApi](#baseapi)
* [BaseUnit](#baseunit)
* [Callback](#callback)
* [ChannelBinding](#channelbinding)
* [ChannelBindings](#channelbindings)
* [ClassTerm](#classterm)
* [CorrelationId](#correlationid)
* [CreativeWork](#creativework)
* [CustomDomainProperty](#customdomainproperty)
* [DataNode](#datanode)
* [DataTypeFragment](#datatypefragment)
* [DatatypePropertyTerm](#datatypepropertyterm)
* [Dialect](#dialect)
* [DialectFragment](#dialectfragment)
* [DialectInstance](#dialectinstance)
* [DialectInstanceFragment](#dialectinstancefragment)
* [DialectInstanceLibrary](#dialectinstancelibrary)
* [DialectInstancePatch](#dialectinstancepatch)
* [DialectLibrary](#dialectlibrary)
* [Document](#document)
* [DocumentMapping](#documentmapping)
* [DocumentationItemFragment](#documentationitemfragment)
* [Documents](#documents)
* [DomainElement](#domainelement)
* [DomainExtension](#domainextension)
* [EmptyBinding](#emptybinding)
* [Encoding](#encoding)
* [EndPoint](#endpoint)
* [Example](#example)
* [Extension](#extension)
* [ExtensionLike](#extensionlike)
* [External](#external)
* [ExternalContextFields](#externalcontextfields)
* [ExternalDomainElement](#externaldomainelement)
* [ExternalFragment](#externalfragment)
* [ExternalSourceElement](#externalsourceelement)
* [FileShape](#fileshape)
* [Fragment](#fragment)
* [HttpApiKeySettings](#httpapikeysettings)
* [HttpMessageBinding](#httpmessagebinding)
* [HttpOperationBinding](#httpoperationbinding)
* [HttpSettings](#httpsettings)
* [IriTemplateMapping](#iritemplatemapping)
* [KafkaMessageBinding](#kafkamessagebinding)
* [KafkaOperationBinding](#kafkaoperationbinding)
* [License](#license)
* [LinkNode](#linknode)
* [LinkableElement](#linkableelement)
* [MatrixShape](#matrixshape)
* [Message](#message)
* [MessageBinding](#messagebinding)
* [MessageBindings](#messagebindings)
* [Module](#module)
* [MqttMessageBinding](#mqttmessagebinding)
* [MqttOperationBinding](#mqttoperationbinding)
* [MqttServerBinding](#mqttserverbinding)
* [MqttServerLastWill](#mqttserverlastwill)
* [NamedExampleFragment](#namedexamplefragment)
* [NilShape](#nilshape)
* [NodeMapping](#nodemapping)
* [NodeShape](#nodeshape)
* [OAuth1Settings](#oauth1settings)
* [OAuth2Flow](#oauth2flow)
* [OAuth2Settings](#oauth2settings)
* [ObjType](#objtype)
* [ObjectNode](#objectnode)
* [ObjectPropertyTerm](#objectpropertyterm)
* [OpenIdConnectSettings](#openidconnectsettings)
* [Operation](#operation)
* [OperationBinding](#operationbinding)
* [OperationBindings](#operationbindings)
* [Organization](#organization)
* [Overlay](#overlay)
* [Parameter](#parameter)
* [ParametrizedDeclaration](#parametrizeddeclaration)
* [ParametrizedResourceType](#parametrizedresourcetype)
* [ParametrizedSecurityScheme](#parametrizedsecurityscheme)
* [ParametrizedTrait](#parametrizedtrait)
* [Payload](#payload)
* [PayloadFragment](#payloadfragment)
* [PropertyDependencies](#propertydependencies)
* [PropertyMapping](#propertymapping)
* [PropertyShape](#propertyshape)
* [PublicNodeMapping](#publicnodemapping)
* [RecursiveShape](#recursiveshape)
* [Request](#request)
* [ResourceType](#resourcetype)
* [ResourceTypeFragment](#resourcetypefragment)
* [Response](#response)
* [ScalarNode](#scalarnode)
* [ScalarShape](#scalarshape)
* [SchemaDependencies](#schemadependencies)
* [SchemaShape](#schemashape)
* [Scope](#scope)
* [SecurityRequirement](#securityrequirement)
* [SecurityScheme](#securityscheme)
* [SecuritySchemeFragment](#securityschemefragment)
* [Server](#server)
* [ServerBinding](#serverbinding)
* [ServerBindings](#serverbindings)
* [Settings](#settings)
* [Shape](#shape)
* [ShapeExtension](#shapeextension)
* [SourceMap](#sourcemap)
* [Tag](#tag)
* [TemplatedLink](#templatedlink)
* [Trait](#trait)
* [TraitFragment](#traitfragment)
* [TupleShape](#tupleshape)
* [UnionNodeMapping](#unionnodemapping)
* [UnionShape](#unionshape)
* [VariableValue](#variablevalue)
* [Vocabulary](#vocabulary)
* [VocabularyReference](#vocabularyreference)
* [WebApi](#webapi)
* [WebSocketsChannelBinding](#websocketschannelbinding)
* [XMLSerializer](#xmlserializer)
## AbstractDeclaration
Graph template that can be used to declare a re-usable graph structure that can be applied to different domain elements
in order to re-use common semantics. Similar to a Lisp macro or a C++ template.
It can be extended by any domain element adding bindings for the variables in the declaration.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | dataNode | [DataNode](#datanode) | Associated dynamic structure for the declaration | http://a.ml/vocabularies/document#dataNode |
 | variable | [string] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Amqp091ChannelBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | is | string | Defines what type of channel is it | http://a.ml/vocabularies/apiBinding#is |
 | exchange | [Amqp091ChannelExchange](#amqp091channelexchange) | Defines the exchange properties | http://a.ml/vocabularies/apiBinding#exchange |
 | queue | [Amqp091Queue](#amqp091queue) | Defines the queue properties | http://a.ml/vocabularies/apiBinding#queue |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Amqp091ChannelExchange


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | type | string | The type of the exchange | http://a.ml/vocabularies/apiBinding#type |
 | durable | boolean | Whether the exchange should survive broker restarts or not | http://a.ml/vocabularies/apiBinding#durable |
 | autoDelete | boolean | Whether the exchange should be deleted when the last queue is unbound from it | http://a.ml/vocabularies/apiBinding#autoDelete |
 | vhost | string | The virtual host of the exchange | http://a.ml/vocabularies/apiBinding#vhost |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Amqp091MessageBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | contentEncoding | string | MIME encoding for the message content | http://a.ml/vocabularies/apiBinding#contentEncoding |
 | messageType | string | Application-specific message type | http://a.ml/vocabularies/apiBinding#messageType |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Amqp091OperationBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | expiration | int | TTL (Time-To-Live) for the message | http://a.ml/vocabularies/apiBinding#expiration |
 | userId | string | Identifies the user who has sent the message | http://a.ml/vocabularies/apiBinding#userId |
 | cc | [string] | The routing keys the message should be routed to at the time of publishing | http://a.ml/vocabularies/apiBinding#cc |
 | priority | int | A priority for the message | http://a.ml/vocabularies/apiBinding#priority |
 | deliveryMode | int | Delivery mode of the message | http://a.ml/vocabularies/apiBinding#deliveryMode |
 | mandatory | boolean | Whether the message is mandatory or not | http://a.ml/vocabularies/apiBinding#mandatory |
 | bcc | [string] | Like cc but consumers will not receive this information | http://a.ml/vocabularies/apiBinding#bcc |
 | replyTo | string | Name of the queue where the consumer should send the response | http://a.ml/vocabularies/apiBinding#replyTo |
 | timestamp | boolean | Whether the message should include a timestamp or not | http://a.ml/vocabularies/apiBinding#timestamp |
 | ack | boolean | Whether the consumer should ack the message or not | http://a.ml/vocabularies/apiBinding#ack |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Amqp091Queue


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | durable | boolean | Whether the exchange should survive broker restarts or not | http://a.ml/vocabularies/apiBinding#durable |
 | exclusive | boolean | Whether the queue should be used only by one connection or not | http://a.ml/vocabularies/apiBinding#exclusive |
 | autoDelete | boolean | Whether the exchange should be deleted when the last queue is unbound from it | http://a.ml/vocabularies/apiBinding#autoDelete |
 | vhost | string | The virtual host of the exchange | http://a.ml/vocabularies/apiBinding#vhost |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## AnnotationTypeDeclarationFragment
Fragment encoding a RAML annotation type

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## AnyShape
Base class for all shapes stored in the graph model

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |

## ApiKeySettings
Settings for an API Key security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string |  | http://a.ml/vocabularies/core#name |
 | in | string |  | http://a.ml/vocabularies/security#in |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ArrayNode
Node that represents a dynamic array data structure

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | member | [[DataNode](#datanode)] |  | http://www.w3.org/2000/01/rdf-schema#member |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ArrayShape
Shape that contains a nested collection of data shapes

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | items | [Shape](#shape) | Shapes inside the data arrangement | http://a.ml/vocabularies/shapes#items |
 | contains | [Shape](#shape) | One of the shapes in the data arrangement | http://a.ml/vocabularies/shapes#contains |
 | minCount | int | Minimum items count constraint | http://www.w3.org/ns/shacl#minCount |
 | maxCount | int | Maximum items count constraint | http://www.w3.org/ns/shacl#maxCount |
 | uniqueItems | boolean | Unique items constraint | http://a.ml/vocabularies/shapes#uniqueItems |
 | collectionFormat | string | Input collection format information | http://a.ml/vocabularies/shapes#collectionFormat |
 | unevaluatedItems | boolean | Accepts that items may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedItems |
 | unevaluatedItemsSchema | [Shape](#shape) | Items that may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedItemsSchema |
 | qualifiedMinCount | int | Minimum number of value nodes constraint | http://www.w3.org/ns/shacl#qualifiedMinCount |
 | qualifiedMaxCount | int | Maximum number of value nodes constraint | http://www.w3.org/ns/shacl#qualifiedMaxCount |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## AsyncApi
Top level element describing a asynchronous API

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | identifier | string | The identifier property represents any kind of identifier, such as ISBNs, GTIN codes, UUIDs, etc. | http://a.ml/vocabularies/core#identifier |
 | server | [[Server](#server)] | Server information | http://a.ml/vocabularies/apiContract#server |
 | accepts | [string] | Media-types accepted in a API request | http://a.ml/vocabularies/apiContract#accepts |
 | contentType | [string] | Media types returned by a API response | http://a.ml/vocabularies/apiContract#contentType |
 | scheme | [string] | URI scheme for the API protocol | http://a.ml/vocabularies/apiContract#scheme |
 | version | string | Version of the API | http://a.ml/vocabularies/core#version |
 | termsOfService | string | Terms and conditions when using the API | http://a.ml/vocabularies/core#termsOfService |
 | provider | [Organization](#organization) | Organization providing some kind of asset or service | http://a.ml/vocabularies/core#provider |
 | license | [License](#license) | License for the API | http://a.ml/vocabularies/core#license |
 | documentation | [[CreativeWork](#creativework)] | Documentation associated to the API | http://a.ml/vocabularies/core#documentation |
 | endpoint | [[EndPoint](#endpoint)] | End points defined in the API | http://a.ml/vocabularies/apiContract#endpoint |
 | security | [[SecurityRequirement](#securityrequirement)] | Textual indication of the kind of security scheme used | http://a.ml/vocabularies/security#security |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## BaseApi
Top level element describing any kind of API

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | identifier | string | The identifier property represents any kind of identifier, such as ISBNs, GTIN codes, UUIDs, etc. | http://a.ml/vocabularies/core#identifier |
 | server | [[Server](#server)] | Server information | http://a.ml/vocabularies/apiContract#server |
 | accepts | [string] | Media-types accepted in a API request | http://a.ml/vocabularies/apiContract#accepts |
 | contentType | [string] | Media types returned by a API response | http://a.ml/vocabularies/apiContract#contentType |
 | scheme | [string] | URI scheme for the API protocol | http://a.ml/vocabularies/apiContract#scheme |
 | version | string | Version of the API | http://a.ml/vocabularies/core#version |
 | termsOfService | string | Terms and conditions when using the API | http://a.ml/vocabularies/core#termsOfService |
 | provider | [Organization](#organization) | Organization providing some kind of asset or service | http://a.ml/vocabularies/core#provider |
 | license | [License](#license) | License for the API | http://a.ml/vocabularies/core#license |
 | documentation | [[CreativeWork](#creativework)] | Documentation associated to the API | http://a.ml/vocabularies/core#documentation |
 | endpoint | [[EndPoint](#endpoint)] | End points defined in the API | http://a.ml/vocabularies/apiContract#endpoint |
 | security | [[SecurityRequirement](#securityrequirement)] | Textual indication of the kind of security scheme used | http://a.ml/vocabularies/security#security |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## BaseUnit
Base class for every single document model unit. After parsing a document the parser generate parsing Units. Units encode the domain elements and can reference other units to re-use descriptions.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Callback
Model defining the information for a HTTP callback/ webhook

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | expression | string | Structural location of the information to fulfill the callback | http://a.ml/vocabularies/apiContract#expression |
 | endpoint | [EndPoint](#endpoint) | Endpoint targeted by the callback | http://a.ml/vocabularies/apiContract#endpoint |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ChannelBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ChannelBindings


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | bindings | [[ChannelBinding](#channelbinding)] | List of channel bindings | http://a.ml/vocabularies/apiBinding#bindings |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ClassTerm


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the ClassTerm | http://a.ml/vocabularies/core#name |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | description | string | Human readable description for the term | http://a.ml/vocabularies/core#description |
 | properties | [url] | Properties that have the ClassTerm in the domain | http://a.ml/vocabularies/meta#properties |
 | subClassOf | [url] | Subsumption relationship across terms | http://www.w3.org/2000/01/rdf-schema#subClassOf |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## CorrelationId
Model defining an identifier that can used for message tracing and correlation

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | location | string | Structural location of a piece of information | http://a.ml/vocabularies/core#location |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## CreativeWork
The most generic kind of creative work, including books, movies, photographs, software programs, etc.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | url | url | URL for the creative work | http://a.ml/vocabularies/core#url |
 | title | string | Title of the item | http://a.ml/vocabularies/core#title |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |

## CustomDomainProperty
Definition of an extension to the domain model defined directly by a user in the RAML/OpenAPI document.
This can be achieved by using an annotationType in RAML. In OpenAPI thy don't need to
      be declared, they can just be used.
      This should be mapped to new RDF properties declared directly in the main document or module.
      Contrast this extension mechanism with the creation of a propertyTerm in a vocabulary, a more
re-usable and generic way of achieving the same functionality.
It can be validated using a SHACL shape

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | domain | [url] | RDFS domain property | http://www.w3.org/2000/01/rdf-schema#domain |
 | schema | [Shape](#shape) | Schema for an entity | http://a.ml/vocabularies/shapes#schema |
 | name | string | Name for an entity | http://a.ml/vocabularies/core#name |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## DataNode
Base class for all data nodes parsed from the data structure

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## DataTypeFragment
Fragment encoding a RAML data type

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DatatypePropertyTerm


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | displayName | string | Human readable name for the property term | http://a.ml/vocabularies/core#displayName |
 | description | string | Human readable description of the property term | http://a.ml/vocabularies/core#description |
 | range | url | Range of the proeprty term, scalar or object | http://www.w3.org/2000/01/rdf-schema#range |
 | subPropertyOf | [url] | Subsumption relationship for terms | http://www.w3.org/2000/01/rdf-schema#subPropertyOf |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Dialect
Definition of an AML dialect, mapping AST nodes from dialect documents into an output semantic graph

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the dialect | http://a.ml/vocabularies/core#name |
 | version | string | Version of the dialect | http://a.ml/vocabularies/core#version |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | documents | [Documents](#documents) | Document mapping for the the dialect | http://a.ml/vocabularies/meta#documents |
 | location | string | Location of the metadata document that generated this base unit | http://a.ml/vocabularies/document#location |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectFragment
AML dialect mapping fragment that can be included in multiple AML dialects

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | location | string | Location of the metadata document that generated this base unit | http://a.ml/vocabularies/document#location |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectInstance


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | definedBy | url |  | http://a.ml/vocabularies/meta#definedBy |
 | graphDependencies | [url] |  | http://a.ml/vocabularies/document#graphDependencies |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectInstanceFragment


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | definedBy | url |  | http://a.ml/vocabularies/meta#definedBy |
 | fragment | string |  | http://a.ml/vocabularies/meta#fragment |
 | graphDependencies | [url] |  | http://a.ml/vocabularies/document#graphDependencies |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectInstanceLibrary


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | definedBy | url |  | http://a.ml/vocabularies/meta#definedBy |
 | graphDependencies | [url] |  | http://a.ml/vocabularies/document#graphDependencies |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectInstancePatch


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | definedBy | url |  | http://a.ml/vocabularies/meta#definedBy |
 | graphDependencies | [url] |  | http://a.ml/vocabularies/document#graphDependencies |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | extends | url | Target base unit being extended by this extension model | http://a.ml/vocabularies/document#extends |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DialectLibrary
Library of AML mappings that can be reused in different AML dialects

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | location | string | Location of the metadata document that generated this base unit | http://a.ml/vocabularies/document#location |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Document
A Document is a parsing Unit that encodes a stand-alone DomainElement and can include references to other DomainElements that reference from the encoded DomainElement.
Since it encodes a DomainElement, but also declares references, it behaves like a Fragment and a Module at the same time.
The main difference is that the Document encoded DomainElement is stand-alone and that the references declared are supposed to be private not for re-use from other Units

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## DocumentMapping
Mapping for a particular dialect document into a graph base unit

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the document for a dialect base unit | http://a.ml/vocabularies/core#name |
 | encodedNode | url | Node in the dialect encoded in the target mapped base unit | http://a.ml/vocabularies/meta#encodedNode |
 | declaredNode | [[PublicNodeMapping](#publicnodemapping)] | Node in the dialect declared in the target mappend base unit | http://a.ml/vocabularies/meta#declaredNode |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## DocumentationItemFragment
Fragment encoding a RAML documentation item

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Documents
Mapping from different type of dialect documents to base units in the parsed graph

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | rootDocument | [DocumentMapping](#documentmapping) | Root node encoded in a mapped document base unit | http://a.ml/vocabularies/meta#rootDocument |
 | fragments | [[DocumentMapping](#documentmapping)] | Mapping of fragment base unit for a particular dialect | http://a.ml/vocabularies/meta#fragments |
 | library | [DocumentMapping](#documentmapping) | Mappig of module base unit for a particular dialect | http://a.ml/vocabularies/meta#library |
 | selfEncoded | boolean | Information about if the base unit URL should be the same as the URI of the parsed root nodes in the unit | http://a.ml/vocabularies/meta#selfEncoded |
 | declarationsPath | string | Information about the AST location of the declarations to be parsed as declared domain elements | http://a.ml/vocabularies/meta#declarationsPath |
 | keyProperty | boolean | Information about whether the dialect is defined by the header or a key property | http://a.ml/vocabularies/meta#keyProperty |
 | referenceStyle | string | Determines the style for inclusions (RamlStyle or JsonSchemaStyle) | http://a.ml/vocabularies/meta#referenceStyle |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## DomainElement
Base class for any element describing a domain model. Domain Elements are encoded or declared into base units

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## DomainExtension
Extension to the model being parsed from RAML annotation or OpenAPI extensions
They must be a DomainPropertySchema (only in RAML) defining them.
The DomainPropertySchema might have an associated Data Shape that must validate the extension nested graph.
They are parsed as RDF graphs using a default transformation from a set of nested records into RDF.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | extensionName | string | Name of an extension entity | http://a.ml/vocabularies/core#extensionName |
 | definedBy | [CustomDomainProperty](#customdomainproperty) | Definition for the extended entity | http://a.ml/vocabularies/document#definedBy |
 | extension | [DataNode](#datanode) | Data structure associated to the extension | http://a.ml/vocabularies/document#extension |
 | element | string | Element being extended | http://a.ml/vocabularies/document#element |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## EmptyBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |

## Encoding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | propertyName | string |  | http://a.ml/vocabularies/apiContract#propertyName |
 | contentType | string |  | http://a.ml/vocabularies/apiContract#contentType |
 | header | [[Parameter](#parameter)] |  | http://a.ml/vocabularies/apiContract#header |
 | style | string | Describes how a specific property value will be serialized depending on its type. | http://a.ml/vocabularies/apiContract#style |
 | explode | boolean |  | http://a.ml/vocabularies/apiContract#explode |
 | allowReserved | boolean |  | http://a.ml/vocabularies/apiContract#allowReserved |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## EndPoint
EndPoint in the API holding a number of executable operations

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | path | string | Path template for an endpoint | http://a.ml/vocabularies/apiContract#path |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | summary | string | Human readable short description of the endpoint | http://a.ml/vocabularies/core#summary |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | supportedOperation | [[Operation](#operation)] | Operations supported by an endpoint | http://a.ml/vocabularies/apiContract#supportedOperation |
 | parameter | [[Parameter](#parameter)] | Additional data required or returned by an operation | http://a.ml/vocabularies/apiContract#parameter |
 | payload | [[Payload](#payload)] | Main payload data required or returned by an operation | http://a.ml/vocabularies/apiContract#payload |
 | server | [[Server](#server)] | Specific information about the server where the endpoint is accessible | http://a.ml/vocabularies/apiContract#server |
 | security | [[SecurityRequirement](#securityrequirement)] | Textual indication of the kind of security scheme used | http://a.ml/vocabularies/security#security |
 | binding | [ChannelBindings](#channelbindings) | Bindings for this endpoint | http://a.ml/vocabularies/apiBinding#binding |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Example
Example value for a schema inside an API

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | guiSummary | string | Human readable description of the example | http://a.ml/vocabularies/apiContract#guiSummary |
 | description | string |  | http://a.ml/vocabularies/core#description |
 | externalValue | string | Raw text containing an unparsable example | http://a.ml/vocabularies/document#externalValue |
 | strict | boolean | Indicates if this example should be validated against an associated schema | http://a.ml/vocabularies/document#strict |
 | mediaType | string | Media type associated to the example | http://a.ml/vocabularies/core#mediaType |
 | structuredValue | [DataNode](#datanode) | Data structure containing the value of the example | http://a.ml/vocabularies/document#structuredValue |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |

## Extension
API spec information designed to be applied and compelement the information of a base specification. RAML extensions and overlays are examples of extensions.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | extends | url | Target base unit being extended by this extension model | http://a.ml/vocabularies/document#extends |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## ExtensionLike
A Document that extends a target document, overwriting part of the information or overlaying additional information.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | extends | url | Target base unit being extended by this extension model | http://a.ml/vocabularies/document#extends |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## External


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | displayName | string | The display name of the item | http://a.ml/vocabularies/core#displayName |
 | base | string | Base URI for the external model | http://a.ml/vocabularies/meta#base |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ExternalContextFields


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |

## ExternalDomainElement
Domain element containing foreign information that cannot be included into the model semantics

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | mediaType | string | Media type associated to the encoded fragment information | http://a.ml/vocabularies/core#mediaType |

## ExternalFragment
Fragment encoding an external entity

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## ExternalSourceElement
Inlined fragment of information

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |

## FileShape
Shape describing data uploaded in an API request

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | fileType | [string] | Type of file described by this shape | http://a.ml/vocabularies/shapes#fileType |
 | pattern | string | Pattern constraint | http://www.w3.org/ns/shacl#pattern |
 | minLength | int | Minimum lenght constraint | http://www.w3.org/ns/shacl#minLength |
 | maxLength | int | Maximum length constraint | http://www.w3.org/ns/shacl#maxLength |
 | minInclusive | double | Minimum inclusive constraint | http://www.w3.org/ns/shacl#minInclusive |
 | maxInclusive | double | Maximum inclusive constraint | http://www.w3.org/ns/shacl#maxInclusive |
 | minExclusive | boolean | Minimum exclusive constraint | http://www.w3.org/ns/shacl#minExclusive |
 | maxExclusive | boolean | Maximum exclusive constraint | http://www.w3.org/ns/shacl#maxExclusive |
 | format | string | Format constraint | http://a.ml/vocabularies/shapes#format |
 | multipleOf | double | Multiple of constraint | http://a.ml/vocabularies/shapes#multipleOf |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Fragment
A Fragment is a parsing Unit that encodes a DomainElement

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## HttpApiKeySettings
Settings for an Http API Key security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string |  | http://a.ml/vocabularies/core#name |
 | in | string |  | http://a.ml/vocabularies/security#in |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## HttpMessageBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | headers | [Shape](#shape) | A Schema object containing the definitions for HTTP-specific headers | http://a.ml/vocabularies/apiBinding#headers |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## HttpOperationBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | operationType | string | Type of operation | http://a.ml/vocabularies/apiBinding#operationType |
 | method | string | Operation binding method | http://a.ml/vocabularies/apiBinding#method |
 | query | [Shape](#shape) | A Schema object containing the definitions for each query parameter | http://a.ml/vocabularies/apiBinding#query |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## HttpSettings
Settings for an HTTP security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | scheme | string |  | http://a.ml/vocabularies/security#scheme |
 | bearerFormat | string |  | http://a.ml/vocabularies/security#bearerFormat |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## IriTemplateMapping


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | templateVariable | string | Variable defined inside an URL template | http://a.ml/vocabularies/apiContract#templateVariable |
 | linkExpression | string | OAS 3 link expression | http://a.ml/vocabularies/apiContract#linkExpression |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## KafkaMessageBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | messageKey | [Shape](#shape) | Schema that defines the message key | http://a.ml/vocabularies/apiBinding#messageKey |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## KafkaOperationBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | groupId | [Shape](#shape) | Schema that defines the id of the consumer group | http://a.ml/vocabularies/apiBinding#groupId |
 | clientId | [Shape](#shape) | Schema that defines the id of the consumer inside a consumer group | http://a.ml/vocabularies/apiBinding#clientId |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## License
Licensing information for a resource

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | url | url | URL identifying the organization | http://a.ml/vocabularies/core#url |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## LinkNode
Node that represents a dynamic link in a data structure

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | value | string |  | http://a.ml/vocabularies/data#value |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## LinkableElement
Reification of a link between elements in the model. Used when we want to capture the structure of the source document
in the graph itself. Linkable elements are just replaced by regular links after resolution.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |

## MatrixShape
Data shape containing nested multi-dimensional collection shapes

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | items | [Shape](#shape) | Shapes inside the data arrangement | http://a.ml/vocabularies/shapes#items |
 | contains | [Shape](#shape) | One of the shapes in the data arrangement | http://a.ml/vocabularies/shapes#contains |
 | minCount | int | Minimum items count constraint | http://www.w3.org/ns/shacl#minCount |
 | maxCount | int | Maximum items count constraint | http://www.w3.org/ns/shacl#maxCount |
 | uniqueItems | boolean | Unique items constraint | http://a.ml/vocabularies/shapes#uniqueItems |
 | collectionFormat | string | Input collection format information | http://a.ml/vocabularies/shapes#collectionFormat |
 | unevaluatedItems | boolean | Accepts that items may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedItems |
 | unevaluatedItemsSchema | [Shape](#shape) | Items that may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedItemsSchema |
 | qualifiedMinCount | int | Minimum number of value nodes constraint | http://www.w3.org/ns/shacl#qualifiedMinCount |
 | qualifiedMaxCount | int | Maximum number of value nodes constraint | http://www.w3.org/ns/shacl#qualifiedMaxCount |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Message


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | payload | [[Payload](#payload)] | Payload for a Request/Response | http://a.ml/vocabularies/apiContract#payload |
 | correlationId | [CorrelationId](#correlationid) | An identifier that can be used for message tracing and correlation | http://a.ml/vocabularies/core#correlationId |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | title | string | Title of the item | http://a.ml/vocabularies/core#title |
 | summary | string | Human readable short description of the request/response | http://a.ml/vocabularies/core#summary |
 | header | [[Parameter](#parameter)] | Parameter passed as a header to an operation for communication models | http://a.ml/vocabularies/apiContract#header |
 | binding | [MessageBindings](#messagebindings) | Bindings for this request/response | http://a.ml/vocabularies/apiBinding#binding |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | isAbstract | boolean | Defines a model as abstract | http://a.ml/vocabularies/apiContract#isAbstract |
 | headerExamples | [[Example](#example)] | Examples for a header definition | http://a.ml/vocabularies/apiContract#headerExamples |
 | headerSchema | [NodeShape](#nodeshape) | Object Schema who's properties are headers for the message. | http://a.ml/vocabularies/apiContract#headerSchema |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## MessageBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## MessageBindings


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | bindings | [[MessageBinding](#messagebinding)] | List of message bindings | http://a.ml/vocabularies/apiBinding#bindings |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Module
A Module is a parsing Unit that declares DomainElements that can be referenced from the DomainElements in other parsing Units.
It main purpose is to expose the declared references so they can be re-used

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## MqttMessageBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## MqttOperationBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | qos | int | Defines how hard the broker/client will try to ensure that a message is received | http://a.ml/vocabularies/apiBinding#qos |
 | retain | boolean | Whether the broker should retain the message or not | http://a.ml/vocabularies/apiBinding#retain |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## MqttServerBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | clientId | string | The client identifier | http://a.ml/vocabularies/apiBinding#clientId |
 | cleanSession | boolean | Whether to create a persistent connection or not | http://a.ml/vocabularies/apiBinding#cleanSession |
 | lastWill | [MqttServerLastWill](#mqttserverlastwill) | Last Will and Testament configuration | http://a.ml/vocabularies/apiBinding#lastWill |
 | keepAlive | int | Interval in seconds of the longest period of time the broker and the client can endure without sending a message | http://a.ml/vocabularies/apiBinding#keepAlive |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## MqttServerLastWill


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | topic | string | The topic where the Last Will and Testament message will be sent | http://a.ml/vocabularies/apiBinding#topic |
 | qos | int | Defines how hard the broker/client will try to ensure that the Last Will and Testament message is received | http://a.ml/vocabularies/apiBinding#qos |
 | retain | boolean | Whether the broker should retain the Last Will and Testament message or not | http://a.ml/vocabularies/apiBinding#retain |
 | message | string | Message used to notify other clients about an ungracefully disconnected client. | http://a.ml/vocabularies/apiBinding#message |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## NamedExampleFragment
Fragment encoding a RAML named example

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## NilShape
Data shape representing the null/nil value in the input schema

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |

## NodeMapping


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | targetClass | url | Target class whose instances will need to match the constraint described for the node | http://www.w3.org/ns/shacl#targetClass |
 | name | string | Name of the node mappable element | http://a.ml/vocabularies/core#name |
 | property | [[PropertyMapping](#propertymapping)] | Data shape constraint for a property of the target node | http://www.w3.org/ns/shacl#property |
 | uriTemplate | string | URI template that will be used to generate the URI of the parsed nodeds in the graph | http://a.ml/vocabularies/apiContract#uriTemplate |
 | mergePolicy | string | Indication of how to merge this graph node when applying a patch document | http://a.ml/vocabularies/meta#mergePolicy |
 | resolvedExtends | [url] |  | http://a.ml/vocabularies/meta#resolvedExtends |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## NodeShape
Shape that validates a record of fields, like a JS object

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | minProperties | int | Minimum number of properties in the input node constraint | http://a.ml/vocabularies/shapes#minProperties |
 | maxProperties | int | Maximum number of properties in the input node constraint | http://a.ml/vocabularies/shapes#maxProperties |
 | closed | boolean | Additional properties in the input node accepted constraint | http://www.w3.org/ns/shacl#closed |
 | additionalPropertiesSchema | [Shape](#shape) | Additional properties schema | http://www.w3.org/ns/shacl#additionalPropertiesSchema |
 | discriminator | string | Discriminator property | http://a.ml/vocabularies/shapes#discriminator |
 | discriminatorValue | string | Values for the discriminator property | http://a.ml/vocabularies/shapes#discriminatorValue |
 | discriminatorMapping | [[IriTemplateMapping](#iritemplatemapping)] | Mappping of acceptable values for the node discriminator | http://a.ml/vocabularies/shapes#discriminatorMapping |
 | property | [[PropertyShape](#propertyshape)] | Properties associated to this node | http://www.w3.org/ns/shacl#property |
 | propertyNames | [Shape](#shape) | Property names schema | http://www.w3.org/ns/shacl#propertyNames |
 | dependencies | [[PropertyDependencies](#propertydependencies)] | Dependent properties constraint | http://a.ml/vocabularies/shapes#dependencies |
 | schemaDependencies | [[SchemaDependencies](#schemadependencies)] | Applied schemas if property exists constraint | http://a.ml/vocabularies/shapes#schemaDependencies |
 | unevaluatedProperties | boolean | Accepts that properties may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedProperties |
 | unevaluatedPropertiesSchema | [Shape](#shape) | Properties that may not be evaluated in schema validation | http://a.ml/vocabularies/shapes#unevaluatedPropertiesSchema |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OAuth1Settings
Settings for an OAuth1 security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | requestTokenUri | string |  | http://a.ml/vocabularies/security#requestTokenUri |
 | authorizationUri | string |  | http://a.ml/vocabularies/security#authorizationUri |
 | tokenCredentialsUri | string |  | http://a.ml/vocabularies/security#tokenCredentialsUri |
 | signature | [string] |  | http://a.ml/vocabularies/security#signature |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OAuth2Flow
Flow for an OAuth2 security scheme setting

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | authorizationUri | string |  | http://a.ml/vocabularies/security#authorizationUri |
 | accessTokenUri | string |  | http://a.ml/vocabularies/security#accessTokenUri |
 | flow | string |  | http://a.ml/vocabularies/security#flow |
 | refreshUri | string |  | http://a.ml/vocabularies/security#refreshUri |
 | scope | [[Scope](#scope)] |  | http://a.ml/vocabularies/security#scope |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OAuth2Settings
Settings for an OAuth2 security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | authorizationGrant | [string] |  | http://a.ml/vocabularies/security#authorizationGrant |
 | flows | [[OAuth2Flow](#oauth2flow)] |  | http://a.ml/vocabularies/security#flows |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ObjType


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |

## ObjectNode
Node that represents a dynamic object with records data structure

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ObjectPropertyTerm


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | displayName | string | Human readable name for the property term | http://a.ml/vocabularies/core#displayName |
 | description | string | Human readable description of the property term | http://a.ml/vocabularies/core#description |
 | range | url | Range of the proeprty term, scalar or object | http://www.w3.org/2000/01/rdf-schema#range |
 | subPropertyOf | [url] | Subsumption relationship for terms | http://www.w3.org/2000/01/rdf-schema#subPropertyOf |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OpenIdConnectSettings
Settings for an OpenID security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | openIdConnectUrl | string |  | http://a.ml/vocabularies/security#openIdConnectUrl |
 | scope | [[Scope](#scope)] |  | http://a.ml/vocabularies/security#scope |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Operation
Action that can be executed using a particular HTTP invocation

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | method | string | HTTP method required to invoke the operation | http://a.ml/vocabularies/apiContract#method |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | deprecated | boolean | Marks the operation as deprecated | http://a.ml/vocabularies/core#deprecated |
 | guiSummary | string | Human readable description of the operation | http://a.ml/vocabularies/apiContract#guiSummary |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | scheme | [string] | URI scheme for the API protocol | http://a.ml/vocabularies/apiContract#scheme |
 | accepts | [string] | Media-types accepted in a API request | http://a.ml/vocabularies/apiContract#accepts |
 | mediaType | [string] | Media types returned by a API response | http://a.ml/vocabularies/core#mediaType |
 | expects | [[Request](#request)] | Request information required by the operation | http://a.ml/vocabularies/apiContract#expects |
 | returns | [[Response](#response)] | Response data returned by the operation | http://a.ml/vocabularies/apiContract#returns |
 | security | [[SecurityRequirement](#securityrequirement)] | Security schemes applied to an element in the API spec | http://a.ml/vocabularies/security#security |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | callback | [[Callback](#callback)] | Associated callbacks | http://a.ml/vocabularies/apiContract#callback |
 | server | [[Server](#server)] | Server information | http://a.ml/vocabularies/apiContract#server |
 | binding | [OperationBindings](#operationbindings) | Bindings for this operation | http://a.ml/vocabularies/apiBinding#binding |
 | isAbstract | boolean | Defines a model as abstract | http://a.ml/vocabularies/apiContract#isAbstract |
 | operationId | string | Identifier of the target operation | http://a.ml/vocabularies/apiContract#operationId |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OperationBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## OperationBindings


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | bindings | [[OperationBinding](#operationbinding)] | List of operation bindings | http://a.ml/vocabularies/apiBinding#bindings |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Organization
Organization providing an good or service

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | url | url | URL identifying the organization | http://a.ml/vocabularies/core#url |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | email | string | Contact email for the organization | http://a.ml/vocabularies/core#email |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Overlay
Model defining a RAML overlay

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | extends | url | Target base unit being extended by this extension model | http://a.ml/vocabularies/document#extends |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Parameter
Piece of data required or returned by an Operation

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | paramName | string | Name of a parameter | http://a.ml/vocabularies/apiContract#paramName |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | required | boolean | Marks the parameter as required | http://a.ml/vocabularies/apiContract#required |
 | deprecated | boolean | Marks the parameter as deprecated | http://a.ml/vocabularies/document#deprecated |
 | allowEmptyValue | boolean | Parameter can be passed without value | http://a.ml/vocabularies/apiContract#allowEmptyValue |
 | style | string | Encoding style for the parameter information | http://a.ml/vocabularies/apiContract#style |
 | explode | boolean |  | http://a.ml/vocabularies/apiContract#explode |
 | allowReserved | boolean |  | http://a.ml/vocabularies/apiContract#allowReserved |
 | binding | string | Part of the Request model where the parameter can be encoded (header, path, query param, etc.) | http://a.ml/vocabularies/apiContract#binding |
 | schema | [Shape](#shape) | Schema the parameter value must validate | http://a.ml/vocabularies/shapes#schema |
 | payload | [[Payload](#payload)] |  | http://a.ml/vocabularies/apiContract#payload |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ParametrizedDeclaration
Generic graph template supporting variables that can be transformed into a domain element

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | target | [AbstractDeclaration](#abstractdeclaration) | Target node for the parameter | http://a.ml/vocabularies/document#target |
 | variable | [[VariableValue](#variablevalue)] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ParametrizedResourceType
RAML resource type that can accept parameters

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | target | [AbstractDeclaration](#abstractdeclaration) | Target node for the parameter | http://a.ml/vocabularies/document#target |
 | variable | [[VariableValue](#variablevalue)] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ParametrizedSecurityScheme


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name for the security scheme | http://a.ml/vocabularies/core#name |
 | scheme | [SecurityScheme](#securityscheme) |  | http://a.ml/vocabularies/security#scheme |
 | settings | [Settings](#settings) | Security scheme settings | http://a.ml/vocabularies/security#settings |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ParametrizedTrait
RAML trait with declared parameters

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | target | [AbstractDeclaration](#abstractdeclaration) | Target node for the parameter | http://a.ml/vocabularies/document#target |
 | variable | [[VariableValue](#variablevalue)] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Payload
Encoded payload using certain media-type

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | mediaType | string | Media types supported in the payload | http://a.ml/vocabularies/core#mediaType |
 | schemaMediaType | string | Defines the format of the payload schema | http://a.ml/vocabularies/apiContract#schemaMediaType |
 | schema | [Shape](#shape) | Schema associated to this payload | http://a.ml/vocabularies/shapes#schema |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | encoding | [[Encoding](#encoding)] | An array of properties and its encoding information. The key, being the property name, must exist in the schema as a property | http://a.ml/vocabularies/apiContract#encoding |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |

## PayloadFragment
Fragment encoding HTTP payload information

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | mediaType | string | HTTP Media type associated to the encoded fragment information | http://a.ml/vocabularies/core#mediaType |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## PropertyDependencies
Dependency between sets of property shapes

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | propertySource | string | Source property name in the dependency | http://a.ml/vocabularies/shapes#propertySource |
 | propertyTarget | [string] | Target property name in the dependency | http://a.ml/vocabularies/shapes#propertyTarget |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## PropertyMapping
Semantic mapping from an input AST in a dialect document to the output graph of information for a class of output node

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | path | url | URI in the mapped graph for this mapped property | http://www.w3.org/ns/shacl#path |
 | name | string | Name in the source AST for the mapped property | http://a.ml/vocabularies/core#name |
 | datatype | url | Scalar constraint over the type of the mapped property | http://www.w3.org/ns/shacl#datatype |
 | node | [url] | Object constraint over the type of the mapped property | http://www.w3.org/ns/shacl#node |
 | mapProperty | string | Marks the mapping as a 'map' mapping syntax. Directly related with mapTermKeyProperty | http://a.ml/vocabularies/meta#mapProperty |
 | mapValueProperty | string | Marks the mapping as a 'map value' mapping syntax. Directly related with mapTermValueProperty | http://a.ml/vocabularies/meta#mapValueProperty |
 | mapTermProperty | url | Marks the mapping as a 'map' mapping syntax.  | http://a.ml/vocabularies/meta#mapTermProperty |
 | mapTermValueProperty | url | Marks the mapping as a 'map value' mapping syntax | http://a.ml/vocabularies/meta#mapTermValueProperty |
 | minCount | int | Minimum count constraint over the mapped property | http://www.w3.org/ns/shacl#minCount |
 | pattern | string | Pattern constraint over the mapped property | http://www.w3.org/ns/shacl#pattern |
 | minInclusive | double | Minimum inclusive constraint over the mapped property | http://www.w3.org/ns/shacl#minInclusive |
 | maxInclusive | double | Maximum inclusive constraint over the mapped property | http://www.w3.org/ns/shacl#maxInclusive |
 | allowMultiple | boolean | Allows multiple mapped nodes for the property mapping | http://a.ml/vocabularies/meta#allowMultiple |
 | sorted | boolean | Marks the mapping as requiring order in the mapped collection of nodes | http://a.ml/vocabularies/meta#sorted |
 | in | [Any] | Enum constraint for the values of the property mapping | http://www.w3.org/ns/shacl#in |
 | typeDiscriminatorMap | string | Information about the discriminator values in the source AST for the property mapping | http://a.ml/vocabularies/meta#typeDiscriminatorMap |
 | unique | boolean | Marks the values for the property mapping as a primary key for this type of node | http://a.ml/vocabularies/meta#unique |
 | externallyLinkable | boolean | Marks this object property as supporting external links | http://a.ml/vocabularies/meta#externallyLinkable |
 | typeDiscriminatorName | string | Information about the field in the source AST to be used as discrimintaro in the property mapping | http://a.ml/vocabularies/meta#typeDiscriminatorName |
 | mergePolicy | string | Indication of how to merge this graph node when applying a patch document | http://a.ml/vocabularies/meta#mergePolicy |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## PropertyShape
Constraint over a property in a data shape.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | path | url | Path to the constrained property | http://www.w3.org/ns/shacl#path |
 | range | [Shape](#shape) | Range property constraint | http://a.ml/vocabularies/shapes#range |
 | minCount | int | Minimum count property constraint | http://www.w3.org/ns/shacl#minCount |
 | maxCount | int | Maximum count property constraint | http://www.w3.org/ns/shacl#maxCount |
 | patternName | string | Patterned property constraint | http://a.ml/vocabularies/shapes#patternName |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## PublicNodeMapping
Mapping for a graph node mapping to a particular function in a dialect

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the mapping | http://a.ml/vocabularies/core#name |
 | mappedNode | url | Node in the dialect definition associated to this mapping | http://a.ml/vocabularies/meta#mappedNode |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## RecursiveShape
Recursion on a Shape structure, used when expanding a shape and finding the canonical representation of that shape.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | fixPoint | url | Link to the base of the recursion for a recursive shape | http://a.ml/vocabularies/shapes#fixPoint |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Request
Request information for an operation

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | required | boolean | Marks the parameter as required | http://a.ml/vocabularies/apiContract#required |
 | parameter | [[Parameter](#parameter)] | Parameters associated to the communication model | http://a.ml/vocabularies/apiContract#parameter |
 | queryString | [Shape](#shape) | Query string for the communication model | http://a.ml/vocabularies/apiContract#queryString |
 | uriParameter | [[Parameter](#parameter)] |  | http://a.ml/vocabularies/apiContract#uriParameter |
 | cookieParameter | [[Parameter](#parameter)] |  | http://a.ml/vocabularies/apiContract#cookieParameter |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | payload | [[Payload](#payload)] | Payload for a Request/Response | http://a.ml/vocabularies/apiContract#payload |
 | correlationId | [CorrelationId](#correlationid) | An identifier that can be used for message tracing and correlation | http://a.ml/vocabularies/core#correlationId |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | title | string | Title of the item | http://a.ml/vocabularies/core#title |
 | summary | string | Human readable short description of the request/response | http://a.ml/vocabularies/core#summary |
 | header | [[Parameter](#parameter)] | Parameter passed as a header to an operation for communication models | http://a.ml/vocabularies/apiContract#header |
 | binding | [MessageBindings](#messagebindings) | Bindings for this request/response | http://a.ml/vocabularies/apiBinding#binding |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | isAbstract | boolean | Defines a model as abstract | http://a.ml/vocabularies/apiContract#isAbstract |
 | headerExamples | [[Example](#example)] | Examples for a header definition | http://a.ml/vocabularies/apiContract#headerExamples |
 | headerSchema | [NodeShape](#nodeshape) | Object Schema who's properties are headers for the message. | http://a.ml/vocabularies/apiContract#headerSchema |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ResourceType
Type of document base unit encoding a RAML resource type

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | dataNode | [DataNode](#datanode) | Associated dynamic structure for the declaration | http://a.ml/vocabularies/document#dataNode |
 | variable | [string] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ResourceTypeFragment
Fragment encoding a RAML resource type

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Response
Response information for an operation

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | statusCode | string | HTTP status code returned by a response | http://a.ml/vocabularies/apiContract#statusCode |
 | link | [[TemplatedLink](#templatedlink)] | Structural definition of links on the source data shape AST | http://a.ml/vocabularies/apiContract#link |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | payload | [[Payload](#payload)] | Payload for a Request/Response | http://a.ml/vocabularies/apiContract#payload |
 | correlationId | [CorrelationId](#correlationid) | An identifier that can be used for message tracing and correlation | http://a.ml/vocabularies/core#correlationId |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | title | string | Title of the item | http://a.ml/vocabularies/core#title |
 | summary | string | Human readable short description of the request/response | http://a.ml/vocabularies/core#summary |
 | header | [[Parameter](#parameter)] | Parameter passed as a header to an operation for communication models | http://a.ml/vocabularies/apiContract#header |
 | binding | [MessageBindings](#messagebindings) | Bindings for this request/response | http://a.ml/vocabularies/apiBinding#binding |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | isAbstract | boolean | Defines a model as abstract | http://a.ml/vocabularies/apiContract#isAbstract |
 | headerExamples | [[Example](#example)] | Examples for a header definition | http://a.ml/vocabularies/apiContract#headerExamples |
 | headerSchema | [NodeShape](#nodeshape) | Object Schema who's properties are headers for the message. | http://a.ml/vocabularies/apiContract#headerSchema |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ScalarNode
Node that represents a dynamic scalar value data structure

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | value | string | value for an scalar dynamic node | http://a.ml/vocabularies/data#value |
 | datatype | url | Data type of value for an scalar dynamic node | http://www.w3.org/ns/shacl#datatype |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ScalarShape
Data shape describing a scalar value in the input data model, reified as an scalar node in the mapped graph

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | datatype | url | Scalar range constraining this scalar shape | http://www.w3.org/ns/shacl#datatype |
 | encoding | string | Describes the contents' value encoding | http://a.ml/vocabularies/shapes#encoding |
 | mediaType | string | Describes the content's value mediatype | http://a.ml/vocabularies/shapes#mediaType |
 | contentSchema | [Shape](#shape) | Describes the content's value structure | http://a.ml/vocabularies/shapes#contentSchema |
 | pattern | string | Pattern constraint | http://www.w3.org/ns/shacl#pattern |
 | minLength | int | Minimum lenght constraint | http://www.w3.org/ns/shacl#minLength |
 | maxLength | int | Maximum length constraint | http://www.w3.org/ns/shacl#maxLength |
 | minInclusive | double | Minimum inclusive constraint | http://www.w3.org/ns/shacl#minInclusive |
 | maxInclusive | double | Maximum inclusive constraint | http://www.w3.org/ns/shacl#maxInclusive |
 | minExclusive | boolean | Minimum exclusive constraint | http://www.w3.org/ns/shacl#minExclusive |
 | maxExclusive | boolean | Maximum exclusive constraint | http://www.w3.org/ns/shacl#maxExclusive |
 | format | string | Format constraint | http://a.ml/vocabularies/shapes#format |
 | multipleOf | double | Multiple of constraint | http://a.ml/vocabularies/shapes#multipleOf |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SchemaDependencies
Dependency between a property shape and a schema

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | propertySource | string | Source property name in the dependency | http://a.ml/vocabularies/shapes#propertySource |
 | schemaTarget | [Shape](#shape) | Target applied shape in the dependency | http://a.ml/vocabularies/shapes#schemaTarget |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SchemaShape
Raw schema that cannot be parsed using AMF shapes model

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | mediaType | string | Media type associated to a shape | http://a.ml/vocabularies/core#mediaType |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Scope


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the scope | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description for the scope | http://a.ml/vocabularies/core#description |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SecurityRequirement
Flow for an OAuth2 security scheme setting

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | schemes | [[ParametrizedSecurityScheme](#parametrizedsecurityscheme)] |  | http://a.ml/vocabularies/security#schemes |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SecurityScheme
Authentication and access control mechanism defined in an API

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name for the security scheme | http://a.ml/vocabularies/core#name |
 | type | string | Type of security scheme | http://a.ml/vocabularies/security#type |
 | displayName | string | Human readable name for the term | http://a.ml/vocabularies/core#displayName |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | header | [[Parameter](#parameter)] | Parameter passed as a header to an operation for communication models | http://a.ml/vocabularies/apiContract#header |
 | parameter | [[Parameter](#parameter)] | Parameters associated to the communication model | http://a.ml/vocabularies/apiContract#parameter |
 | response | [[Response](#response)] | Response associated to this security scheme | http://a.ml/vocabularies/apiContract#response |
 | settings | [Settings](#settings) | Security scheme settings | http://a.ml/vocabularies/security#settings |
 | queryString | [Shape](#shape) | Query string for the communication model | http://a.ml/vocabularies/apiContract#queryString |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SecuritySchemeFragment
Fragment encoding a RAML security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## Server
Information about the network accessible locations where the API is available

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | urlTemplate | string | URL (potentially a template) for the server | http://a.ml/vocabularies/core#urlTemplate |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | variable | [[Parameter](#parameter)] | Variables in the URL for the server | http://a.ml/vocabularies/apiContract#variable |
 | protocol | string | The protocol this URL supports for connection | http://a.ml/vocabularies/apiContract#protocol |
 | protocolVersion | string | The version of the protocol used for connection | http://a.ml/vocabularies/apiContract#protocolVersion |
 | security | [[SecurityRequirement](#securityrequirement)] | Textual indication of the kind of security scheme used | http://a.ml/vocabularies/security#security |
 | binding | [ServerBindings](#serverbindings) | Bindings for this server | http://a.ml/vocabularies/apiBinding#binding |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ServerBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## ServerBindings


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | bindings | [[ServerBinding](#serverbinding)] | List of server bindings | http://a.ml/vocabularies/apiBinding#bindings |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Settings
Settings for a security scheme

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | additionalProperties | [DataNode](#datanode) |  | http://a.ml/vocabularies/security#additionalProperties |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Shape
Base class for all shapes. Shapes are Domain Entities that define constraints over parts of a data graph.
They can be used to define and enforce schemas for the data graph information through SHACL.
Shapes can be recursive and inherit from other shapes.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |

## ShapeExtension
Custom extensions for a data shape definition inside an API definition

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | definedBy | [CustomDomainProperty](#customdomainproperty) | Definition for the extended entity | http://a.ml/vocabularies/document#definedBy |
 | extension | [DataNode](#datanode) | Data structure associated to the extension | http://a.ml/vocabularies/document#extension |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## SourceMap
SourceMaps include tags with syntax specific information obtained when parsing a particular specification syntax like RAML or OpenAPI.
It can be used to re-generate the document from the RDF model with a similar syntax

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |

## Tag
Categorical information provided by some API spec format. Tags are extensions to the model supported directly in the input API spec format.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | documentation | [CreativeWork](#creativework) | Documentation about the tag | http://a.ml/vocabularies/core#documentation |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## TemplatedLink
Templated link containing URL template and variables mapping

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | template | string | URL template for a templated link | http://a.ml/vocabularies/apiContract#template |
 | operationId | string | Identifier of the target operation | http://a.ml/vocabularies/apiContract#operationId |
 | mapping | [[IriTemplateMapping](#iritemplatemapping)] | Variable mapping for the URL template | http://a.ml/vocabularies/apiContract#mapping |
 | requestBody | string |  | http://a.ml/vocabularies/apiContract#requestBody |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | server | [Server](#server) |  | http://a.ml/vocabularies/apiContract#server |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |

## Trait
Type of document base unit encoding a RAML trait

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | dataNode | [DataNode](#datanode) | Associated dynamic structure for the declaration | http://a.ml/vocabularies/document#dataNode |
 | variable | [string] | Variables to be replaced in the graph template introduced by an AbstractDeclaration | http://a.ml/vocabularies/document#variable |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## TraitFragment
Fragment encoding a RAML trait

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | encodes | [DomainElement](#domainelement) | The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains. | http://a.ml/vocabularies/document#encodes |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## TupleShape
Data shape containing a multi-valued collection of shapes

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | items | [[Shape](#shape)] | Shapes contained in the Tuple Shape | http://a.ml/vocabularies/shapes#items |
 | minCount | int | Minimum items count constraint | http://www.w3.org/ns/shacl#minCount |
 | maxCount | int | Maximum items count constraint | http://www.w3.org/ns/shacl#maxCount |
 | uniqueItems | boolean | Unique items constraint | http://a.ml/vocabularies/shapes#uniqueItems |
 | closedItems | boolean | Constraint limiting additional shapes in the collection | http://a.ml/vocabularies/shapes#closedItems |
 | additionalItemsSchema | [Shape](#shape) | Controls whether it’s valid to have additional items in the array beyond what is defined | http://a.ml/vocabularies/shapes#additionalItemsSchema |
 | collectionFormat | string | Input collection format information | http://a.ml/vocabularies/shapes#collectionFormat |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## UnionNodeMapping


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the node mappable element | http://a.ml/vocabularies/core#name |
 | typeDiscriminatorMap | string | Information about the discriminator values in the source AST for the property mapping | http://a.ml/vocabularies/meta#typeDiscriminatorMap |
 | typeDiscriminatorName | string | Information about the field in the source AST to be used as discrimintaro in the property mapping | http://a.ml/vocabularies/meta#typeDiscriminatorName |
 | node | [url] | Object constraint over the type of the mapped property | http://www.w3.org/ns/shacl#node |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## UnionShape
Shape representing the union of many alternative data shapes

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | anyOf | [[Shape](#shape)] | Data shapes in the union | http://a.ml/vocabularies/shapes#anyOf |
 | link-target | url | URI of the linked element | http://a.ml/vocabularies/document#link-target |
 | link-label | string | Label for the type of link | http://a.ml/vocabularies/document#link-label |
 | recursive | boolean | Indication taht this kind of linkable element can support recursive links | http://a.ml/vocabularies/document#recursive |
 | name | string | Name for a data shape | http://www.w3.org/ns/shacl#name |
 | name | string | Human readable name for the term | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | defaultValue | [DataNode](#datanode) | Default value parsed for a data shape property | http://www.w3.org/ns/shacl#defaultValue |
 | in | [[DataNode](#datanode)] | Enumeration of possible values for a data shape property | http://www.w3.org/ns/shacl#in |
 | inherits | [[Shape](#shape)] | Relationship of inheritance between data shapes | http://a.ml/vocabularies/shapes#inherits |
 | defaultValueStr | string | Textual representation of the parsed default value for the shape property | http://www.w3.org/ns/shacl#defaultValueStr |
 | not | [Shape](#shape) | Logical not composition of data shapes | http://www.w3.org/ns/shacl#not |
 | and | [[Shape](#shape)] | Logical and composition of data shapes | http://www.w3.org/ns/shacl#and |
 | or | [[Shape](#shape)] | Logical or composition of data shapes | http://www.w3.org/ns/shacl#or |
 | xone | [[Shape](#shape)] | Logical exclusive or composition of data shapes | http://www.w3.org/ns/shacl#xone |
 | closure | [url] | Transitive closure of data shapes this particular shape inherits structure from | http://a.ml/vocabularies/shapes#closure |
 | if | [Shape](#shape) | Condition for applying composition of data shapes | http://www.w3.org/ns/shacl#if |
 | then | [Shape](#shape) | Composition of data shape when if data shape is valid | http://www.w3.org/ns/shacl#then |
 | else | [Shape](#shape) | Composition of data shape when if data shape is invalid | http://www.w3.org/ns/shacl#else |
 | readOnly | boolean | Read only property constraint | http://a.ml/vocabularies/shapes#readOnly |
 | writeOnly | boolean | Write only property constraint | http://a.ml/vocabularies/shapes#writeOnly |
 | deprecated | boolean | Deprecated annotation for a property constraint | http://a.ml/vocabularies/shapes#deprecated |
 | documentation | [CreativeWork](#creativework) | Documentation for a particular part of the model | http://a.ml/vocabularies/core#documentation |
 | xmlSerialization | [XMLSerializer](#xmlserializer) | Information about how to serialize | http://a.ml/vocabularies/shapes#xmlSerialization |
 | comment | string | A comment on an item. The comment's content is expressed via the text | http://a.ml/vocabularies/core#comment |
 | examples | [[Example](#example)] | Examples for a particular domain element | http://a.ml/vocabularies/apiContract#examples |
 | raw | string | Raw textual information that cannot be processed for the current model semantics. | http://a.ml/vocabularies/document#raw |
 | reference-id | url | Internal identifier for an inlined fragment | http://a.ml/vocabularies/document#reference-id |
 | location | string | Location of an inlined fragment | http://a.ml/vocabularies/document#location |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## VariableValue
Value for a variable in a graph template

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | name of the template variable | http://a.ml/vocabularies/core#name |
 | value | [DataNode](#datanode) | value of the variables | http://a.ml/vocabularies/document#value |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## Vocabulary
Basic primitives for the declaration of vocabularies.

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name for an entity | http://a.ml/vocabularies/core#name |
 | imports | [[VocabularyReference](#vocabularyreference)] | import relationships between vocabularies | http://www.w3.org/2002/07/owl#imports |
 | externals | [[External](#external)] |  | http://a.ml/vocabularies/meta#externals |
 | declares | [[DomainElement](#domainelement)] | The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units. URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements. | http://a.ml/vocabularies/document#declares |
 | base | string | Base URI prefix for definitions in this vocabulary | http://a.ml/vocabularies/meta#base |
 | location | string | Location of the metadata document that generated this base unit | http://a.ml/vocabularies/document#location |
 | version | string | Version of the current model | http://a.ml/vocabularies/document#version |
 | references | [[BaseUnit](#baseunit)] | references across base units | http://a.ml/vocabularies/document#references |
 | usage | string | Human readable description of the unit | http://a.ml/vocabularies/document#usage |
 | describedBy | url | Link to the AML dialect describing a particular subgraph of information | http://a.ml/vocabularies/meta#describedBy |
 | root | boolean | Indicates if the base unit represents the root of the document model obtained from parsing | http://a.ml/vocabularies/document#root |

## VocabularyReference


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | alias | string |  | http://a.ml/vocabularies/document#alias |
 | reference | string |  | http://a.ml/vocabularies/document#reference |
 | base | string |  | http://a.ml/vocabularies/meta#base |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## WebApi
Top level element describing a HTTP API

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | name | string | Name of the shape | http://a.ml/vocabularies/core#name |
 | description | string | Human readable description of an element | http://a.ml/vocabularies/core#description |
 | identifier | string | The identifier property represents any kind of identifier, such as ISBNs, GTIN codes, UUIDs, etc. | http://a.ml/vocabularies/core#identifier |
 | server | [[Server](#server)] | Server information | http://a.ml/vocabularies/apiContract#server |
 | accepts | [string] | Media-types accepted in a API request | http://a.ml/vocabularies/apiContract#accepts |
 | contentType | [string] | Media types returned by a API response | http://a.ml/vocabularies/apiContract#contentType |
 | scheme | [string] | URI scheme for the API protocol | http://a.ml/vocabularies/apiContract#scheme |
 | version | string | Version of the API | http://a.ml/vocabularies/core#version |
 | termsOfService | string | Terms and conditions when using the API | http://a.ml/vocabularies/core#termsOfService |
 | provider | [Organization](#organization) | Organization providing some kind of asset or service | http://a.ml/vocabularies/core#provider |
 | license | [License](#license) | License for the API | http://a.ml/vocabularies/core#license |
 | documentation | [[CreativeWork](#creativework)] | Documentation associated to the API | http://a.ml/vocabularies/core#documentation |
 | endpoint | [[EndPoint](#endpoint)] | End points defined in the API | http://a.ml/vocabularies/apiContract#endpoint |
 | security | [[SecurityRequirement](#securityrequirement)] | Textual indication of the kind of security scheme used | http://a.ml/vocabularies/security#security |
 | tag | [[Tag](#tag)] | Additionally custom tagged information | http://a.ml/vocabularies/apiContract#tag |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## WebSocketsChannelBinding


 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | method | string | The HTTP method to use when establishing the connection | http://a.ml/vocabularies/apiBinding#method |
 | query | [Shape](#shape) | A Schema object containing the definitions for each query parameter | http://a.ml/vocabularies/apiBinding#query |
 | headers | [Shape](#shape) | A Schema object containing the definitions for HTTP-specific headers | http://a.ml/vocabularies/apiBinding#headers |
 | bindingVersion | string | The version of this binding | http://a.ml/vocabularies/apiBinding#bindingVersion |
 | type | string | Binding for a corresponding known type | http://a.ml/vocabularies/apiBinding#type |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |

## XMLSerializer
Information about how to encode into XML a particular data shape

 | Name | Value | Documentation | Namespace |
 | ------ | ------ | ------ | ------ |
 | xmlAttribute | boolean | XML attribute mapping | http://a.ml/vocabularies/shapes#xmlAttribute |
 | xmlWrapped | boolean | XML wrapped mapping flag | http://a.ml/vocabularies/shapes#xmlWrapped |
 | xmlName | string | XML name mapping | http://a.ml/vocabularies/shapes#xmlName |
 | xmlNamespace | string | XML namespace mapping | http://a.ml/vocabularies/shapes#xmlNamespace |
 | xmlPrefix | string | XML prefix mapping | http://a.ml/vocabularies/shapes#xmlPrefix |
 | extends | [[DomainElement](#domainelement)] | Entity that is going to be extended overlaying or adding additional information The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model. | http://a.ml/vocabularies/document#extends |
