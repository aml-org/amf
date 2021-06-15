package amf.client.convert

import amf.client.model.document._
import amf.client.model.domain._
import amf.core.client.platform.model.document.PayloadFragment
import amf.core.client.platform.model.domain.RecursiveShape
import amf.core.internal.metamodel.document.PayloadFragmentModel
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.plugins.document.apicontract.metamodel.FragmentsTypesModels._
import amf.plugins.document.apicontract.model
import amf.plugins.domain.apicontract.metamodel.bindings._
import amf.plugins.domain.apicontract.metamodel.{
  CorrelationIdModel,
  IriTemplateMappingModel,
  TemplatedLinkModel,
  templates
}
import amf.plugins.domain.{apicontract, shapes}

/** Shared WebApi registrations. */
// TODO: could be renamed to ApiRegister??
private[amf] object ApiRegister extends PlatformSecrets {

  // TODO ARM remove when APIMF-3000 is done
  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = {

    // Web Api (document)
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) {
      case s: model.AnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(s)
    }
    platform.registerWrapper(DataTypeFragmentModel) {
      case s: model.DataTypeFragment => DataType(s)
    }
    platform.registerWrapper(PayloadFragmentModel) {
      case s: amf.core.client.scala.model.document.PayloadFragment => PayloadFragment(s)
    }
    platform.registerWrapper(DocumentationItemFragmentModel) {
      case s: model.DocumentationItemFragment => DocumentationItem(s)
    }
    platform.registerWrapper(NamedExampleFragmentModel) {
      case s: model.NamedExampleFragment => NamedExample(s)
    }
    platform.registerWrapper(ResourceTypeFragmentModel) {
      case s: model.ResourceTypeFragment => ResourceTypeFragment(s)
    }
    platform.registerWrapper(SecuritySchemeFragmentModel) {
      case s: model.SecuritySchemeFragment => SecuritySchemeFragment(s)
    }
    platform.registerWrapper(TraitFragmentModel) {
      case s: model.TraitFragment => TraitFragment(s)
    }
    platform.registerWrapper(amf.plugins.document.apicontract.metamodel.ExtensionModel) {
      case m: model.Extension => Extension(m)
    }
    platform.registerWrapper(amf.plugins.document.apicontract.metamodel.OverlayModel) {
      case m: model.Overlay => Overlay(m)
    }

    // WebApi (domain)
    platform.registerWrapper(apicontract.metamodel.EndPointModel) {
      case s: apicontract.models.EndPoint => EndPoint(s)
    }
    platform.registerWrapper(apicontract.metamodel.LicenseModel) {
      case s: apicontract.models.License => License(s)
    }
    platform.registerWrapper(apicontract.metamodel.OperationModel) {
      case s: apicontract.models.Operation => Operation(s)
    }
    platform.registerWrapper(apicontract.metamodel.OrganizationModel) {
      case s: apicontract.models.Organization => Organization(s)
    }
    platform.registerWrapper(apicontract.metamodel.ParameterModel) {
      case s: apicontract.models.Parameter => Parameter(s)
    }
    platform.registerWrapper(apicontract.metamodel.ServerModel) {
      case s: apicontract.models.Server => Server(s)
    }
    platform.registerWrapper(apicontract.metamodel.CallbackModel) {
      case s: apicontract.models.Callback => Callback(s)
    }
    platform.registerWrapper(apicontract.metamodel.EncodingModel) {
      case s: apicontract.models.Encoding => Encoding(s)
    }
    platform.registerWrapper(templates.ParametrizedResourceTypeModel) {
      case s: apicontract.models.templates.ParametrizedResourceType => ParametrizedResourceType(s)
    }
    platform.registerWrapper(apicontract.metamodel.TagModel) {
      case s: apicontract.models.Tag => Tag(s)
    }
    platform.registerWrapper(templates.ParametrizedTraitModel) {
      case s: apicontract.models.templates.ParametrizedTrait => ParametrizedTrait(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.ParametrizedSecuritySchemeModel) {
      case s: apicontract.models.security.ParametrizedSecurityScheme => ParametrizedSecurityScheme(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.SecurityRequirementModel) {
      case s: apicontract.models.security.SecurityRequirement => SecurityRequirement(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.SecuritySchemeModel) {
      case s: apicontract.models.security.SecurityScheme => SecurityScheme(s)
    }
    platform.registerWrapper(apicontract.metamodel.PayloadModel) {
      case s: apicontract.models.Payload => Payload(s)
    }
    platform.registerWrapper(apicontract.metamodel.RequestModel) {
      case s: apicontract.models.Request => Request(s)
    }
    platform.registerWrapper(apicontract.metamodel.ResponseModel) {
      case s: apicontract.models.Response => Response(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.ScopeModel) {
      case s: apicontract.models.security.Scope => Scope(s)
    }
    platform.registerWrapper(apicontract.metamodel.security.OAuth2FlowModel) {
      case of: apicontract.models.security.OAuth2Flow => OAuth2Flow(of)
    }
    platform.registerWrapper(apicontract.metamodel.security.SettingsModel) {
      case s: apicontract.models.security.Settings => new Settings(s)
    }
    platform.registerWrapper(apicontract.metamodel.api.WebApiModel) {
      case s: apicontract.models.api.WebApi => WebApi(s)
    }
    platform.registerWrapper(apicontract.metamodel.api.AsyncApiModel) {
      case s: apicontract.models.api.AsyncApi => AsyncApi(s)
    }
    platform.registerWrapper(apicontract.metamodel.templates.TraitModel) {
      case s: apicontract.models.templates.Trait => Trait(s)
    }
    platform.registerWrapper(apicontract.metamodel.templates.ResourceTypeModel) {
      case s: apicontract.models.templates.ResourceType => ResourceType(s)
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
      case s: apicontract.models.TemplatedLink => TemplatedLink(s)
    }
    platform.registerWrapper(IriTemplateMappingModel) {
      case s: apicontract.models.IriTemplateMapping => IriTemplateMapping(s)
    }
    platform.registerWrapper(CorrelationIdModel) {
      case s: apicontract.models.CorrelationId => CorrelationId(s)
    }
    platform.registerWrapper(Amqp091ChannelBindingModel) {
      case s: apicontract.models.bindings.amqp.Amqp091ChannelBinding => Amqp091ChannelBinding(s)
    }
    platform.registerWrapper(OperationBindingsModel) {
      case s: apicontract.models.bindings.OperationBindings => OperationBindings(s)
    }
    platform.registerWrapper(ServerBindingsModel) {
      case s: apicontract.models.bindings.ServerBindings => ServerBindings(s)
    }
    platform.registerWrapper(ChannelBindingsModel) {
      case s: apicontract.models.bindings.ChannelBindings => ChannelBindings(s)
    }
    platform.registerWrapper(MessageBindingsModel) {
      case s: apicontract.models.bindings.MessageBindings => MessageBindings(s)
    }
    platform.registerWrapper(Amqp091ChannelExchangeModel) {
      case s: apicontract.models.bindings.amqp.Amqp091ChannelExchange => Amqp091ChannelExchange(s)
    }
    platform.registerWrapper(Amqp091QueueModel) {
      case s: apicontract.models.bindings.amqp.Amqp091Queue => Amqp091Queue(s)
    }
    platform.registerWrapper(Amqp091MessageBindingModel) {
      case s: apicontract.models.bindings.amqp.Amqp091MessageBinding => Amqp091MessageBinding(s)
    }
    platform.registerWrapper(Amqp091OperationBindingModel) {
      case s: apicontract.models.bindings.amqp.Amqp091OperationBinding => Amqp091OperationBinding(s)
    }
    platform.registerWrapper(HttpMessageBindingModel) {
      case s: apicontract.models.bindings.http.HttpMessageBinding => HttpMessageBinding(s)
    }
    platform.registerWrapper(HttpOperationBindingModel) {
      case s: apicontract.models.bindings.http.HttpOperationBinding => HttpOperationBinding(s)
    }
    platform.registerWrapper(KafkaMessageBindingModel) {
      case s: apicontract.models.bindings.kafka.KafkaMessageBinding => KafkaMessageBinding(s)
    }
    platform.registerWrapper(KafkaOperationBindingModel) {
      case s: apicontract.models.bindings.kafka.KafkaOperationBinding => KafkaOperationBinding(s)
    }
    platform.registerWrapper(MqttMessageBindingModel) {
      case s: apicontract.models.bindings.mqtt.MqttMessageBinding => MqttMessageBinding(s)
    }
    platform.registerWrapper(MqttOperationBindingModel) {
      case s: apicontract.models.bindings.mqtt.MqttOperationBinding => MqttOperationBinding(s)
    }
    platform.registerWrapper(MqttServerBindingModel) {
      case s: apicontract.models.bindings.mqtt.MqttServerBinding => MqttServerBinding(s)
    }
    platform.registerWrapper(MqttServerLastWillModel) {
      case s: apicontract.models.bindings.mqtt.MqttServerLastWill => MqttServerLastWill(s)
    }
    platform.registerWrapper(WebSocketsChannelBindingModel) {
      case s: apicontract.models.bindings.websockets.WebSocketsChannelBinding => WebSocketsChannelBinding(s)
    }
    platform.registerWrapper(EmptyBindingModel) {
      case s: apicontract.models.bindings.EmptyBinding => EmptyBinding(s)
    }
  }

}
