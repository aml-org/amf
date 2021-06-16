package amf.apicontract.client.platform.model.domain.security

import amf.apicontract.client.platform.model.domain.{Parameter, Response}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, Shape}
import amf.apicontract.client.scala.model.domain.security.{SecurityScheme => InternalSecurityScheme}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * SecurityScheme model class.
  */
@JSExportAll
case class SecurityScheme(override private[amf] val _internal: InternalSecurityScheme)
    extends DomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.SecurityScheme")
  def this() = this(InternalSecurityScheme())

  def name: StrField                         = _internal.name
  def `type`: StrField                       = _internal.`type`
  def displayName: StrField                  = _internal.displayName
  def description: StrField                  = _internal.description
  def headers: ClientList[Parameter]         = _internal.headers.asClient
  def queryParameters: ClientList[Parameter] = _internal.queryParameters.asClient
  def responses: ClientList[Response]        = _internal.responses.asClient
  def settings: Settings                     = _internal.settings
  def queryString: Shape                     = _internal.queryString

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }

  def withDisplayName(displayName: String): this.type = {
    _internal.withDisplayName(displayName)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withHeaders(headers: ClientList[Parameter]): this.type = {
    _internal.withHeaders(headers.asInternal)
    this
  }

  def withQueryParameters(queryParameters: ClientList[Parameter]): this.type = {
    _internal.withQueryParameters(queryParameters.asInternal)
    this
  }

  def withResponses(responses: ClientList[Response]): this.type = {
    _internal.withResponses(responses.asInternal)
    this
  }

  def withSettings(settings: Settings): this.type = {
    _internal.withSettings(settings)
    this
  }

  def withQueryString(queryString: Shape): this.type = {
    _internal.withQueryString(queryString)
    this
  }

  def withHeader(name: String): Parameter = _internal.withHeader(name)

  def withQueryParameter(name: String): Parameter = _internal.withQueryParameter(name)

  def withResponse(name: String): Response = _internal.withResponse(name)

  def withDefaultSettings(): Settings = _internal.withDefaultSettings()

  def withOAuth1Settings(): OAuth1Settings = _internal.withOAuth1Settings()

  def withOAuth2Settings(): OAuth2Settings = _internal.withOAuth2Settings()

  def withApiKeySettings(): ApiKeySettings = _internal.withApiKeySettings()

  def withHttpApiKeySettings(): HttpApiKeySettings = _internal.withHttpApiKeySettings()

  def withHttpSettings(): HttpSettings = _internal.withHttpSettings()

  def withOpenIdConnectSettings(): OpenIdConnectSettings = _internal.withOpenIdConnectSettings()

  override def linkCopy(): SecurityScheme = _internal.linkCopy()
}
