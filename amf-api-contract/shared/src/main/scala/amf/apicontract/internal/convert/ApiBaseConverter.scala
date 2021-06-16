package amf.apicontract.internal.convert

import amf.apicontract.client.platform
import amf.apicontract.client.platform.config
import amf.apicontract.client.platform.model.domain
import amf.apicontract.client.scala.config.AMFConfiguration
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
import amf.apicontract.client.scala.{AMFDocumentResult, AMFLibraryResult}
import amf.core.internal.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.core.internal.unsafe.PlatformSecrets
import amf.plugins.domain.apicontract.models._
import amf.apicontract.client.scala.model.domain.bindings._
import amf.apicontract.client.scala.model.domain.bindings.amqp._
import amf.apicontract.client.scala.model.domain.bindings.http._
import amf.apicontract.client.scala.model.domain.bindings.kafka._
import amf.apicontract.client.scala.model.domain.bindings.mqtt._
import amf.apicontract.client.scala.model.domain.bindings.websockets._
import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}

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

trait ChannelBindingConverter extends PlatformSecrets {
  implicit object ChannelBindingMatcher extends BidirectionalMatcher[ChannelBinding, domain.ChannelBinding] {
    override def asClient(from: ChannelBinding): domain.ChannelBinding =
      platform.wrap[domain.ChannelBinding](from)
    override def asInternal(from: domain.ChannelBinding): ChannelBinding = from._internal
  }
}
trait ChannelBindingsConverter extends PlatformSecrets {
  implicit object ChannelBindingsMatcher extends BidirectionalMatcher[ChannelBindings, domain.ChannelBindings] {
    override def asClient(from: ChannelBindings): domain.ChannelBindings =
      platform.wrap[domain.ChannelBindings](from)
    override def asInternal(from: domain.ChannelBindings): ChannelBindings = from._internal
  }
}
trait OperationBindingsConverter extends PlatformSecrets {
  implicit object OperationBindingsMatcher extends BidirectionalMatcher[OperationBindings, domain.OperationBindings] {
    override def asClient(from: OperationBindings): domain.OperationBindings =
      platform.wrap[domain.OperationBindings](from)
    override def asInternal(from: domain.OperationBindings): OperationBindings = from._internal
  }
}
trait MessageBindingsConverter extends PlatformSecrets {
  implicit object MessageBindingsMatcher extends BidirectionalMatcher[MessageBindings, domain.MessageBindings] {
    override def asClient(from: MessageBindings): domain.MessageBindings =
      platform.wrap[domain.MessageBindings](from)
    override def asInternal(from: domain.MessageBindings): MessageBindings = from._internal
  }
}
trait ServerBindingsConverter extends PlatformSecrets {
  implicit object ServerBindingsMatcher extends BidirectionalMatcher[ServerBindings, domain.ServerBindings] {
    override def asClient(from: ServerBindings): domain.ServerBindings =
      platform.wrap[domain.ServerBindings](from)
    override def asInternal(from: domain.ServerBindings): ServerBindings = from._internal
  }
}
trait OperationBindingConverter extends PlatformSecrets {
  implicit object OperationBindingMatcher extends BidirectionalMatcher[OperationBinding, domain.OperationBinding] {
    override def asClient(from: OperationBinding): domain.OperationBinding =
      platform.wrap[domain.OperationBinding](from)
    override def asInternal(from: domain.OperationBinding): OperationBinding = from._internal
  }
}
trait MessageBindingConverter extends PlatformSecrets {
  implicit object MessageBindingMatcher extends BidirectionalMatcher[MessageBinding, domain.MessageBinding] {
    override def asClient(from: MessageBinding): domain.MessageBinding =
      platform.wrap[domain.MessageBinding](from)
    override def asInternal(from: domain.MessageBinding): MessageBinding = from._internal
  }
}
trait ServerBindingConverter extends PlatformSecrets {
  implicit object ServerBindingMatcher extends BidirectionalMatcher[ServerBinding, domain.ServerBinding] {
    override def asClient(from: ServerBinding): domain.ServerBinding =
      platform.wrap[domain.ServerBinding](from)
    override def asInternal(from: domain.ServerBinding): ServerBinding = from._internal
  }
}

