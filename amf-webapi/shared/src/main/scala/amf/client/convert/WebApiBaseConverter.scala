package amf.client.convert

import amf.client.model.domain.{
  Request => ClientRequest,
  Message => ClientMessage,
  Amqp091OperationBinding => ClientAmqp091OperationBinding,
  EndPoint => ClientEndPoint,
  Settings => ClientSettings,
  Parameter => ClientParameter,
  OpenIdConnectSettings => ClientOpenIdConnectSettings,
  SecurityScheme => ClientSecurityScheme,
  OperationBinding => ClientOperationBinding,
  Scope => ClientScope,
  MqttServerBinding => ClientMqttServerBinding,
  DynamicBinding => ClientDynamicBinding,
  ChannelBinding => ClientChannelBinding,
  Server => ClientServer,
  ApiKeySettings => ClientApiKeySettings,
  HttpApiKeySettings => ClientHttpApiKeySettings,
  KafkaOperationBinding => ClientKafkaOperationBinding,
  HttpSettings => ClientHttpSettings,
  License => ClientLicense,
  TemplatedLink => ClientTemplatedLink,
  EmptyBinding => ClientEmptyBinding,
  Amqp091ChannelBinding => ClientAmqp091ChannelBinding,
  Encoding => ClientEncoding,
  ResourceType => ClientResourceType,
  MessageBinding => ClientMessageBinding,
  Amqp091ChannelExchange => ClientAmqp091ChannelExchange,
  CorrelationId => ClientCorrelationId,
  Operation => ClientOperation,
  HttpOperationBinding => ClientHttpOperationBinding,
  MqttMessageBinding => ClientMqttMessageBinding,
  Callback => ClientCallback,
  CreativeWork => ClientCreativeWork,
  Amqp091MessageBinding => ClientAmqp091MessageBinding,
  IriTemplateMapping => ClientIriTemplatedMaping,
  OAuth2Flow => ClientOAuth2Flow,
  WebSocketsChannelBinding => ClientWebSocketsChannelBinding,
  KafkaMessageBinding => ClientKafkaMessageBinding,
  Payload => ClientPayload,
  Tag => ClientTag,
  Response => ClientResponse,
  Trait => ClientTrait,
  OAuth1Settings => ClientOAuth1Settings,
  Amqp091Queue => ClientAmqp091Queue,
  Organization => ClientOrganization,
  HttpMessageBinding => ClientHttpMessageBinding,
  MqttOperationBinding => ClientMqttOperationBinding,
  SecurityRequirement => ClientSecurityRequirement,
  ServerBinding => ClientServerBinding,
  ParametrizedSecurityScheme => ClientParametrizedSecurityScheme,
  MqttServerLastWill => ClientMqttServerLastWill,
  OAuth2Settings => ClientOAuth2Settings
}
import amf.client.validate.{PayloadValidator => ClientInternalPayloadValidator}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.PayloadValidator
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.bindings.amqp._
import amf.plugins.domain.webapi.models.bindings.mqtt._
import amf.plugins.domain.webapi.models.bindings.http._
import amf.plugins.domain.webapi.models.bindings.kafka._
import amf.plugins.domain.webapi.models.bindings.websockets._
import amf.plugins.domain.webapi.models.bindings.{
  ChannelBinding,
  DynamicBinding,
  EmptyBinding,
  MessageBinding,
  OperationBinding,
  ServerBinding
}
import amf.plugins.domain.webapi.models.security._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}

trait WebApiBaseConverter
    extends CoreBaseConverter
    with EndPointConverter
    with ResourceTypeConverter
    with TraitConverter
    with OrganizationConverter
    with LicenseConverter
    with CreativeWorkConverter
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
    with IriTemplateMappingConverter
    with TemplatedLinkConverter
    with CallbackConverter
    with EncodingConverter
    with PayloadValidatorConverter
    with OAuth2FlowConverter
    with SecurityRequirementConverter
    with CorrelationIdConverter
    with Amqp091ChannelBindingConverter
    with Amqp091MessageBindingConverter
    with Amqp091OperationBindingConverter
    with DynamicBindingConverter
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

