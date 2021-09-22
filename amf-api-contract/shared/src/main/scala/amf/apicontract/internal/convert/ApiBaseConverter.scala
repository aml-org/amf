package amf.apicontract.internal.convert

import amf.apicontract.client.platform
import amf.apicontract.client.platform.model.domain
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.client.scala.model.domain.{
  Callback,
  CorrelationId,
  Encoding,
  EndPoint,
  License,
  Message,
  Operation,
  Organization,
  Parameter,
  Payload,
  Request,
  Response,
  Server,
  Tag,
  TemplatedLink
}
import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091MessageBinding,
  Amqp091OperationBinding,
  Amqp091Queue
}
import amf.apicontract.client.scala.model.domain.bindings.http.{HttpMessageBinding, HttpOperationBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{KafkaMessageBinding, KafkaOperationBinding}
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttMessageBinding,
  MqttOperationBinding,
  MqttServerBinding,
  MqttServerLastWill
}
import amf.apicontract.client.scala.model.domain.bindings.websockets.WebSocketsChannelBinding
import amf.apicontract.client.scala.model.domain.bindings.{
  ChannelBinding,
  ChannelBindings,
  EmptyBinding,
  MessageBinding,
  MessageBindings,
  OperationBinding,
  OperationBindings,
  ServerBinding,
  ServerBindings
}
import amf.apicontract.client.scala.model.domain.security.{
  ApiKeySettings,
  HttpApiKeySettings,
  HttpSettings,
  OAuth1Settings,
  OAuth2Flow,
  OAuth2Settings,
  OpenIdConnectSettings,
  ParametrizedSecurityScheme,
  Scope,
  SecurityRequirement,
  SecurityScheme,
  Settings
}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.scala.{AMFConfiguration, AMFDocumentResult, AMFLibraryResult}
import amf.core.internal.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.convert.ShapesBaseConverter

trait ApiBaseConverter
    extends CoreBaseConverter
    with ShapesBaseConverter
    with EndPointConverter
    with ResourceTypeConverter
    with TraitConverter
    with OrganizationConverter
    with LicenseConverter
    with MessageConverter
    with OperationConverter
    with TagConverter
    with ParameterConverter
    with PayloadConverter
    with ParametrizedSecuritySchemeConverter
    with SecuritySchemeConverter
    with SettingsConverter
    with ScopeConverter
    with ServerConverter
    with TemplatedLinkConverter
    with CallbackConverter
    with EncodingConverter
    with OAuth2FlowConverter
    with SecurityRequirementConverter
    with CorrelationIdConverter
    with ChannelBindingsConverter
    with OperationBindingsConverter
    with ServerBindingsConverter
    with MessageBindingsConverter
    with Amqp091ChannelBindingConverter
    with Amqp091MessageBindingConverter
    with Amqp091OperationBindingConverter
    with EmptyBindingConverter
    with HttpMessageBindingConverter
    with HttpOperationBindingConverter
    with KafkaMessageBindingConverter
    with KafkaOperationBindingConverter
    with MqttMessageBindingConverter
    with MqttOperationBindingConverter
    with MqttServerBindingConverter
    with WebSocketsChannelBindingConverter
    with MqttServerLastWillConverter
    with Amqp091ChannelExchangeConverter
    with Amqp091QueueConverter
    with ChannelBindingConverter
    with OperationBindingConverter
    with MessageBindingConverter
    with ServerBindingConverter
    with AMFConfigurationConverter
    with AMFDocumentResultConverter
    with AMFLibraryResultConverter
    with APIContractProcessingDataConverter

