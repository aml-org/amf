package amf.apicontract.internal.convert

import amf.apicontract.client.platform.model.document.{
  AnnotationTypeDeclaration,
  DataType,
  DocumentationItem,
  Extension,
  NamedExample,
  Overlay,
  ResourceTypeFragment,
  SecuritySchemeFragment,
  TraitFragment
}
import amf.apicontract.client.platform.model.domain.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091MessageBinding,
  Amqp091OperationBinding,
  Amqp091Queue,
  AsyncApi,
  Callback,
  ChannelBindings,
  CorrelationId,
  EmptyBinding,
  Encoding,
  EndPoint,
  HttpMessageBinding,
  HttpOperationBinding,
  KafkaMessageBinding,
  KafkaOperationBinding,
  License,
  MessageBindings,
  MqttMessageBinding,
  MqttOperationBinding,
  MqttServerBinding,
  MqttServerLastWill,
  OAuth2Flow,
  Operation,
  OperationBindings,
  Organization,
  Parameter,
  ParametrizedResourceType,
  ParametrizedSecurityScheme,
  ParametrizedTrait,
  Payload,
  Request,
  ResourceType,
  Response,
  Scope,
  SecurityRequirement,
  SecurityScheme,
  Server,
  ServerBindings,
  Settings,
  Tag,
  TemplatedLink,
  Trait,
  WebApi,
  WebSocketsChannelBinding
}
import amf.apicontract.internal.metamodel.document.{ExtensionModel, OverlayModel}
import amf.apicontract.client.scala.model.{document, domain}
import amf.apicontract.client.scala.model.document.{
  AnnotationTypeDeclarationFragment,
  DataTypeFragment,
  DocumentationItemFragment,
  NamedExampleFragment
}
import amf.client.model.domain._
import amf.core.client.platform.model.document.PayloadFragment
import amf.core.client.platform.model.domain.RecursiveShape
import amf.core.internal.metamodel.document.PayloadFragmentModel
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.internal.metamodel.domain.{
  CallbackModel,
  CorrelationIdModel,
  EncodingModel,
  EndPointModel,
  LicenseModel,
  OperationModel,
  OrganizationModel,
  ParameterModel,
  PayloadModel,
  RequestModel,
  ResponseModel,
  ServerModel,
  TagModel,
  TemplatedLinkModel
}
import amf.plugins.document.apicontract.model
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.plugins.domain.apicontract.metamodel.amf.apicontract.internal.metamodel.domain.templates
import amf.plugins.domain.{apicontract, shapes}

/** Shared WebApi registrations. */
// TODO: could be renamed to ApiRegister??
private[amf] object ApiRegister extends PlatformSecrets {

  // TODO ARM remove when APIMF-3000 is done
  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = {

    // Web Api (document)
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

    // WebApi (domain)
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
    platform.registerWrapper(templates.ParametrizedResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ParametrizedResourceType =>
        ParametrizedResourceType(s)
    }
    platform.registerWrapper(TagModel) {
      case s: domain.Tag => Tag(s)
    }
    platform.registerWrapper(templates.ParametrizedTraitModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ParametrizedTrait => ParametrizedTrait(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.ParametrizedSecuritySchemeModel) {
      case s: amf.apicontract.client.scala.model.domain.security.ParametrizedSecurityScheme =>
        ParametrizedSecurityScheme(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.SecurityRequirementModel) {
      case s: amf.apicontract.client.scala.model.domain.security.SecurityRequirement => SecurityRequirement(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.SecuritySchemeModel) {
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
    platform.registerWrapper(apicontract.metamodel.security.ScopeModel) {
      case s: amf.apicontract.client.scala.model.domain.security.Scope => Scope(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.OAuth2FlowModel) {
      case of: amf.apicontract.client.scala.model.domain.security.OAuth2Flow => OAuth2Flow(of)
    }
    platform.registerWrapper(apicontract.metamodel.security.SettingsModel) {
      case s: amf.apicontract.client.scala.model.domain.security.Settings => new Settings(s)
    }
    platform.registerWrapper(apicontract.metamodel.api.WebApiModel) {
      case s: amf.apicontract.client.scala.model.domain.api.WebApi => WebApi(s)
    }
    platform.registerWrapper(apicontract.metamodel.api.AsyncApiModel) {
      case s: amf.apicontract.client.scala.model.domain.api.AsyncApi => AsyncApi(s)
    }
    platform.registerWrapper(apicontract.metamodel.templates.TraitModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.Trait => Trait(s)
    }
    platform.registerWrapper(apicontract.metamodel.templates.ResourceTypeModel) {
      case s: amf.apicontract.client.scala.model.domain.templates.ResourceType => ResourceType(s)
    }

    // DataShapes (domain)
    platform.registerWrapper(shapes.metamodel.AnyShapeModel) {
      case s: shapes.models.AnyShape => new AnyShape(s)
    }
    platform.registerWrapper(shapes.metamodel.NilShapeModel) {
      case s: shapes.models.NilShape => NilShape(s)
    }
    platform.registerWrapper(shapes.metamodel.ArrayShapeModel) {
      case s: shapes.models.ArrayShape => ArrayShape(s)
    }
    platform.registerWrapper(shapes.metamodel.MatrixShapeModel) {
      case s: shapes.models.MatrixShape => new MatrixShape(s.toArrayShape)
    }
    platform.registerWrapper(shapes.metamodel.TupleShapeModel) {
      case s: shapes.models.TupleShape => TupleShape(s)
    }
    platform.registerWrapper(shapes.metamodel.CreativeWorkModel) {
      case s: shapes.models.CreativeWork => CreativeWork(s)
    }
    platform.registerWrapper(shapes.metamodel.ExampleModel) {
      case s: shapes.models.Example => Example(s)
    }
    platform.registerWrapper(shapes.metamodel.FileShapeModel) {
      case s: shapes.models.FileShape => FileShape(s)
    }
    platform.registerWrapper(shapes.metamodel.NodeShapeModel) {
      case s: shapes.models.NodeShape => NodeShape(s)
    }
    platform.registerWrapper(shapes.metamodel.ScalarShapeModel) {
      case s: shapes.models.ScalarShape => ScalarShape(s)
    }
    platform.registerWrapper(shapes.metamodel.SchemaShapeModel) {
      case s: shapes.models.SchemaShape => SchemaShape(s)
    }
    platform.registerWrapper(shapes.metamodel.XMLSerializerModel) {
      case s: shapes.models.XMLSerializer => XMLSerializer(s)
    }
    platform.registerWrapper(shapes.metamodel.PropertyDependenciesModel) {
      case s: shapes.models.PropertyDependencies => PropertyDependencies(s)
    }
    platform.registerWrapper(shapes.metamodel.SchemaDependenciesModel) {
      case s: shapes.models.SchemaDependencies => SchemaDependencies(s)
    }
    platform.registerWrapper(shapes.metamodel.UnionShapeModel) {
      case s: shapes.models.UnionShape => UnionShape(s)
    }
    platform.registerWrapper(amf.core.internal.metamodel.domain.RecursiveShapeModel) {
      case s: amf.core.client.scala.model.domain.RecursiveShape => RecursiveShape(s)
    }
    platform.registerWrapper(TemplatedLinkModel) {
      case s: domain.TemplatedLink => TemplatedLink(s)
    }
    platform.registerWrapper(IriTemplateMappingModel) {
      case s: apicontract.models.IriTemplateMapping => IriTemplateMapping(s)
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
  }

}