trait ChannelBindingConverter extends PlatformSecrets {
  implicit object ChannelBindingMatcher extends BidirectionalMatcher[ChannelBinding, ClientChannelBinding] {
    override def asClient(from: ChannelBinding): ClientChannelBinding =
      platform.wrap[ClientChannelBinding](from)
    override def asInternal(from: ClientChannelBinding): ChannelBinding = from._internal
  }
}
trait OperationBindingConverter extends PlatformSecrets {
  implicit object OperationBindingMatcher extends BidirectionalMatcher[OperationBinding, ClientOperationBinding] {
    override def asClient(from: OperationBinding): ClientOperationBinding =
      platform.wrap[ClientOperationBinding](from)
    override def asInternal(from: ClientOperationBinding): OperationBinding = from._internal
  }
}
trait MessageBindingConverter extends PlatformSecrets {
  implicit object MessageBindingMatcher extends BidirectionalMatcher[MessageBinding, ClientMessageBinding] {
    override def asClient(from: MessageBinding): ClientMessageBinding =
      platform.wrap[ClientMessageBinding](from)
    override def asInternal(from: ClientMessageBinding): MessageBinding = from._internal
  }
}
trait ServerBindingConverter extends PlatformSecrets {
  implicit object ServerBindingMatcher extends BidirectionalMatcher[ServerBinding, ClientServerBinding] {
    override def asClient(from: ServerBinding): ClientServerBinding =
      platform.wrap[ClientServerBinding](from)
    override def asInternal(from: ClientServerBinding): ServerBinding = from._internal
  }
}