trait ChannelBindingConverter extends PlatformSecrets {
  implicit object ChannelBindingMatcher extends BidirectionalMatcher[ChannelBinding, domain.bindings.ChannelBinding] {
    override def asClient(from: ChannelBinding): domain.bindings.ChannelBinding =
      platform.wrap[domain.bindings.ChannelBinding](from)
    override def asInternal(from: domain.bindings.ChannelBinding): ChannelBinding = from._internal
  }
}
trait ChannelBindingsConverter extends PlatformSecrets {
  implicit object ChannelBindingsMatcher
      extends BidirectionalMatcher[ChannelBindings, domain.bindings.ChannelBindings] {
    override def asClient(from: ChannelBindings): domain.bindings.ChannelBindings =
      platform.wrap[domain.bindings.ChannelBindings](from)
    override def asInternal(from: domain.bindings.ChannelBindings): ChannelBindings = from._internal
  }
}
trait OperationBindingsConverter extends PlatformSecrets {
  implicit object OperationBindingsMatcher
      extends BidirectionalMatcher[OperationBindings, domain.bindings.OperationBindings] {
    override def asClient(from: OperationBindings): domain.bindings.OperationBindings =
      platform.wrap[domain.bindings.OperationBindings](from)
    override def asInternal(from: domain.bindings.OperationBindings): OperationBindings = from._internal
  }
}
trait MessageBindingsConverter extends PlatformSecrets {
  implicit object MessageBindingsMatcher
      extends BidirectionalMatcher[MessageBindings, domain.bindings.MessageBindings] {
    override def asClient(from: MessageBindings): domain.bindings.MessageBindings =
      platform.wrap[domain.bindings.MessageBindings](from)
    override def asInternal(from: domain.bindings.MessageBindings): MessageBindings = from._internal
  }
}
trait ServerBindingsConverter extends PlatformSecrets {
  implicit object ServerBindingsMatcher extends BidirectionalMatcher[ServerBindings, domain.bindings.ServerBindings] {
    override def asClient(from: ServerBindings): domain.bindings.ServerBindings =
      platform.wrap[domain.bindings.ServerBindings](from)
    override def asInternal(from: domain.bindings.ServerBindings): ServerBindings = from._internal
  }
}
trait OperationBindingConverter extends PlatformSecrets {
  implicit object OperationBindingMatcher
      extends BidirectionalMatcher[OperationBinding, domain.bindings.OperationBinding] {
    override def asClient(from: OperationBinding): domain.bindings.OperationBinding =
      platform.wrap[domain.bindings.OperationBinding](from)
    override def asInternal(from: domain.bindings.OperationBinding): OperationBinding = from._internal
  }
}
trait MessageBindingConverter extends PlatformSecrets {
  implicit object MessageBindingMatcher extends BidirectionalMatcher[MessageBinding, domain.bindings.MessageBinding] {
    override def asClient(from: MessageBinding): domain.bindings.MessageBinding =
      platform.wrap[domain.bindings.MessageBinding](from)
    override def asInternal(from: domain.bindings.MessageBinding): MessageBinding = from._internal
  }
}
trait ServerBindingConverter extends PlatformSecrets {
  implicit object ServerBindingMatcher extends BidirectionalMatcher[ServerBinding, domain.bindings.ServerBinding] {
    override def asClient(from: ServerBinding): domain.bindings.ServerBinding =
      platform.wrap[domain.bindings.ServerBinding](from)
    override def asInternal(from: domain.bindings.ServerBinding): ServerBinding = from._internal
  }
}

