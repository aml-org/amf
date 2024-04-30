package amf.apicontract.internal.convert

import amf.apicontract.client.platform.model.document._
import amf.apicontract.client.platform.model.domain._
import amf.apicontract.client.platform.model.domain.api.{AsyncApi, WebApi}
import amf.apicontract.client.platform.model.domain.bindings._
import amf.apicontract.client.platform.model.domain.bindings.amqp._
import amf.apicontract.client.platform.model.domain.bindings.anypointmq._
import amf.apicontract.client.platform.model.domain.bindings.googlepubsub._
import amf.apicontract.client.platform.model.domain.bindings.http._
import amf.apicontract.client.platform.model.domain.bindings.ibmmq._
import amf.apicontract.client.platform.model.domain.bindings.kafka._
import amf.apicontract.client.platform.model.domain.bindings.mqtt._
import amf.apicontract.client.platform.model.domain.bindings.pulsar._
import amf.apicontract.client.platform.model.domain.bindings.solace._
import amf.apicontract.client.platform.model.domain.bindings.websockets.WebSocketsChannelBinding
import amf.apicontract.client.platform.model.domain.federation._
import amf.apicontract.client.platform.model.domain.security._
import amf.apicontract.client.platform.model.domain.templates.{
  ParametrizedResourceType,
  ParametrizedTrait,
  ResourceType,
  Trait
}
import amf.apicontract.client.scala.model.document.{
  AnnotationTypeDeclarationFragment,
  DataTypeFragment,
  DocumentationItemFragment,
  NamedExampleFragment
}
import amf.apicontract.client.scala.model.{document, domain}
import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.internal.metamodel.document.{
  APIContractProcessingDataModel,
  ComponentModuleModel,
  ExtensionModel,
  OverlayModel
}
import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.api._
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.metamodel.domain.federation.{
  EndpointFederationMetadataModel,
  OperationFederationMetadataModel,
  ParameterFederationMetadataModel,
  ParameterKeyMappingModel
}
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.metamodel.domain.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}
import amf.core.client.platform.model.document.PayloadFragment
import amf.core.internal.convert.UniqueInitializer
import amf.core.internal.metamodel.document.PayloadFragmentModel
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model
import amf.shapes.client.scala.model.document.{DataTypeFragment => ShapeDataTypeFragment}
import amf.shapes.internal.convert.ShapesRegister
import amf.shapes.internal.document.metamodel.{DataTypeFragmentModel, JsonSchemaDocumentModel}

