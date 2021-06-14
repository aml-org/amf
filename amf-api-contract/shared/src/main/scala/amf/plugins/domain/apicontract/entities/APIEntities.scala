package amf.plugins.domain.apicontract.entities

import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.plugins.domain.shapes.metamodel.{CreativeWorkModel, DiscriminatorValueMappingModel}
import amf.plugins.domain.apicontract.metamodel.api.{AsyncApiModel, WebApiModel}
import amf.plugins.domain.apicontract.metamodel.bindings._
import amf.plugins.domain.apicontract.metamodel.security._
import amf.plugins.domain.apicontract.metamodel.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}
import amf.plugins.domain.apicontract.metamodel._

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
    DiscriminatorValueMappingModel
  )

}