trait Amqp091ChannelBindingConverter extends PlatformSecrets {
  implicit object Amqp091ChannelBindingMatcher
      extends BidirectionalMatcher[Amqp091ChannelBinding, ClientAmqp091ChannelBinding] {
    override def asClient(from: Amqp091ChannelBinding): ClientAmqp091ChannelBinding =
      platform.wrap[ClientAmqp091ChannelBinding](from)
    override def asInternal(from: ClientAmqp091ChannelBinding): Amqp091ChannelBinding = from._internal
  }
}
trait Amqp091MessageBindingConverter extends PlatformSecrets {
  implicit object Amqp091MessageBindingMatcher
      extends BidirectionalMatcher[Amqp091MessageBinding, ClientAmqp091MessageBinding] {
    override def asClient(from: Amqp091MessageBinding): ClientAmqp091MessageBinding =
      platform.wrap[ClientAmqp091MessageBinding](from)
    override def asInternal(from: ClientAmqp091MessageBinding): Amqp091MessageBinding = from._internal
  }
}
trait Amqp091OperationBindingConverter extends PlatformSecrets {
  implicit object Amqp091OperationBindingMatcher
      extends BidirectionalMatcher[Amqp091OperationBinding, ClientAmqp091OperationBinding] {
    override def asClient(from: Amqp091OperationBinding): ClientAmqp091OperationBinding =
      platform.wrap[ClientAmqp091OperationBinding](from)
    override def asInternal(from: ClientAmqp091OperationBinding): Amqp091OperationBinding = from._internal
  }
}
trait DynamicBindingConverter extends PlatformSecrets {
  implicit object DynamicBindingMatcher extends BidirectionalMatcher[DynamicBinding, ClientDynamicBinding] {
    override def asClient(from: DynamicBinding): ClientDynamicBinding =
      platform.wrap[ClientDynamicBinding](from)
    override def asInternal(from: ClientDynamicBinding): DynamicBinding = from._internal
  }
}
trait EmptyBindingConverter extends PlatformSecrets {
  implicit object EmptyBindingMatcher extends BidirectionalMatcher[EmptyBinding, ClientEmptyBinding] {
    override def asClient(from: EmptyBinding): ClientEmptyBinding =
      platform.wrap[ClientEmptyBinding](from)
    override def asInternal(from: ClientEmptyBinding): EmptyBinding = from._internal
  }
}
trait HttpMessageBindingConverter extends PlatformSecrets {
  implicit object HttpMessageBindingMatcher
      extends BidirectionalMatcher[HttpMessageBinding, ClientHttpMessageBinding] {
    override def asClient(from: HttpMessageBinding): ClientHttpMessageBinding =
      platform.wrap[ClientHttpMessageBinding](from)
    override def asInternal(from: ClientHttpMessageBinding): HttpMessageBinding = from._internal
  }
}
trait HttpOperationBindingConverter extends PlatformSecrets {
  implicit object HttpOperationBindingMatcher
      extends BidirectionalMatcher[HttpOperationBinding, ClientHttpOperationBinding] {
    override def asClient(from: HttpOperationBinding): ClientHttpOperationBinding =
      platform.wrap[ClientHttpOperationBinding](from)
    override def asInternal(from: ClientHttpOperationBinding): HttpOperationBinding = from._internal
  }
}
trait KafkaMessageBindingConverter extends PlatformSecrets {
  implicit object KafkaMessageBindingMatcher
      extends BidirectionalMatcher[KafkaMessageBinding, ClientKafkaMessageBinding] {
    override def asClient(from: KafkaMessageBinding): ClientKafkaMessageBinding =
      platform.wrap[ClientKafkaMessageBinding](from)
    override def asInternal(from: ClientKafkaMessageBinding): KafkaMessageBinding = from._internal
  }
}
trait KafkaOperationBindingConverter extends PlatformSecrets {
  implicit object KafkaOperationBindingMatcher
      extends BidirectionalMatcher[KafkaOperationBinding, ClientKafkaOperationBinding] {
    override def asClient(from: KafkaOperationBinding): ClientKafkaOperationBinding =
      platform.wrap[ClientKafkaOperationBinding](from)
    override def asInternal(from: ClientKafkaOperationBinding): KafkaOperationBinding = from._internal
  }
}
trait MqttMessageBindingConverter extends PlatformSecrets {
  implicit object MqttMessageBindingMatcher
      extends BidirectionalMatcher[MqttMessageBinding, ClientMqttMessageBinding] {
    override def asClient(from: MqttMessageBinding): ClientMqttMessageBinding =
      platform.wrap[ClientMqttMessageBinding](from)
    override def asInternal(from: ClientMqttMessageBinding): MqttMessageBinding = from._internal
  }
}
trait MqttOperationBindingConverter extends PlatformSecrets {
  implicit object MqttOperationBindingMatcher
      extends BidirectionalMatcher[MqttOperationBinding, ClientMqttOperationBinding] {
    override def asClient(from: MqttOperationBinding): ClientMqttOperationBinding =
      platform.wrap[ClientMqttOperationBinding](from)
    override def asInternal(from: ClientMqttOperationBinding): MqttOperationBinding = from._internal
  }
}
trait MqttServerBindingConverter extends PlatformSecrets {
  implicit object MqttServerBindingMatcher extends BidirectionalMatcher[MqttServerBinding, ClientMqttServerBinding] {
    override def asClient(from: MqttServerBinding): ClientMqttServerBinding =
      platform.wrap[ClientMqttServerBinding](from)
    override def asInternal(from: ClientMqttServerBinding): MqttServerBinding = from._internal
  }
}
trait WebSocketsChannelBindingConverter extends PlatformSecrets {
  implicit object WebSocketsChannelBindingMatcher
      extends BidirectionalMatcher[WebSocketsChannelBinding, ClientWebSocketsChannelBinding] {
    override def asClient(from: WebSocketsChannelBinding): ClientWebSocketsChannelBinding =
      platform.wrap[ClientWebSocketsChannelBinding](from)
    override def asInternal(from: ClientWebSocketsChannelBinding): WebSocketsChannelBinding = from._internal
  }
}

trait MqttServerLastWillConverter extends PlatformSecrets {
  implicit object MqttServerLastWillMatcher
      extends BidirectionalMatcher[MqttServerLastWill, ClientMqttServerLastWill] {
    override def asClient(from: MqttServerLastWill): ClientMqttServerLastWill =
      platform.wrap[ClientMqttServerLastWill](from)
    override def asInternal(from: ClientMqttServerLastWill): MqttServerLastWill = from._internal
  }
}

trait Amqp091ChannelExchangeConverter extends PlatformSecrets {
  implicit object Amqp091ChannelExchangeMatcher
      extends BidirectionalMatcher[Amqp091ChannelExchange, ClientAmqp091ChannelExchange] {
    override def asClient(from: Amqp091ChannelExchange): ClientAmqp091ChannelExchange =
      platform.wrap[ClientAmqp091ChannelExchange](from)
    override def asInternal(from: ClientAmqp091ChannelExchange): Amqp091ChannelExchange = from._internal
  }
}