trait Amqp091ChannelBindingConverter extends PlatformSecrets {
  implicit object Amqp091ChannelBindingMatcher
      extends BidirectionalMatcher[Amqp091ChannelBinding, domain.bindings.amqp.Amqp091ChannelBinding] {
    override def asClient(from: Amqp091ChannelBinding): domain.bindings.amqp.Amqp091ChannelBinding =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelBinding): Amqp091ChannelBinding = from._internal
  }
}
trait Amqp091MessageBindingConverter extends PlatformSecrets {
  implicit object Amqp091MessageBindingMatcher
      extends BidirectionalMatcher[Amqp091MessageBinding, domain.bindings.amqp.Amqp091MessageBinding] {
    override def asClient(from: Amqp091MessageBinding): domain.bindings.amqp.Amqp091MessageBinding =
      platform.wrap[domain.bindings.amqp.Amqp091MessageBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091MessageBinding): Amqp091MessageBinding = from._internal
  }
}
trait Amqp091OperationBindingConverter extends PlatformSecrets {
  implicit object Amqp091OperationBindingMatcher
      extends BidirectionalMatcher[Amqp091OperationBinding, domain.bindings.amqp.Amqp091OperationBinding] {
    override def asClient(from: Amqp091OperationBinding): domain.bindings.amqp.Amqp091OperationBinding =
      platform.wrap[domain.bindings.amqp.Amqp091OperationBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091OperationBinding): Amqp091OperationBinding =
      from._internal
  }
}
trait EmptyBindingConverter extends PlatformSecrets {
  implicit object EmptyBindingMatcher extends BidirectionalMatcher[EmptyBinding, domain.bindings.EmptyBinding] {
    override def asClient(from: EmptyBinding): domain.bindings.EmptyBinding =
      platform.wrap[domain.bindings.EmptyBinding](from)
    override def asInternal(from: domain.bindings.EmptyBinding): EmptyBinding = from._internal
  }
}
trait HttpMessageBindingConverter extends PlatformSecrets {
  implicit object HttpMessageBindingMatcher
      extends BidirectionalMatcher[HttpMessageBinding, domain.bindings.http.HttpMessageBinding] {
    override def asClient(from: HttpMessageBinding): domain.bindings.http.HttpMessageBinding =
      platform.wrap[domain.bindings.http.HttpMessageBinding](from)
    override def asInternal(from: domain.bindings.http.HttpMessageBinding): HttpMessageBinding = from._internal
  }
}
trait HttpOperationBindingConverter extends PlatformSecrets {
  implicit object HttpOperationBindingMatcher
      extends BidirectionalMatcher[HttpOperationBinding, domain.bindings.http.HttpOperationBinding] {
    override def asClient(from: HttpOperationBinding): domain.bindings.http.HttpOperationBinding =
      platform.wrap[domain.bindings.http.HttpOperationBinding](from)
    override def asInternal(from: domain.bindings.http.HttpOperationBinding): HttpOperationBinding = from._internal
  }
}
trait KafkaMessageBindingConverter extends PlatformSecrets {
  implicit object KafkaMessageBindingMatcher
      extends BidirectionalMatcher[KafkaMessageBinding, domain.bindings.kafka.KafkaMessageBinding] {
    override def asClient(from: KafkaMessageBinding): domain.bindings.kafka.KafkaMessageBinding =
      platform.wrap[domain.bindings.kafka.KafkaMessageBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaMessageBinding): KafkaMessageBinding = from._internal
  }
}
trait KafkaOperationBindingConverter extends PlatformSecrets {
  implicit object KafkaOperationBindingMatcher
      extends BidirectionalMatcher[KafkaOperationBinding, domain.bindings.kafka.KafkaOperationBinding] {
    override def asClient(from: KafkaOperationBinding): domain.bindings.kafka.KafkaOperationBinding =
      platform.wrap[domain.bindings.kafka.KafkaOperationBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaOperationBinding): KafkaOperationBinding = from._internal
  }
}
trait MqttMessageBindingConverter extends PlatformSecrets {
  implicit object MqttMessageBindingMatcher
      extends BidirectionalMatcher[MqttMessageBinding, domain.bindings.mqtt.MqttMessageBinding] {
    override def asClient(from: MqttMessageBinding): domain.bindings.mqtt.MqttMessageBinding =
      platform.wrap[domain.bindings.mqtt.MqttMessageBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttMessageBinding): MqttMessageBinding = from._internal
  }
}
trait MqttOperationBindingConverter extends PlatformSecrets {
  implicit object MqttOperationBindingMatcher
      extends BidirectionalMatcher[MqttOperationBinding, domain.bindings.mqtt.MqttOperationBinding] {
    override def asClient(from: MqttOperationBinding): domain.bindings.mqtt.MqttOperationBinding =
      platform.wrap[domain.bindings.mqtt.MqttOperationBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttOperationBinding): MqttOperationBinding = from._internal
  }
}
trait MqttServerBindingConverter extends PlatformSecrets {
  implicit object MqttServerBindingMatcher
      extends BidirectionalMatcher[MqttServerBinding, domain.bindings.mqtt.MqttServerBinding] {
    override def asClient(from: MqttServerBinding): domain.bindings.mqtt.MqttServerBinding =
      platform.wrap[domain.bindings.mqtt.MqttServerBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerBinding): MqttServerBinding = from._internal
  }
}
trait WebSocketsChannelBindingConverter extends PlatformSecrets {
  implicit object WebSocketsChannelBindingMatcher
      extends BidirectionalMatcher[WebSocketsChannelBinding, domain.bindings.websockets.WebSocketsChannelBinding] {
    override def asClient(from: WebSocketsChannelBinding): domain.bindings.websockets.WebSocketsChannelBinding =
      platform.wrap[domain.bindings.websockets.WebSocketsChannelBinding](from)
    override def asInternal(from: domain.bindings.websockets.WebSocketsChannelBinding): WebSocketsChannelBinding =
      from._internal
  }
}