trait Amqp091ChannelBindingConverter extends PlatformSecrets {
  implicit object Amqp091ChannelBindingMatcher
      extends BidirectionalMatcher[Amqp091ChannelBinding, domain.Amqp091ChannelBinding] {
    override def asClient(from: Amqp091ChannelBinding): domain.Amqp091ChannelBinding =
      platform.wrap[domain.Amqp091ChannelBinding](from)
    override def asInternal(from: domain.Amqp091ChannelBinding): Amqp091ChannelBinding = from._internal
  }
}
trait Amqp091MessageBindingConverter extends PlatformSecrets {
  implicit object Amqp091MessageBindingMatcher
      extends BidirectionalMatcher[Amqp091MessageBinding, domain.Amqp091MessageBinding] {
    override def asClient(from: Amqp091MessageBinding): domain.Amqp091MessageBinding =
      platform.wrap[domain.Amqp091MessageBinding](from)
    override def asInternal(from: domain.Amqp091MessageBinding): Amqp091MessageBinding = from._internal
  }
}
trait Amqp091OperationBindingConverter extends PlatformSecrets {
  implicit object Amqp091OperationBindingMatcher
      extends BidirectionalMatcher[Amqp091OperationBinding, domain.Amqp091OperationBinding] {
    override def asClient(from: Amqp091OperationBinding): domain.Amqp091OperationBinding =
      platform.wrap[domain.Amqp091OperationBinding](from)
    override def asInternal(from: domain.Amqp091OperationBinding): Amqp091OperationBinding = from._internal
  }
}
trait EmptyBindingConverter extends PlatformSecrets {
  implicit object EmptyBindingMatcher extends BidirectionalMatcher[EmptyBinding, domain.EmptyBinding] {
    override def asClient(from: EmptyBinding): domain.EmptyBinding =
      platform.wrap[domain.EmptyBinding](from)
    override def asInternal(from: domain.EmptyBinding): EmptyBinding = from._internal
  }
}
trait HttpMessageBindingConverter extends PlatformSecrets {
  implicit object HttpMessageBindingMatcher
      extends BidirectionalMatcher[HttpMessageBinding, domain.HttpMessageBinding] {
    override def asClient(from: HttpMessageBinding): domain.HttpMessageBinding =
      platform.wrap[domain.HttpMessageBinding](from)
    override def asInternal(from: domain.HttpMessageBinding): HttpMessageBinding = from._internal
  }
}
trait HttpOperationBindingConverter extends PlatformSecrets {
  implicit object HttpOperationBindingMatcher
      extends BidirectionalMatcher[HttpOperationBinding, domain.HttpOperationBinding] {
    override def asClient(from: HttpOperationBinding): domain.HttpOperationBinding =
      platform.wrap[domain.HttpOperationBinding](from)
    override def asInternal(from: domain.HttpOperationBinding): HttpOperationBinding = from._internal
  }
}
trait KafkaMessageBindingConverter extends PlatformSecrets {
  implicit object KafkaMessageBindingMatcher
      extends BidirectionalMatcher[KafkaMessageBinding, domain.KafkaMessageBinding] {
    override def asClient(from: KafkaMessageBinding): domain.KafkaMessageBinding =
      platform.wrap[domain.KafkaMessageBinding](from)
    override def asInternal(from: domain.KafkaMessageBinding): KafkaMessageBinding = from._internal
  }
}
trait KafkaOperationBindingConverter extends PlatformSecrets {
  implicit object KafkaOperationBindingMatcher
      extends BidirectionalMatcher[KafkaOperationBinding, domain.KafkaOperationBinding] {
    override def asClient(from: KafkaOperationBinding): domain.KafkaOperationBinding =
      platform.wrap[domain.KafkaOperationBinding](from)
    override def asInternal(from: domain.KafkaOperationBinding): KafkaOperationBinding = from._internal
  }
}
trait MqttMessageBindingConverter extends PlatformSecrets {
  implicit object MqttMessageBindingMatcher
      extends BidirectionalMatcher[MqttMessageBinding, domain.MqttMessageBinding] {
    override def asClient(from: MqttMessageBinding): domain.MqttMessageBinding =
      platform.wrap[domain.MqttMessageBinding](from)
    override def asInternal(from: domain.MqttMessageBinding): MqttMessageBinding = from._internal
  }
}
trait MqttOperationBindingConverter extends PlatformSecrets {
  implicit object MqttOperationBindingMatcher
      extends BidirectionalMatcher[MqttOperationBinding, domain.MqttOperationBinding] {
    override def asClient(from: MqttOperationBinding): domain.MqttOperationBinding =
      platform.wrap[domain.MqttOperationBinding](from)
    override def asInternal(from: domain.MqttOperationBinding): MqttOperationBinding = from._internal
  }
}
trait MqttServerBindingConverter extends PlatformSecrets {
  implicit object MqttServerBindingMatcher extends BidirectionalMatcher[MqttServerBinding, domain.MqttServerBinding] {
    override def asClient(from: MqttServerBinding): domain.MqttServerBinding =
      platform.wrap[domain.MqttServerBinding](from)
    override def asInternal(from: domain.MqttServerBinding): MqttServerBinding = from._internal
  }
}
trait WebSocketsChannelBindingConverter extends PlatformSecrets {
  implicit object WebSocketsChannelBindingMatcher
      extends BidirectionalMatcher[WebSocketsChannelBinding, domain.WebSocketsChannelBinding] {
    override def asClient(from: WebSocketsChannelBinding): domain.WebSocketsChannelBinding =
      platform.wrap[domain.WebSocketsChannelBinding](from)
    override def asInternal(from: domain.WebSocketsChannelBinding): WebSocketsChannelBinding = from._internal
  }
}