trait Amqp091QueueConverter extends PlatformSecrets {
  implicit object Amqp091QueueMatcher extends BidirectionalMatcher[Amqp091Queue, ClientAmqp091Queue] {
    override def asClient(from: Amqp091Queue): ClientAmqp091Queue =
      platform.wrap[ClientAmqp091Queue](from)
    override def asInternal(from: ClientAmqp091Queue): Amqp091Queue = from._internal
  }
}

trait EndPointConverter extends PlatformSecrets {

  implicit object EndPointMatcher extends BidirectionalMatcher[EndPoint, ClientEndPoint] {
    override def asClient(from: EndPoint): ClientEndPoint   = platform.wrap[ClientEndPoint](from)
    override def asInternal(from: ClientEndPoint): EndPoint = from._internal
  }
}

trait ResourceTypeConverter extends PlatformSecrets {

  implicit object ResourceTypeMatcher extends BidirectionalMatcher[ResourceType, ClientResourceType] {
    override def asClient(from: ResourceType): ClientResourceType   = ClientResourceType(from)
    override def asInternal(from: ClientResourceType): ResourceType = from._internal
  }
}

trait TraitConverter extends PlatformSecrets {

  implicit object TraitMatcher extends BidirectionalMatcher[Trait, ClientTrait] {
    override def asClient(from: Trait): ClientTrait   = ClientTrait(from)
    override def asInternal(from: ClientTrait): Trait = from._internal
  }
}

trait OrganizationConverter extends PlatformSecrets {

  implicit object OrganizationMatcher extends BidirectionalMatcher[Organization, ClientOrganization] {
    override def asClient(from: Organization): ClientOrganization   = platform.wrap[ClientOrganization](from)
    override def asInternal(from: ClientOrganization): Organization = from._internal
  }
}

trait LicenseConverter extends PlatformSecrets {

  implicit object LicenseMatcher extends BidirectionalMatcher[License, ClientLicense] {
    override def asClient(from: License): ClientLicense   = platform.wrap[ClientLicense](from)
    override def asInternal(from: ClientLicense): License = from._internal
  }
}

trait ParameterConverter extends PlatformSecrets {

  implicit object ParameterMatcher extends BidirectionalMatcher[Parameter, ClientParameter] {
    override def asClient(from: Parameter): ClientParameter   = platform.wrap[ClientParameter](from)
    override def asInternal(from: ClientParameter): Parameter = from._internal
  }
}

trait ServerConverter extends PlatformSecrets {

  implicit object ServerMatcher extends BidirectionalMatcher[Server, ClientServer] {
    override def asClient(from: Server): ClientServer   = platform.wrap[ClientServer](from)
    override def asInternal(from: ClientServer): Server = from._internal
  }
}

trait CallbackConverter extends PlatformSecrets {

  implicit object CallbackMatcher extends BidirectionalMatcher[Callback, ClientCallback] {
    override def asClient(from: Callback): ClientCallback   = platform.wrap[ClientCallback](from)
    override def asInternal(from: ClientCallback): Callback = from._internal
  }
}

trait EncodingConverter extends PlatformSecrets {

  implicit object EncodingMatcher extends BidirectionalMatcher[Encoding, ClientEncoding] {
    override def asClient(from: Encoding): ClientEncoding   = platform.wrap[ClientEncoding](from)
    override def asInternal(from: ClientEncoding): Encoding = from._internal
  }
}

trait PayloadConverter extends PlatformSecrets {

  implicit object PayloadMatcher extends BidirectionalMatcher[Payload, ClientPayload] {
    override def asClient(from: Payload): ClientPayload   = platform.wrap[ClientPayload](from)
    override def asInternal(from: ClientPayload): Payload = from._internal
  }
}

trait OperationConverter extends PlatformSecrets {

  implicit object OperationMatcher extends BidirectionalMatcher[Operation, ClientOperation] {
    override def asClient(from: Operation): ClientOperation   = platform.wrap[ClientOperation](from)
    override def asInternal(from: ClientOperation): Operation = from._internal
  }

}

trait TagConverter extends PlatformSecrets {