trait MqttServerLastWillConverter extends PlatformSecrets {
  implicit object MqttServerLastWillMatcher
      extends BidirectionalMatcher[MqttServerLastWill, domain.bindings.mqtt.MqttServerLastWill] {
    override def asClient(from: MqttServerLastWill): domain.bindings.mqtt.MqttServerLastWill =
      platform.wrap[domain.bindings.mqtt.MqttServerLastWill](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerLastWill): MqttServerLastWill = from._internal
  }
}

trait Amqp091ChannelExchangeConverter extends PlatformSecrets {
  implicit object Amqp091ChannelExchangeMatcher
      extends BidirectionalMatcher[Amqp091ChannelExchange, domain.bindings.amqp.Amqp091ChannelExchange] {
    override def asClient(from: Amqp091ChannelExchange): domain.bindings.amqp.Amqp091ChannelExchange =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelExchange](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelExchange): Amqp091ChannelExchange = from._internal
  }
}

trait Amqp091QueueConverter extends PlatformSecrets {
  implicit object Amqp091QueueMatcher extends BidirectionalMatcher[Amqp091Queue, domain.bindings.amqp.Amqp091Queue] {
    override def asClient(from: Amqp091Queue): domain.bindings.amqp.Amqp091Queue =
      platform.wrap[domain.bindings.amqp.Amqp091Queue](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091Queue): Amqp091Queue = from._internal
  }
}

trait EndPointConverter extends PlatformSecrets {

  implicit object EndPointMatcher extends BidirectionalMatcher[EndPoint, domain.EndPoint] {
    override def asClient(from: EndPoint): domain.EndPoint   = platform.wrap[domain.EndPoint](from)
    override def asInternal(from: domain.EndPoint): EndPoint = from._internal
  }
}

trait ResourceTypeConverter extends PlatformSecrets {

  implicit object ResourceTypeMatcher extends BidirectionalMatcher[ResourceType, domain.templates.ResourceType] {
    override def asClient(from: ResourceType): domain.templates.ResourceType   = domain.templates.ResourceType(from)
    override def asInternal(from: domain.templates.ResourceType): ResourceType = from._internal
  }
}

trait TraitConverter extends PlatformSecrets {

  implicit object TraitMatcher extends BidirectionalMatcher[Trait, domain.templates.Trait] {
    override def asClient(from: Trait): domain.templates.Trait   = domain.templates.Trait(from)
    override def asInternal(from: domain.templates.Trait): Trait = from._internal
  }
}

trait OrganizationConverter extends PlatformSecrets {