trait MqttServerLastWillConverter extends PlatformSecrets {
  implicit object MqttServerLastWillMatcher
      extends BidirectionalMatcher[MqttServerLastWill, domain.MqttServerLastWill] {
    override def asClient(from: MqttServerLastWill): domain.MqttServerLastWill =
      platform.wrap[domain.MqttServerLastWill](from)
    override def asInternal(from: domain.MqttServerLastWill): MqttServerLastWill = from._internal
  }
}

trait Amqp091ChannelExchangeConverter extends PlatformSecrets {
  implicit object Amqp091ChannelExchangeMatcher
      extends BidirectionalMatcher[Amqp091ChannelExchange, domain.Amqp091ChannelExchange] {
    override def asClient(from: Amqp091ChannelExchange): domain.Amqp091ChannelExchange =
      platform.wrap[domain.Amqp091ChannelExchange](from)
    override def asInternal(from: domain.Amqp091ChannelExchange): Amqp091ChannelExchange = from._internal
  }
}

trait Amqp091QueueConverter extends PlatformSecrets {
  implicit object Amqp091QueueMatcher extends BidirectionalMatcher[Amqp091Queue, domain.Amqp091Queue] {
    override def asClient(from: Amqp091Queue): domain.Amqp091Queue =
      platform.wrap[domain.Amqp091Queue](from)
    override def asInternal(from: domain.Amqp091Queue): Amqp091Queue = from._internal
  }
}

trait EndPointConverter extends PlatformSecrets {

  implicit object EndPointMatcher extends BidirectionalMatcher[EndPoint, domain.EndPoint] {
    override def asClient(from: EndPoint): domain.EndPoint   = platform.wrap[domain.EndPoint](from)
    override def asInternal(from: domain.EndPoint): EndPoint = from._internal
  }
}

trait ResourceTypeConverter extends PlatformSecrets {

  implicit object ResourceTypeMatcher extends BidirectionalMatcher[ResourceType, domain.ResourceType] {
    override def asClient(from: ResourceType): domain.ResourceType   = ClientResourceType(from)
    override def asInternal(from: domain.ResourceType): ResourceType = from._internal
  }
}

trait TraitConverter extends PlatformSecrets {

  implicit object TraitMatcher extends BidirectionalMatcher[Trait, domain.Trait] {
    override def asClient(from: Trait): domain.Trait   = ClientTrait(from)
    override def asInternal(from: domain.Trait): Trait = from._internal
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
      extends BidirectionalMatcher[ParametrizedSecurityScheme, domain.ParametrizedSecurityScheme] {
    override def asClient(from: ParametrizedSecurityScheme): domain.ParametrizedSecurityScheme =
      platform.wrap[domain.ParametrizedSecurityScheme](from)

    override def asInternal(from: domain.ParametrizedSecurityScheme): ParametrizedSecurityScheme = from._internal
  }
}

trait SecuritySchemeConverter extends PlatformSecrets {

  implicit object SecuritySchemeMatcher extends BidirectionalMatcher[SecurityScheme, domain.SecurityScheme] {
    override def asClient(from: SecurityScheme): domain.SecurityScheme   = platform.wrap[domain.SecurityScheme](from)
    override def asInternal(from: domain.SecurityScheme): SecurityScheme = from._internal
  }
}

trait OAuth2FlowConverter extends PlatformSecrets {

  implicit object OAuth2FlowMatcher extends BidirectionalMatcher[OAuth2Flow, domain.OAuth2Flow] {
    override def asClient(from: OAuth2Flow): domain.OAuth2Flow   = platform.wrap[domain.OAuth2Flow](from)
    override def asInternal(from: domain.OAuth2Flow): OAuth2Flow = from._internal
  }
}

trait SecurityRequirementConverter extends PlatformSecrets {

