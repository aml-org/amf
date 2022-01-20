package amf.apicontract.internal.convert

import amf.aml.internal.utils.VocabulariesRegister
import amf.apicontract.client.platform.model.document._
import amf.apicontract.client.platform.model.domain.api.{AsyncApi, WebApi}
import amf.apicontract.client.platform.model.domain.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091MessageBinding,
  Amqp091OperationBinding,
  Amqp091Queue
}
import amf.apicontract.client.platform.model.domain.bindings.http.{HttpMessageBinding, HttpOperationBinding}
import amf.apicontract.client.platform.model.domain.bindings.kafka.{KafkaMessageBinding, KafkaOperationBinding}
import amf.apicontract.client.platform.model.domain.bindings.mqtt.{
  MqttMessageBinding,
  MqttOperationBinding,
  MqttServerBinding,
  MqttServerLastWill
}
import amf.apicontract.client.platform.model.domain.bindings.{
  ChannelBindings,
  EmptyBinding,
  MessageBindings,
  OperationBindings,
  ServerBindings
}
import amf.apicontract.client.platform.model.domain.security._
import amf.apicontract.client.platform.model.domain.templates.{
  ParametrizedResourceType,
  ParametrizedTrait,
  ResourceType,
  Trait
}
import amf.apicontract.client.platform.model.domain._
import amf.apicontract.client.platform.model.domain.bindings.websockets.WebSocketsChannelBinding
import amf.apicontract.client.scala.model.document.{
  AnnotationTypeDeclarationFragment,
  DataTypeFragment,
  DocumentationItemFragment,
  NamedExampleFragment
}
import amf.apicontract.client.platform.model.domain.Message
import amf.apicontract.client.scala.model.{document, domain}
import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.internal.metamodel.document.{APIContractProcessingDataModel, ExtensionModel, OverlayModel}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.metamodel.domain.api._
import amf.apicontract.internal.metamodel.domain.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}
import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.security.{
  ParametrizedSecuritySchemeModel,
  ScopeModel,
  SecurityRequirementModel,
  SecuritySchemeModel
}
import amf.core.client.platform.model.document.PayloadFragment
import amf.core.internal.metamodel.document.PayloadFragmentModel
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.convert.ShapesRegister
import amf.apicontract.client.platform.model.document.APIContractProcessingData
import amf.core.internal.convert.CoreRegister