  implicit object OrganizationMatcher extends BidirectionalMatcher[Organization, domain.Organization] {
    override def asClient(from: Organization): domain.Organization   = platform.wrap[domain.Organization](from)
    override def asInternal(from: domain.Organization): Organization = from._internal
  }
}

trait LicenseConverter extends PlatformSecrets {

  implicit object LicenseMatcher extends BidirectionalMatcher[License, domain.License] {
    override def asClient(from: License): domain.License   = platform.wrap[domain.License](from)
    override def asInternal(from: domain.License): License = from._internal
  }
}

trait ParameterConverter extends PlatformSecrets {

  implicit object ParameterMatcher extends BidirectionalMatcher[Parameter, domain.Parameter] {
    override def asClient(from: Parameter): domain.Parameter   = platform.wrap[domain.Parameter](from)
    override def asInternal(from: domain.Parameter): Parameter = from._internal
  }
}

trait ServerConverter extends PlatformSecrets {

  implicit object ServerMatcher extends BidirectionalMatcher[Server, domain.Server] {
    override def asClient(from: Server): domain.Server   = platform.wrap[domain.Server](from)
    override def asInternal(from: domain.Server): Server = from._internal
  }
}

trait CallbackConverter extends PlatformSecrets {

  implicit object CallbackMatcher extends BidirectionalMatcher[Callback, domain.Callback] {
    override def asClient(from: Callback): domain.Callback   = platform.wrap[domain.Callback](from)
    override def asInternal(from: domain.Callback): Callback = from._internal
  }
}

trait EncodingConverter extends PlatformSecrets {

  implicit object EncodingMatcher extends BidirectionalMatcher[Encoding, domain.Encoding] {
    override def asClient(from: Encoding): domain.Encoding   = platform.wrap[domain.Encoding](from)
    override def asInternal(from: domain.Encoding): Encoding = from._internal
  }
}

trait PayloadConverter extends PlatformSecrets {

  implicit object PayloadMatcher extends BidirectionalMatcher[Payload, domain.Payload] {
    override def asClient(from: Payload): domain.Payload   = platform.wrap[domain.Payload](from)
    override def asInternal(from: domain.Payload): Payload = from._internal
  }
}

trait OperationConverter extends PlatformSecrets {

  implicit object OperationMatcher extends BidirectionalMatcher[Operation, domain.Operation] {
    override def asClient(from: Operation): domain.Operation   = platform.wrap[domain.Operation](from)
    override def asInternal(from: domain.Operation): Operation = from._internal
  }

}

trait TagConverter extends PlatformSecrets {

  implicit object TagMatcher extends BidirectionalMatcher[Tag, domain.Tag] {
    override def asClient(from: Tag): domain.Tag   = platform.wrap[domain.Tag](from)
    override def asInternal(from: domain.Tag): Tag = from._internal
  }

}

trait CorrelationIdConverter extends PlatformSecrets {

  implicit object CorrelationIdMatcher extends BidirectionalMatcher[CorrelationId, domain.CorrelationId] {
    override def asClient(from: CorrelationId): domain.CorrelationId   = platform.wrap[domain.CorrelationId](from)
    override def asInternal(from: domain.CorrelationId): CorrelationId = from._internal
  }

}

trait MessageConverter extends PlatformSecrets {

  implicit object ResponseMatcher extends BidirectionalMatcher[Response, domain.Response] {
    override def asClient(from: Response): domain.Response   = platform.wrap[domain.Response](from)
    override def asInternal(from: domain.Response): Response = from._internal
  }

  implicit object RequestMatcher extends BidirectionalMatcher[Request, domain.Request] {
    override def asClient(from: Request): domain.Request   = platform.wrap[domain.Request](from)
    override def asInternal(from: domain.Request): Request = from._internal
  }