  implicit object TagMatcher extends BidirectionalMatcher[Tag, ClientTag] {
    override def asClient(from: Tag): ClientTag   = platform.wrap[ClientTag](from)
    override def asInternal(from: ClientTag): Tag = from._internal
  }

}

trait CorrelationIdConverter extends PlatformSecrets {

  implicit object CorrelationIdMatcher extends BidirectionalMatcher[CorrelationId, ClientCorrelationId] {
    override def asClient(from: CorrelationId): ClientCorrelationId   = platform.wrap[ClientCorrelationId](from)
    override def asInternal(from: ClientCorrelationId): CorrelationId = from._internal
  }

}

trait MessageConverter extends PlatformSecrets {

  implicit object ResponseMatcher extends BidirectionalMatcher[Response, ClientResponse] {
    override def asClient(from: Response): ClientResponse   = platform.wrap[ClientResponse](from)
    override def asInternal(from: ClientResponse): Response = from._internal
  }

  implicit object RequestMatcher extends BidirectionalMatcher[Request, ClientRequest] {
    override def asClient(from: Request): ClientRequest   = platform.wrap[ClientRequest](from)
    override def asInternal(from: ClientRequest): Request = from._internal
  }

  implicit object MessageMatcher extends BidirectionalMatcher[Message, ClientMessage] {
    override def asClient(from: Message): ClientMessage = from match {
      case req: Request  => RequestMatcher.asClient(req)
      case res: Response => ResponseMatcher.asClient(res)
      case base: Message => new ClientMessage(base)
      case _ => // noinspection ScalaStyle
        null
    }
    override def asInternal(from: ClientMessage): Message = from._internal
  }
}

trait CreativeWorkConverter extends PlatformSecrets {

  implicit object CreativeWorkMatcher extends BidirectionalMatcher[CreativeWork, ClientCreativeWork] {
    override def asClient(from: CreativeWork): ClientCreativeWork   = platform.wrap[ClientCreativeWork](from)
    override def asInternal(from: ClientCreativeWork): CreativeWork = from._internal
  }
}

trait ParametrizedSecuritySchemeConverter extends PlatformSecrets {

  implicit object ParametrizedSecuritySchemeMatcher
      extends BidirectionalMatcher[ParametrizedSecurityScheme, ClientParametrizedSecurityScheme] {
    override def asClient(from: ParametrizedSecurityScheme): ClientParametrizedSecurityScheme =
      platform.wrap[ClientParametrizedSecurityScheme](from)

    override def asInternal(from: ClientParametrizedSecurityScheme): ParametrizedSecurityScheme = from._internal
  }
}

trait SecuritySchemeConverter extends PlatformSecrets {

  implicit object SecuritySchemeMatcher extends BidirectionalMatcher[SecurityScheme, ClientSecurityScheme] {
    override def asClient(from: SecurityScheme): ClientSecurityScheme   = platform.wrap[ClientSecurityScheme](from)
    override def asInternal(from: ClientSecurityScheme): SecurityScheme = from._internal
  }
}

trait OAuth2FlowConverter extends PlatformSecrets {

  implicit object OAuth2FlowMatcher extends BidirectionalMatcher[OAuth2Flow, ClientOAuth2Flow] {
    override def asClient(from: OAuth2Flow): ClientOAuth2Flow   = platform.wrap[ClientOAuth2Flow](from)
    override def asInternal(from: ClientOAuth2Flow): OAuth2Flow = from._internal
  }
}

trait SecurityRequirementConverter extends PlatformSecrets {

  implicit object SecurityRequirementMatcher
      extends BidirectionalMatcher[SecurityRequirement, ClientSecurityRequirement] {
    override def asClient(from: SecurityRequirement): ClientSecurityRequirement =
      platform.wrap[ClientSecurityRequirement](from)
    override def asInternal(from: ClientSecurityRequirement): SecurityRequirement = from._internal
  }
}

trait SettingsConverter extends PlatformSecrets {

  implicit object OAuth1SettingsMatcher extends BidirectionalMatcher[OAuth1Settings, ClientOAuth1Settings] {
    override def asClient(from: OAuth1Settings): ClientOAuth1Settings   = ClientOAuth1Settings(from)
    override def asInternal(from: ClientOAuth1Settings): OAuth1Settings = from._internal
  }

