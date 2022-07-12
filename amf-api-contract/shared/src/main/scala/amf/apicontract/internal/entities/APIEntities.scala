package amf.apicontract.internal.entities

import amf.apicontract.internal.metamodel.document.APIContractProcessingDataModel
import amf.apicontract.internal.metamodel.domain.api.{AsyncApiModel, BaseApiModel, WebApiModel}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.metamodel.domain.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}
import amf.apicontract.internal.metamodel.domain._
import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.shapes.internal.document.metamodel.JsonSchemaDocumentModel
import amf.shapes.internal.domain.metamodel.{CreativeWorkModel, DiscriminatorValueMappingModel, IriTemplateMappingModel}

private[amf] object APIEntities extends Entities {

  override protected val innerEntities: Seq[ModelDefaultBuilder] = Seq(
    WebApiModel,
    AsyncApiModel,
    CreativeWorkModel,
    OrganizationModel,
    LicenseModel,
    EndPointModel,
    OperationModel,
    ParameterModel,
    ServerModel,
    PayloadModel,
    RequestModel,
    ResponseModel,
    CustomDomainPropertyModel,
    DomainExtensionModel,
    ParametrizedSecuritySchemeModel,
    SecurityRequirementModel,
    ScopeModel,
    SecuritySchemeModel,
    SettingsModel,
    OAuth1SettingsModel,
    OAuth2SettingsModel,
    OAuth2FlowModel,
    ApiKeySettingsModel,
    TraitModel,
    ResourceTypeModel,
    ParametrizedResourceTypeModel,
    ParametrizedTraitModel,
    TagModel,
    TemplatedLinkModel,
    IriTemplateMappingModel,
    EncodingModel,
    CorrelationIdModel,
    CallbackModel,
    Amqp091ChannelBindingModel,
    Amqp091MessageBindingModel,
    Amqp091QueueModel,
    OperationBindingModel,
    Amqp091ChannelExchangeModel,
    ChannelBindingModel,
    EmptyBindingModel,
    HttpOperationBindingModel,
    HttpMessageBindingModel,
    KafkaOperationBindingModel,
    KafkaMessageBindingModel,
    MessageBindingModel,
    MqttServerBindingModel,
    MqttServerLastWillModel,
    MqttOperationBindingModel,
    MqttMessageBindingModel,
    ServerBindingModel,
    WebSocketsChannelBindingModel,
    DiscriminatorValueMappingModel,
    OperationBindingsModel,
    MessageBindingsModel,
    ChannelBindingsModel,
    ServerBindingsModel,
    MessageModel,
    Amqp091OperationBindingModel,
    OpenIdConnectSettingsModel,
    HttpSettingsModel,
    HttpApiKeySettingsModel
  )
}