  implicit object MessageMatcher extends BidirectionalMatcher[Message, domain.Message] {
    override def asClient(from: Message): domain.Message = from match {
      case req: Request  => RequestMatcher.asClient(req)
      case res: Response => ResponseMatcher.asClient(res)
      case base: Message => new domain.Message(base)
      case _ => // noinspection ScalaStyle
        null
    }
    override def asInternal(from: domain.Message): Message = from._internal
  }
}

trait ParametrizedSecuritySchemeConverter extends PlatformSecrets {

  implicit object ParametrizedSecuritySchemeMatcher
      extends BidirectionalMatcher[ParametrizedSecurityScheme, domain.security.ParametrizedSecurityScheme] {
    override def asClient(from: ParametrizedSecurityScheme): domain.security.ParametrizedSecurityScheme =
      platform.wrap[domain.security.ParametrizedSecurityScheme](from)

    override def asInternal(from: domain.security.ParametrizedSecurityScheme): ParametrizedSecurityScheme =
      from._internal
  }
}

trait SecuritySchemeConverter extends PlatformSecrets {

  implicit object SecuritySchemeMatcher extends BidirectionalMatcher[SecurityScheme, domain.security.SecurityScheme] {
    override def asClient(from: SecurityScheme): domain.security.SecurityScheme =
      platform.wrap[domain.security.SecurityScheme](from)
    override def asInternal(from: domain.security.SecurityScheme): SecurityScheme = from._internal
  }
}

trait OAuth2FlowConverter extends PlatformSecrets {

  implicit object OAuth2FlowMatcher extends BidirectionalMatcher[OAuth2Flow, domain.security.OAuth2Flow] {
    override def asClient(from: OAuth2Flow): domain.security.OAuth2Flow =
      platform.wrap[domain.security.OAuth2Flow](from)
    override def asInternal(from: domain.security.OAuth2Flow): OAuth2Flow = from._internal
  }
}

trait SecurityRequirementConverter extends PlatformSecrets {

  implicit object SecurityRequirementMatcher
      extends BidirectionalMatcher[SecurityRequirement, domain.security.SecurityRequirement] {
    override def asClient(from: SecurityRequirement): domain.security.SecurityRequirement =
      platform.wrap[domain.security.SecurityRequirement](from)
    override def asInternal(from: domain.security.SecurityRequirement): SecurityRequirement = from._internal
  }
}

trait SettingsConverter extends PlatformSecrets {

  implicit object OAuth1SettingsMatcher extends BidirectionalMatcher[OAuth1Settings, domain.security.OAuth1Settings] {
    override def asClient(from: OAuth1Settings): domain.security.OAuth1Settings   = domain.security.OAuth1Settings(from)
    override def asInternal(from: domain.security.OAuth1Settings): OAuth1Settings = from._internal
  }

  implicit object OAuth2SettingsMatcher extends BidirectionalMatcher[OAuth2Settings, domain.security.OAuth2Settings] {
    override def asClient(from: OAuth2Settings): domain.security.OAuth2Settings   = domain.security.OAuth2Settings(from)
    override def asInternal(from: domain.security.OAuth2Settings): OAuth2Settings = from._internal
  }

  implicit object ApiKeySettingsMatcher extends BidirectionalMatcher[ApiKeySettings, domain.security.ApiKeySettings] {
    override def asClient(from: ApiKeySettings): domain.security.ApiKeySettings   = domain.security.ApiKeySettings(from)
    override def asInternal(from: domain.security.ApiKeySettings): ApiKeySettings = from._internal
  }

  implicit object HttpApiKeySettingsMatcher
      extends BidirectionalMatcher[HttpApiKeySettings, domain.security.HttpApiKeySettings] {
    override def asClient(from: HttpApiKeySettings): domain.security.HttpApiKeySettings =
      domain.security.HttpApiKeySettings(from)
    override def asInternal(from: domain.security.HttpApiKeySettings): HttpApiKeySettings = from._internal
  }