/** Shared WebApi registrations. */
private[amf] object ApiRegister extends PlatformSecrets {

  register()

  private def register(): Unit = register(platform)

  private def register(platform: Platform): Unit = {

    // shapes (domain)
    ShapesRegister.register(platform)
    VocabulariesRegister.register(platform)

    // Api (document)
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) {
      case s: AnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(s)
    }
    platform.registerWrapper(DataTypeFragmentModel) {
      case s: DataTypeFragment => DataType(s)
    }
    platform.registerWrapper(PayloadFragmentModel) {
      case s: amf.core.client.scala.model.document.PayloadFragment => PayloadFragment(s)
    }
    platform.registerWrapper(DocumentationItemFragmentModel) {
      case s: DocumentationItemFragment => DocumentationItem(s)
    }
    platform.registerWrapper(NamedExampleFragmentModel) {
      case s: NamedExampleFragment => NamedExample(s)
    }
    platform.registerWrapper(ResourceTypeFragmentModel) {
      case s: document.ResourceTypeFragment => ResourceTypeFragment(s)
    }
    platform.registerWrapper(SecuritySchemeFragmentModel) {
      case s: document.SecuritySchemeFragment => SecuritySchemeFragment(s)
    }
    platform.registerWrapper(TraitFragmentModel) {
      case s: document.TraitFragment => TraitFragment(s)
    }
    platform.registerWrapper(ExtensionModel) {
      case m: document.Extension => Extension(m)
    }
    platform.registerWrapper(OverlayModel) {
      case m: document.Overlay => Overlay(m)
    }

    // Api (domain)
    platform.registerWrapper(EndPointModel) {
      case s: domain.EndPoint => EndPoint(s)
    }
    platform.registerWrapper(LicenseModel) {
      case s: domain.License => License(s)
    }
    platform.registerWrapper(OperationModel) {
      case s: domain.Operation => Operation(s)
    }
    platform.registerWrapper(OrganizationModel) {
      case s: domain.Organization => Organization(s)
    }
    platform.registerWrapper(ParameterModel) {
      case s: domain.Parameter => Parameter(s)
    }
    platform.registerWrapper(ServerModel) {
      case s: domain.Server => Server(s)
    }
    platform.registerWrapper(CallbackModel) {
      case s: domain.Callback => Callback(s)
    }
    platform.registerWrapper(EncodingModel) {
      case s: domain.Encoding => Encoding(s)
    }
    platform.registerWrapper(ParametrizedResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ParametrizedResourceType =>
        ParametrizedResourceType(s)
    }
    platform.registerWrapper(TagModel) {
      case s: domain.Tag => Tag(s)
    }
    platform.registerWrapper(ParametrizedTraitModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ParametrizedTrait => ParametrizedTrait(s)
    }
    platform.registerWrapper(ParametrizedSecuritySchemeModel) {
      case s: amf.apicontract.client.scala.model.domain.security.ParametrizedSecurityScheme =>
        ParametrizedSecurityScheme(s)
    }
    platform.registerWrapper(SecurityRequirementModel) {
      case s: amf.apicontract.client.scala.model.domain.security.SecurityRequirement => SecurityRequirement(s)
    }
    platform.registerWrapper(SecuritySchemeModel) {
      case s: amf.apicontract.client.scala.model.domain.security.SecurityScheme => SecurityScheme(s)
    }
    platform.registerWrapper(PayloadModel) {
      case s: domain.Payload => Payload(s)
    }
    platform.registerWrapper(RequestModel) {
      case s: domain.Request => Request(s)
    }
    platform.registerWrapper(ResponseModel) {
      case s: domain.Response => Response(s)
    }
    platform.registerWrapper(MessageModel) {
      case s: domain.Message => new Message(s)
    }
    platform.registerWrapper(ScopeModel) {
      case s: amf.apicontract.client.scala.model.domain.security.Scope => Scope(s)
    }
    platform.registerWrapper(OAuth2FlowModel) {
      case of: amf.apicontract.client.scala.model.domain.security.OAuth2Flow => OAuth2Flow(of)
    }
    platform.registerWrapper(SettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.Settings => new Settings(s)
    }
    platform.registerWrapper(OAuth2SettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.OAuth2Settings => OAuth2Settings(s)
    }
    platform.registerWrapper(HttpSettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.HttpSettings => HttpSettings(s)
    }
    platform.registerWrapper(OpenIdConnectSettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.OpenIdConnectSettings => OpenIdConnectSettings(s)
    }
    platform.registerWrapper(ApiKeySettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.ApiKeySettings => ApiKeySettings(s)
    }
    platform.registerWrapper(OAuth1SettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.OAuth1Settings => OAuth1Settings(s)
    }
    platform.registerWrapper(HttpApiKeySettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.HttpApiKeySettings => HttpApiKeySettings(s)
    }
    platform.registerWrapper(WebApiModel) {
      case s: amf.apicontract.client.scala.model.domain.api.WebApi => WebApi(s)
    }
    platform.registerWrapper(AsyncApiModel) {
      case s: amf.apicontract.client.scala.model.domain.api.AsyncApi => AsyncApi(s)
    }
    platform.registerWrapper(TraitModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.Trait => Trait(s)
    }
    platform.registerWrapper(ResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ResourceType => ResourceType(s)
    }
    platform.registerWrapper(TemplatedLinkModel) {
      case s: domain.TemplatedLink => TemplatedLink(s)
    }
    platform.registerWrapper(CorrelationIdModel) {
      case s: domain.CorrelationId => CorrelationId(s)
    }
    platform.registerWrapper(Amqp091ChannelBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelBinding => Amqp091ChannelBinding(s)
    }
    platform.registerWrapper(OperationBindingsModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.OperationBindings => OperationBindings(s)
    }
    platform.registerWrapper(ServerBindingsModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ServerBindings => ServerBindings(s)
    }
    platform.registerWrapper(ChannelBindingsModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ChannelBindings => ChannelBindings(s)
    }
    platform.registerWrapper(MessageBindingsModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.MessageBindings => MessageBindings(s)
    }
    platform.registerWrapper(Amqp091ChannelExchangeModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelExchange =>
        Amqp091ChannelExchange(s)
    }
    platform.registerWrapper(Amqp091QueueModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091Queue => Amqp091Queue(s)
    }
    platform.registerWrapper(Amqp091MessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding => Amqp091MessageBinding(s)
    }
    platform.registerWrapper(Amqp091OperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091OperationBinding =>
        Amqp091OperationBinding(s)
    }
    platform.registerWrapper(HttpMessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding => HttpMessageBinding(s)
    }
    platform.registerWrapper(HttpOperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding => HttpOperationBinding(s)
    }
    platform.registerWrapper(KafkaMessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding => KafkaMessageBinding(s)
    }
    platform.registerWrapper(KafkaOperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding =>
        KafkaOperationBinding(s)
    }
    platform.registerWrapper(MqttMessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding => MqttMessageBinding(s)
    }
    platform.registerWrapper(MqttOperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding => MqttOperationBinding(s)
    }
    platform.registerWrapper(MqttServerBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttServerBinding => MqttServerBinding(s)
    }
    platform.registerWrapper(MqttServerLastWillModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttServerLastWill => MqttServerLastWill(s)
    }
    platform.registerWrapper(WebSocketsChannelBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.websockets.WebSocketsChannelBinding =>
        WebSocketsChannelBinding(s)
    }
    platform.registerWrapper(EmptyBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.EmptyBinding => EmptyBinding(s)
    }
    platform.registerWrapper(APIContractProcessingDataModel) {
      case s: amf.apicontract.client.scala.model.document.APIContractProcessingData => APIContractProcessingData(s)
    }
  }

}
