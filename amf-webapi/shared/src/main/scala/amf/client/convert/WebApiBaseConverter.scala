package amf.client.convert

import amf.client.model.domain.{
  ApiKeySettings => ClientApiKeySettings,
  CreativeWork => ClientCreativeWork,
  EndPoint => ClientEndPoint,
  License => ClientLicense,
  OAuth1Settings => ClientOAuth1Settings,
  OAuth2Settings => ClientOAuth2Settings,
  Operation => ClientOperation,
  Organization => ClientOrganization,
  Parameter => ClientParameter,
  ParametrizedSecurityScheme => ClientParametrizedSecurityScheme,
  Payload => ClientPayload,
  Request => ClientRequest,
  ResourceType => ClientResourceType,
  Response => ClientResponse,
  Scope => ClientScope,
  SecurityScheme => ClientSecurityScheme,
  Settings => ClientSettings,
  Trait => ClientTrait
}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.models._
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
    with RequestConverter
    with ResponseConverter
    with OperationConverter
    with ParameterConverter
    with PayloadConverter
    with ParametrizedSecuritySchemeConverter
    with SecuritySchemeConverter
    with SettingsConverter
    with ScopeConverter

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

trait RequestConverter extends PlatformSecrets {

  implicit object RequestMatcher extends BidirectionalMatcher[Request, ClientRequest] {
    override def asClient(from: Request): ClientRequest   = platform.wrap[ClientRequest](from)
    override def asInternal(from: ClientRequest): Request = from._internal
  }
}

trait ResponseConverter extends PlatformSecrets {

  implicit object ResponseMatcher extends BidirectionalMatcher[Response, ClientResponse] {
    override def asClient(from: Response): ClientResponse   = platform.wrap[ClientResponse](from)
    override def asInternal(from: ClientResponse): Response = from._internal
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

  implicit object SettingsMatcher extends BidirectionalMatcher[Settings, ClientSettings] {
    override def asClient(from: Settings): ClientSettings = from match {
      case oauth1: OAuth1Settings => OAuth1SettingsMatcher.asClient(oauth1)
      case oauth2: OAuth2Settings => OAuth2SettingsMatcher.asClient(oauth2)
      case apiKey: ApiKeySettings => ApiKeySettingsMatcher.asClient(apiKey)
      case base: Settings         => new ClientSettings(base)
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