  implicit object HttpSettingsMatcher extends BidirectionalMatcher[HttpSettings, domain.security.HttpSettings] {
    override def asClient(from: HttpSettings): domain.security.HttpSettings   = domain.security.HttpSettings(from)
    override def asInternal(from: domain.security.HttpSettings): HttpSettings = from._internal
  }
  implicit object OpenIdConnectSettingsMatcher
      extends BidirectionalMatcher[OpenIdConnectSettings, domain.security.OpenIdConnectSettings] {
    override def asClient(from: OpenIdConnectSettings): domain.security.OpenIdConnectSettings =
      domain.security.OpenIdConnectSettings(from)
    override def asInternal(from: domain.security.OpenIdConnectSettings): OpenIdConnectSettings = from._internal
  }

  implicit object SettingsMatcher extends BidirectionalMatcher[Settings, domain.security.Settings] {
    override def asClient(from: Settings): domain.security.Settings = from match {
      case oauth1: OAuth1Settings        => OAuth1SettingsMatcher.asClient(oauth1)
      case oauth2: OAuth2Settings        => OAuth2SettingsMatcher.asClient(oauth2)
      case apiKey: ApiKeySettings        => ApiKeySettingsMatcher.asClient(apiKey)
      case http: HttpSettings            => HttpSettingsMatcher.asClient(http)
      case openId: OpenIdConnectSettings => OpenIdConnectSettingsMatcher.asClient(openId)
      case base: Settings                => new domain.security.Settings(base)
      case _ => // noinspection ScalaStyle
        null
    }

    override def asInternal(from: domain.security.Settings): Settings = from._internal
  }
}

trait ScopeConverter extends PlatformSecrets {

  implicit object ScopeMatcher extends BidirectionalMatcher[Scope, domain.security.Scope] {
    override def asClient(from: Scope): domain.security.Scope   = platform.wrap[domain.security.Scope](from)
    override def asInternal(from: domain.security.Scope): Scope = from._internal
  }
}

trait TemplatedLinkConverter extends PlatformSecrets {

  implicit object TemplatedLinkConverter extends BidirectionalMatcher[TemplatedLink, domain.TemplatedLink] {
    override def asClient(from: TemplatedLink): domain.TemplatedLink   = platform.wrap[domain.TemplatedLink](from)
    override def asInternal(from: domain.TemplatedLink): TemplatedLink = from._internal
  }
}

trait AMFConfigurationConverter {
  implicit object AMFConfigurationMatcher extends BidirectionalMatcher[AMFConfiguration, platform.AMFConfiguration] {
    override def asClient(from: AMFConfiguration): platform.AMFConfiguration   = new platform.AMFConfiguration(from)
    override def asInternal(from: platform.AMFConfiguration): AMFConfiguration = from._internal
  }
}

trait AMFLibraryResultConverter {
  implicit object AMFLibraryResultMatcher extends BidirectionalMatcher[AMFLibraryResult, platform.AMFLibraryResult] {
    override def asClient(from: AMFLibraryResult): platform.AMFLibraryResult   = new platform.AMFLibraryResult(from)
    override def asInternal(from: platform.AMFLibraryResult): AMFLibraryResult = from._internal
  }
}

trait AMFDocumentResultConverter {
  implicit object AMFDocumentResultMatcher
      extends BidirectionalMatcher[AMFDocumentResult, platform.AMFDocumentResult] {
    override def asClient(from: AMFDocumentResult): platform.AMFDocumentResult   = new platform.AMFDocumentResult(from)
    override def asInternal(from: platform.AMFDocumentResult): AMFDocumentResult = from._internal
  }
}

trait APIContractProcessingDataConverter {
  implicit object APIContractProcessingDataMatcher
      extends BidirectionalMatcher[APIContractProcessingData, platform.model.document.APIContractProcessingData] {
    override def asClient(from: APIContractProcessingData): platform.model.document.APIContractProcessingData =
      new platform.model.document.APIContractProcessingData(from)
    override def asInternal(from: platform.model.document.APIContractProcessingData): APIContractProcessingData =
      from._internal
  }
}
