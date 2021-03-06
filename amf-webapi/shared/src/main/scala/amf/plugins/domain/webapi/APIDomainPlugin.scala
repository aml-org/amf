package amf.plugins.domain.webapi

import amf.core.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.client.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.shapes.metamodel.{CreativeWorkModel, DiscriminatorValueMappingModel}
import amf.plugins.domain.webapi.annotations._
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.api.{AsyncApiModel, WebApiModel}
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchangeModel,
  Amqp091MessageBindingModel,
  Amqp091QueueModel,
  ChannelBindingModel,
  EmptyBindingModel,
  HttpMessageBindingModel,
  HttpOperationBindingModel,
  KafkaMessageBindingModel,
  KafkaOperationBindingModel,
  MessageBindingModel,
  MqttMessageBindingModel,
  MqttOperationBindingModel,
  MqttServerBindingModel,
  MqttServerLastWillModel,
  OperationBindingModel,
  ServerBindingModel,
  WebSocketsChannelBindingModel
}
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.metamodel.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}

import scala.concurrent.{ExecutionContext, Future}

object APIDomainPlugin extends AMFDomainPlugin {

  override val ID = "API Domain"

  override def dependencies() = Seq(DataShapesDomainPlugin)

  override def modelEntities = Seq(
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

  override def serializableAnnotations() = Map(
    "parent-end-point"                       -> ParentEndPoint,
    "orphan-oas-extension"                   -> OrphanOasExtension,
    "type-property-lexical-info"             -> TypePropertyLexicalInfo,
    "parameter-binding-in-body-lexical-info" -> ParameterBindingInBodyLexicalInfo,
    "invalid-binding"                        -> InvalidBinding
  )

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }
}