  implicit object SecurityRequirementMatcher
      extends BidirectionalMatcher[SecurityRequirement, domain.SecurityRequirement] {
    override def asClient(from: SecurityRequirement): domain.SecurityRequirement =
      platform.wrap[domain.SecurityRequirement](from)
    override def asInternal(from: domain.SecurityRequirement): SecurityRequirement = from._internal
  }
}

trait SettingsConverter extends PlatformSecrets {

  implicit object OAuth1SettingsMatcher extends BidirectionalMatcher[OAuth1Settings, domain.OAuth1Settings] {
    override def asClient(from: OAuth1Settings): domain.OAuth1Settings   = ClientOAuth1Settings(from)
    override def asInternal(from: domain.OAuth1Settings): OAuth1Settings = from._internal
  }

  implicit object OAuth2SettingsMatcher extends BidirectionalMatcher[OAuth2Settings, domain.OAuth2Settings] {
    override def asClient(from: OAuth2Settings): domain.OAuth2Settings   = ClientOAuth2Settings(from)
    override def asInternal(from: domain.OAuth2Settings): OAuth2Settings = from._internal
  }

  implicit object ApiKeySettingsMatcher extends BidirectionalMatcher[ApiKeySettings, domain.ApiKeySettings] {
    override def asClient(from: ApiKeySettings): domain.ApiKeySettings   = ClientApiKeySettings(from)
    override def asInternal(from: domain.ApiKeySettings): ApiKeySettings = from._internal
  }

  implicit object HttpApiKeySettingsMatcher
      extends BidirectionalMatcher[HttpApiKeySettings, domain.HttpApiKeySettings] {
    override def asClient(from: HttpApiKeySettings): domain.HttpApiKeySettings   = ClientHttpApiKeySettings(from)
    override def asInternal(from: domain.HttpApiKeySettings): HttpApiKeySettings = from._internal
  }

  implicit object HttpSettingsMatcher extends BidirectionalMatcher[HttpSettings, domain.HttpSettings] {
    override def asClient(from: HttpSettings): domain.HttpSettings   = ClientHttpSettings(from)
    override def asInternal(from: domain.HttpSettings): HttpSettings = from._internal
  }
  implicit object OpenIdConnectSettingsMatcher
      extends BidirectionalMatcher[OpenIdConnectSettings, domain.OpenIdConnectSettings] {
    override def asClient(from: OpenIdConnectSettings): domain.OpenIdConnectSettings =
      ClientOpenIdConnectSettings(from)
    override def asInternal(from: domain.OpenIdConnectSettings): OpenIdConnectSettings = from._internal
  }

  implicit object SettingsMatcher extends BidirectionalMatcher[Settings, domain.Settings] {
    override def asClient(from: Settings): domain.Settings = from match {
      case oauth1: OAuth1Settings        => OAuth1SettingsMatcher.asClient(oauth1)
      case oauth2: OAuth2Settings        => OAuth2SettingsMatcher.asClient(oauth2)
      case apiKey: ApiKeySettings        => ApiKeySettingsMatcher.asClient(apiKey)
      case http: HttpSettings            => HttpSettingsMatcher.asClient(http)
      case openId: OpenIdConnectSettings => OpenIdConnectSettingsMatcher.asClient(openId)
      case base: Settings                => new domain.Settings(base)
      case _ => // noinspection ScalaStyle
        null
    }

    override def asInternal(from: domain.Settings): Settings = from._internal
  }
}

trait ScopeConverter extends PlatformSecrets {

  implicit object ScopeMatcher extends BidirectionalMatcher[Scope, domain.Scope] {
    override def asClient(from: Scope): domain.Scope   = platform.wrap[domain.Scope](from)
    override def asInternal(from: domain.Scope): Scope = from._internal
  }
}

trait TemplatedLinkConverter extends PlatformSecrets {

  implicit object TemplatedLinkConverter extends BidirectionalMatcher[TemplatedLink, domain.TemplatedLink] {
    override def asClient(from: TemplatedLink): domain.TemplatedLink   = platform.wrap[domain.TemplatedLink](from)
    override def asInternal(from: domain.TemplatedLink): TemplatedLink = from._internal
  }
}

trait AMFConfigurationConverter {
  implicit object AMFConfigurationMatcher extends BidirectionalMatcher[AMFConfiguration, config.AMFConfiguration] {
    override def asClient(from: AMFConfiguration): config.AMFConfiguration   = new config.AMFConfiguration(from)
    override def asInternal(from: config.AMFConfiguration): AMFConfiguration = from._internal
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
