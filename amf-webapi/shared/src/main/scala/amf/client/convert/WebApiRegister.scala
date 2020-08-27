package amf.client.convert

import amf.client.model.document._
import amf.client.model.domain._
import amf.core.metamodel.document.PayloadFragmentModel
import amf.core.remote.Platform
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi.model
import amf.plugins.domain.{shapes, webapi}
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.domain.webapi.metamodel.bindings._
import amf.plugins.domain.webapi.metamodel.{CorrelationIdModel, IriTemplateMappingModel, TemplatedLinkModel, templates}
import amf.plugins.features.validation.CoreValidations
import amf.validation.DialectValidations
import amf.validations.{ParserSideValidations, PayloadValidations, RenderSideValidations, ResolutionSideValidations}

/** Shared WebApi registrations. */
object WebApiRegister {

  def register(platform: Platform): Unit = {

    // Web Api (document)
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) {
      case s: model.AnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(s)
    }
    platform.registerWrapper(DataTypeFragmentModel) {
      case s: model.DataTypeFragment => DataType(s)
    }
    platform.registerWrapper(PayloadFragmentModel) {
      case s: amf.core.model.document.PayloadFragment => PayloadFragment(s)
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
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.ExtensionModel) {
      case m: model.Extension => Extension(m)
    }
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.OverlayModel) {
      case m: model.Overlay => Overlay(m)
    }

    // WebApi (domain)
    platform.registerWrapper(webapi.metamodel.EndPointModel) {
      case s: webapi.models.EndPoint => EndPoint(s)
    }
    platform.registerWrapper(webapi.metamodel.LicenseModel) {
      case s: webapi.models.License => License(s)
    }
    platform.registerWrapper(webapi.metamodel.OperationModel) {
      case s: webapi.models.Operation => Operation(s)
    }
    platform.registerWrapper(webapi.metamodel.OrganizationModel) {
      case s: webapi.models.Organization => Organization(s)
    }
    platform.registerWrapper(webapi.metamodel.ParameterModel) {
      case s: webapi.models.Parameter => Parameter(s)
    }
    platform.registerWrapper(webapi.metamodel.ServerModel) {
      case s: webapi.models.Server => Server(s)
    }
    platform.registerWrapper(webapi.metamodel.CallbackModel) {
      case s: webapi.models.Callback => Callback(s)
    }
    platform.registerWrapper(webapi.metamodel.EncodingModel) {
      case s: webapi.models.Encoding => Encoding(s)
    }
    platform.registerWrapper(templates.ParametrizedResourceTypeModel) {
      case s: webapi.models.templates.ParametrizedResourceType => ParametrizedResourceType(s)
    }
    platform.registerWrapper(webapi.metamodel.TagModel) {
      case s: webapi.models.Tag => Tag(s)
    }
    platform.registerWrapper(templates.ParametrizedTraitModel) {
      case s: webapi.models.templates.ParametrizedTrait => ParametrizedTrait(s)
    }
    platform.registerWrapper(webapi.metamodel.security.ParametrizedSecuritySchemeModel) {
      case s: webapi.models.security.ParametrizedSecurityScheme => ParametrizedSecurityScheme(s)
    }
    platform.registerWrapper(webapi.metamodel.security.SecurityRequirementModel) {
      case s: webapi.models.security.SecurityRequirement => SecurityRequirement(s)
    }
    platform.registerWrapper(webapi.metamodel.security.SecuritySchemeModel) {
      case s: webapi.models.security.SecurityScheme => SecurityScheme(s)
    }
    platform.registerWrapper(webapi.metamodel.PayloadModel) {
      case s: webapi.models.Payload => Payload(s)
    }
    platform.registerWrapper(webapi.metamodel.RequestModel) {
      case s: webapi.models.Request => Request(s)
    }
    platform.registerWrapper(webapi.metamodel.ResponseModel) {
      case s: webapi.models.Response => Response(s)
    }
    platform.registerWrapper(webapi.metamodel.security.ScopeModel) {
      case s: webapi.models.security.Scope => Scope(s)
    }
    platform.registerWrapper(webapi.metamodel.security.OAuth2FlowModel) {
      case of: webapi.models.security.OAuth2Flow => OAuth2Flow(of)
    }
    platform.registerWrapper(webapi.metamodel.security.SettingsModel) {
      case s: webapi.models.security.Settings => new Settings(s)
    }
    platform.registerWrapper(webapi.metamodel.WebApiModel) {
      case s: webapi.models.WebApi => WebApi(s)
    }
    platform.registerWrapper(webapi.metamodel.templates.TraitModel) {
      case s: webapi.models.templates.Trait => Trait(s)
    }
    platform.registerWrapper(webapi.metamodel.templates.ResourceTypeModel) {
      case s: webapi.models.templates.ResourceType => ResourceType(s)
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
    platform.registerWrapper(shapes.metamodel.UnionShapeModel) {
      case s: shapes.models.UnionShape => UnionShape(s)
    }
    platform.registerWrapper(amf.core.metamodel.domain.RecursiveShapeModel) {
      case s: amf.core.model.domain.RecursiveShape => RecursiveShape(s)
    }
    platform.registerWrapper(TemplatedLinkModel) {
      case s: webapi.models.TemplatedLink => TemplatedLink(s)
    }
    platform.registerWrapper(IriTemplateMappingModel) {
      case s: webapi.models.IriTemplateMapping => IriTemplateMapping(s)
    }
    platform.registerWrapper(CorrelationIdModel) {
      case s: webapi.models.CorrelationId => CorrelationId(s)
    }
    platform.registerWrapper(Amqp091ChannelBindingModel) {
      case s: webapi.models.bindings.amqp.Amqp091ChannelBinding => Amqp091ChannelBinding(s)
    }
    platform.registerWrapper(OperationBindingsModel) {
      case s: webapi.models.bindings.OperationBindings => OperationBindings(s)
    }
    platform.registerWrapper(ServerBindingsModel) {
      case s: webapi.models.bindings.ServerBindings => ServerBindings(s)
    }
    platform.registerWrapper(ChannelBindingsModel) {
      case s: webapi.models.bindings.ChannelBindings => ChannelBindings(s)
    }
    platform.registerWrapper(MessageBindingsModel) {
      case s: webapi.models.bindings.MessageBindings => MessageBindings(s)
    }
    platform.registerWrapper(Amqp091ChannelExchangeModel) {
      case s: webapi.models.bindings.amqp.Amqp091ChannelExchange => Amqp091ChannelExchange(s)
    }
    platform.registerWrapper(Amqp091QueueModel) {
      case s: webapi.models.bindings.amqp.Amqp091Queue => Amqp091Queue(s)
    }
    platform.registerWrapper(Amqp091MessageBindingModel) {
      case s: webapi.models.bindings.amqp.Amqp091MessageBinding => Amqp091MessageBinding(s)
    }
    platform.registerWrapper(Amqp091OperationBindingModel) {
      case s: webapi.models.bindings.amqp.Amqp091OperationBinding => Amqp091OperationBinding(s)
    }
    platform.registerWrapper(HttpMessageBindingModel) {
      case s: webapi.models.bindings.http.HttpMessageBinding => HttpMessageBinding(s)
    }
    platform.registerWrapper(HttpOperationBindingModel) {
      case s: webapi.models.bindings.http.HttpOperationBinding => HttpOperationBinding(s)
    }
    platform.registerWrapper(KafkaMessageBindingModel) {
      case s: webapi.models.bindings.kafka.KafkaMessageBinding => KafkaMessageBinding(s)
    }
    platform.registerWrapper(KafkaOperationBindingModel) {
      case s: webapi.models.bindings.kafka.KafkaOperationBinding => KafkaOperationBinding(s)
    }
    platform.registerWrapper(MqttMessageBindingModel) {
      case s: webapi.models.bindings.mqtt.MqttMessageBinding => MqttMessageBinding(s)
    }
    platform.registerWrapper(MqttOperationBindingModel) {
      case s: webapi.models.bindings.mqtt.MqttOperationBinding => MqttOperationBinding(s)
    }
    platform.registerWrapper(MqttServerBindingModel) {
      case s: webapi.models.bindings.mqtt.MqttServerBinding => MqttServerBinding(s)
    }
    platform.registerWrapper(MqttServerLastWillModel) {
      case s: webapi.models.bindings.mqtt.MqttServerLastWill => MqttServerLastWill(s)
    }
    platform.registerWrapper(WebSocketsChannelBindingModel) {
      case s: webapi.models.bindings.websockets.WebSocketsChannelBinding => WebSocketsChannelBinding(s)
    }
    platform.registerWrapper(EmptyBindingModel) {
      case s: webapi.models.bindings.EmptyBinding => EmptyBinding(s)
    }

    platform.registerValidations(CoreValidations.validations, CoreValidations.levels)
    platform.registerValidations(DialectValidations.validations, DialectValidations.levels)
    platform.registerValidations(ParserSideValidations.validations, ParserSideValidations.levels)
    platform.registerValidations(PayloadValidations.validations, PayloadValidations.levels)
    platform.registerValidations(RenderSideValidations.validations, RenderSideValidations.levels)
    platform.registerValidations(ResolutionSideValidations.validations, ResolutionSideValidations.levels)

    amf.Core.registerPlugin(WebAPIDomainPlugin)
  }

}