/** Shared WebApi registrations. */
private[amf] object ApiRegister extends UniqueInitializer with PlatformSecrets {

  // TODO ARM remove when APIMF-3000 is done
  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = if (shouldInitialize) {

    // shapes (domain)
    ShapesRegister.register(platform)

    // Api (document)
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) { case s: AnnotationTypeDeclarationFragment =>
      AnnotationTypeDeclaration(s)
    }
    // TODO Change this when we can break interface
    platform.registerWrapper(DataTypeFragmentModel) {
      case d: DataTypeFragment       => DataType(d)
      case sd: ShapeDataTypeFragment => new DataType(sd)
    }
    platform.registerWrapper(PayloadFragmentModel) { case s: amf.core.client.scala.model.document.PayloadFragment =>
      PayloadFragment(s)
    }
    platform.registerWrapper(DocumentationItemFragmentModel) { case s: DocumentationItemFragment =>
      DocumentationItem(s)
    }
    platform.registerWrapper(NamedExampleFragmentModel) { case s: NamedExampleFragment =>
      NamedExample(s)
    }
    platform.registerWrapper(ResourceTypeFragmentModel) { case s: document.ResourceTypeFragment =>
      ResourceTypeFragment(s)
    }
    platform.registerWrapper(SecuritySchemeFragmentModel) { case s: document.SecuritySchemeFragment =>
      SecuritySchemeFragment(s)
    }
    platform.registerWrapper(TraitFragmentModel) { case s: document.TraitFragment =>
      TraitFragment(s)
    }
    platform.registerWrapper(ExtensionModel) { case m: document.Extension =>
      Extension(m)
    }
    platform.registerWrapper(OverlayModel) { case m: document.Overlay =>
      Overlay(m)
    }
    platform.registerWrapper(ComponentModuleModel) { case m: document.ComponentModule =>
      new ComponentModule(m)
    }

    // Api (domain)
    platform.registerWrapper(EndPointModel) { case s: domain.EndPoint =>
      EndPoint(s)
    }
    platform.registerWrapper(LicenseModel) { case s: domain.License =>
      License(s)
    }
    platform.registerWrapper(OperationModel) { case s: domain.Operation =>
      Operation(s)
    }
    platform.registerWrapper(OrganizationModel) { case s: domain.Organization =>
      Organization(s)
    }
    platform.registerWrapper(ParameterModel) { case s: domain.Parameter =>
      Parameter(s)
    }
    platform.registerWrapper(ServerModel) { case s: domain.Server =>
      Server(s)
    }
    platform.registerWrapper(CallbackModel) { case s: domain.Callback =>
      Callback(s)
    }
    platform.registerWrapper(EncodingModel) { case s: domain.Encoding =>
      Encoding(s)
    }
    platform.registerWrapper(ParametrizedResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ParametrizedResourceType =>
        ParametrizedResourceType(s)
    }
    platform.registerWrapper(TagModel) { case s: domain.Tag =>
      Tag(s)
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
    platform.registerWrapper(PayloadModel) { case s: domain.Payload =>
      Payload(s)
    }
    platform.registerWrapper(RequestModel) { case s: domain.Request =>
      Request(s)
    }
    platform.registerWrapper(ResponseModel) { case s: domain.Response =>
      Response(s)
    }
    platform.registerWrapper(MessageModel) { case s: domain.Message =>
      new Message(s)
    }
    platform.registerWrapper(ScopeModel) { case s: amf.apicontract.client.scala.model.domain.security.Scope =>
      Scope(s)
    }
    platform.registerWrapper(OAuth2FlowModel) {
      case of: amf.apicontract.client.scala.model.domain.security.OAuth2Flow => OAuth2Flow(of)
    }
    platform.registerWrapper(SettingsModel) { case s: amf.apicontract.client.scala.model.domain.security.Settings =>
      new Settings(s)
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
    platform.registerWrapper(WebApiModel) { case s: amf.apicontract.client.scala.model.domain.api.WebApi =>
      WebApi(s)
    }
    platform.registerWrapper(AsyncApiModel) { case s: amf.apicontract.client.scala.model.domain.api.AsyncApi =>
      AsyncApi(s)
    }
    platform.registerWrapper(TraitModel) { case s: amf.apicontract.client.scala.model.domain.templates.Trait =>
      Trait(s)
    }
    platform.registerWrapper(ResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ResourceType => ResourceType(s)
    }
    platform.registerWrapper(TemplatedLinkModel) { case s: domain.TemplatedLink =>
      TemplatedLink(s)
    }
    platform.registerWrapper(CorrelationIdModel) { case s: domain.CorrelationId =>
      CorrelationId(s)
    }
    platform.registerWrapper(Amqp091ChannelBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelBinding010 =>
        Amqp091ChannelBinding010(s)
    }
    platform.registerWrapper(Amqp091ChannelBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelBinding020 =>
        Amqp091ChannelBinding020(s)
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
    platform.registerWrapper(Amqp091ChannelExchange010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelExchange010 =>
        Amqp091ChannelExchange010(s)
    }
    platform.registerWrapper(Amqp091ChannelExchange020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091ChannelExchange020 =>
        Amqp091ChannelExchange020(s)
    }
    platform.registerWrapper(Amqp091Queue010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091Queue010 => Amqp091Queue010(s)
    }
    platform.registerWrapper(Amqp091Queue020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091Queue020 => Amqp091Queue020(s)
    }
    platform.registerWrapper(Amqp091MessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding => Amqp091MessageBinding(s)
    }
    platform.registerWrapper(Amqp091OperationBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091OperationBinding010 =>
        Amqp091OperationBinding010(s)
    }
    platform.registerWrapper(Amqp091OperationBinding030Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091OperationBinding030 =>
        Amqp091OperationBinding030(s)
    }
    platform.registerWrapper(HttpMessageBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding020 => HttpMessageBinding020(s)
    }
    platform.registerWrapper(HttpMessageBinding030Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding030 => HttpMessageBinding030(s)
    }
    platform.registerWrapper(HttpOperationBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding010 =>
        HttpOperationBinding010(s)
    }
    platform.registerWrapper(HttpOperationBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding020 =>
        HttpOperationBinding020(s)
    }
    platform.registerWrapper(KafkaMessageBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding010 =>
        KafkaMessageBinding010(s)
    }
    platform.registerWrapper(KafkaMessageBinding030Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding030 =>
        KafkaMessageBinding030(s)
    }
    platform.registerWrapper(KafkaOperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding => KafkaOperationBinding(s)
    }
    platform.registerWrapper(KafkaServerBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaServerBinding => KafkaServerBinding(s)
    }
    platform.registerWrapper(KafkaChannelBinding030Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaChannelBinding030 =>
        KafkaChannelBinding030(s)
    }
    platform.registerWrapper(KafkaChannelBinding040Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaChannelBinding040 =>
        KafkaChannelBinding040(s)
    }
    platform.registerWrapper(KafkaChannelBinding050Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaChannelBinding050 =>
        KafkaChannelBinding050(s)
    }
    platform.registerWrapper(KafkaTopicConfiguration040Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaTopicConfiguration040 =>
        KafkaTopicConfiguration040(s)
    }
    platform.registerWrapper(KafkaTopicConfiguration050Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaTopicConfiguration050 =>
        KafkaTopicConfiguration050(s)
    }
    platform.registerWrapper(MqttMessageBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding010 => MqttMessageBinding010(s)
    }
    platform.registerWrapper(MqttMessageBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding020 => MqttMessageBinding020(s)
    }
    platform.registerWrapper(MqttOperationBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding010 =>
        MqttOperationBinding010(s)
    }
    platform.registerWrapper(MqttOperationBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding020 =>
        MqttOperationBinding020(s)
    }
    platform.registerWrapper(MqttServerBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttServerBinding010 => MqttServerBinding010(s)
    }
    platform.registerWrapper(MqttServerBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttServerBinding020 => MqttServerBinding020(s)
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
    platform.registerWrapper(SolaceServerBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceServerBinding =>
        SolaceServerBinding(s)
    }
    platform.registerWrapper(SolaceOperationBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceOperationBinding =>
        SolaceOperationBinding(s)
    }
    platform.registerWrapper(SolaceOperationDestination010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceOperationDestination010 =>
        SolaceOperationDestination010(s)
    }
    platform.registerWrapper(SolaceOperationDestination020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceOperationDestination020 =>
        SolaceOperationDestination020(s)
    }
    platform.registerWrapper(SolaceOperationQueueModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceOperationQueue =>
        SolaceOperationQueue(s)
    }
    platform.registerWrapper(SolaceOperationTopicModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.solace.SolaceOperationTopic =>
        SolaceOperationTopic(s)
    }
    platform.registerWrapper(AnypointMQMessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding =>
        AnypointMQMessageBinding(s)
    }
    platform.registerWrapper(AnypointMQChannelBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQChannelBinding =>
        AnypointMQChannelBinding(s)
    }
    platform.registerWrapper(IBMMQMessageBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQMessageBinding => IBMMQMessageBinding(s)
    }
    platform.registerWrapper(IBMMQServerBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQServerBinding => IBMMQServerBinding(s)
    }
    platform.registerWrapper(IBMMQChannelBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQChannelBinding => IBMMQChannelBinding(s)
    }
    platform.registerWrapper(IBMMQChannelQueueModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQChannelQueue => IBMMQChannelQueue(s)
    }
    platform.registerWrapper(IBMMQChannelTopicModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQChannelTopic => IBMMQChannelTopic(s)
    }
    platform.registerWrapper(GooglePubSubChannelBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubChannelBinding010 =>
        GooglePubSubChannelBinding010(s)
    }
    platform.registerWrapper(GooglePubSubChannelBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubChannelBinding020 =>
        GooglePubSubChannelBinding020(s)
    }
    platform.registerWrapper(GooglePubSubMessageStoragePolicyModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubMessageStoragePolicy =>
        GooglePubSubMessageStoragePolicy(s)
    }
    platform.registerWrapper(GooglePubSubSchemaSettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubSchemaSettings =>
        GooglePubSubSchemaSettings(s)
    }
    platform.registerWrapper(GooglePubSubMessageBinding010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubMessageBinding010 =>
        GooglePubSubMessageBinding010(s)
    }
    platform.registerWrapper(GooglePubSubMessageBinding020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubMessageBinding020 =>
        GooglePubSubMessageBinding020(s)
    }
    platform.registerWrapper(GooglePubSubSchemaDefinition010Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubSchemaDefinition010 =>
        GooglePubSubSchemaDefinition010(s)
    }
    platform.registerWrapper(GooglePubSubSchemaDefinition020Model) {
      case s: amf.apicontract.client.scala.model.domain.bindings.googlepubsub.GooglePubSubSchemaDefinition020 =>
        GooglePubSubSchemaDefinition020(s)
    }
    platform.registerWrapper(APIContractProcessingDataModel) {
      case s: amf.apicontract.client.scala.model.document.APIContractProcessingData => APIContractProcessingData(s)
    }
    platform.registerWrapper(JsonSchemaDocumentModel) { case s: model.document.JsonSchemaDocument =>
      JsonSchemaDocument(s)
    }
    platform.registerWrapper(OperationFederationMetadataModel) {
      case s: amf.apicontract.client.scala.model.domain.federation.OperationFederationMetadata =>
        OperationFederationMetadata(s)
    }
    platform.registerWrapper(ParameterKeyMappingModel) {
      case s: amf.apicontract.client.scala.model.domain.federation.ParameterKeyMapping => ParameterKeyMapping(s)
    }
    platform.registerWrapper(EndpointFederationMetadataModel) {
      case s: amf.apicontract.client.scala.model.domain.federation.EndPointFederationMetadata =>
        EndPointFederationMetadata(s)
    }
    platform.registerWrapper(ParameterFederationMetadataModel) {
      case s: amf.apicontract.client.scala.model.domain.federation.ParameterFederationMetadata =>
        ParameterFederationMetadata(s)
    }
    platform.registerWrapper(PulsarServerBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.pulsar.PulsarServerBinding =>
        PulsarServerBinding(s)
    }
    platform.registerWrapper(PulsarChannelBindingModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.pulsar.PulsarChannelBinding =>
        PulsarChannelBinding(s)
    }
    platform.registerWrapper(PulsarChannelRetentionModel) {
      case s: amf.apicontract.client.scala.model.domain.bindings.pulsar.PulsarChannelRetention =>
        PulsarChannelRetention(s)
    }
  }

}