  implicit object OAuth2SettingsMatcher extends BidirectionalMatcher[OAuth2Settings, ClientOAuth2Settings] {
    override def asClient(from: OAuth2Settings): ClientOAuth2Settings   = ClientOAuth2Settings(from)
    override def asInternal(from: ClientOAuth2Settings): OAuth2Settings = from._internal
  }

  implicit object ApiKeySettingsMatcher extends BidirectionalMatcher[ApiKeySettings, ClientApiKeySettings] {
    override def asClient(from: ApiKeySettings): ClientApiKeySettings   = ClientApiKeySettings(from)
    override def asInternal(from: ClientApiKeySettings): ApiKeySettings = from._internal
  }

  implicit object HttpApiKeySettingsMatcher
      extends BidirectionalMatcher[HttpApiKeySettings, ClientHttpApiKeySettings] {
    override def asClient(from: HttpApiKeySettings): ClientHttpApiKeySettings   = ClientHttpApiKeySettings(from)
    override def asInternal(from: ClientHttpApiKeySettings): HttpApiKeySettings = from._internal
  }

  implicit object HttpSettingsMatcher extends BidirectionalMatcher[HttpSettings, ClientHttpSettings] {
    override def asClient(from: HttpSettings): ClientHttpSettings   = ClientHttpSettings(from)
    override def asInternal(from: ClientHttpSettings): HttpSettings = from._internal
  }
  implicit object OpenIdConnectSettingsMatcher
      extends BidirectionalMatcher[OpenIdConnectSettings, ClientOpenIdConnectSettings] {
    override def asClient(from: OpenIdConnectSettings): ClientOpenIdConnectSettings   = ClientOpenIdConnectSettings(from)
    override def asInternal(from: ClientOpenIdConnectSettings): OpenIdConnectSettings = from._internal
  }

  implicit object SettingsMatcher extends BidirectionalMatcher[Settings, ClientSettings] {
    override def asClient(from: Settings): ClientSettings = from match {
      case oauth1: OAuth1Settings        => OAuth1SettingsMatcher.asClient(oauth1)
      case oauth2: OAuth2Settings        => OAuth2SettingsMatcher.asClient(oauth2)
      case apiKey: ApiKeySettings        => ApiKeySettingsMatcher.asClient(apiKey)
      case http: HttpSettings            => HttpSettingsMatcher.asClient(http)
      case openId: OpenIdConnectSettings => OpenIdConnectSettingsMatcher.asClient(openId)
      case base: Settings                => new ClientSettings(base)
      case _ => // noinspection ScalaStyle
        null
    }

    override def asInternal(from: ClientSettings): Settings = from._internal
  }
}

trait ScopeConverter extends PlatformSecrets {

  implicit object ScopeMatcher extends BidirectionalMatcher[Scope, ClientScope] {
    override def asClient(from: Scope): ClientScope   = platform.wrap[ClientScope](from)
    override def asInternal(from: ClientScope): Scope = from._internal
  }
}

trait IriTemplateMappingConverter extends PlatformSecrets {

  implicit object IriTemplateMappingConverter
      extends BidirectionalMatcher[IriTemplateMapping, ClientIriTemplatedMaping] {
    override def asClient(from: IriTemplateMapping): ClientIriTemplatedMaping =
      platform.wrap[ClientIriTemplatedMaping](from)
    override def asInternal(from: ClientIriTemplatedMaping): IriTemplateMapping = from._internal
  }
}

trait TemplatedLinkConverter extends PlatformSecrets {

  implicit object TemplatedLinkConverter extends BidirectionalMatcher[TemplatedLink, ClientTemplatedLink] {
    override def asClient(from: TemplatedLink): ClientTemplatedLink   = platform.wrap[ClientTemplatedLink](from)
    override def asInternal(from: ClientTemplatedLink): TemplatedLink = from._internal
  }
}

trait PayloadValidatorConverter {

  implicit object PayloadValidatorMatcher
      extends BidirectionalMatcher[PayloadValidator, ClientInternalPayloadValidator] {
    override def asClient(from: PayloadValidator): ClientInternalPayloadValidator =
      new ClientInternalPayloadValidator(from)

    override def asInternal(from: ClientInternalPayloadValidator): PayloadValidator = from._internal
  }
}
