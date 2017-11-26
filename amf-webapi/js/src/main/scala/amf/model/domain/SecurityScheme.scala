package amf.model.domain

import amf.plugins.domain.webapi.models.security

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS SecurityScheme model class.
  */
@JSExportAll
case class SecurityScheme private[model] (private val scheme: security.SecurityScheme)
    extends DomainElement
    with Linkable {
  def this() = this(security.SecurityScheme())

  val name: String                            = scheme.name
  val `type`: String                          = scheme.`type`
  val displayName: String                     = scheme.displayName
  val description: String                     = scheme.description
  val headers: js.Iterable[Parameter]         = scheme.headers.map(Parameter).toJSArray
  val queryParameters: js.Iterable[Parameter] = scheme.queryParameters.map(Parameter).toJSArray
  val responses: js.Iterable[Response]        = scheme.responses.map(Response).toJSArray
  val settings: Settings                      = Settings(scheme.settings)
  val queryString: Shape                      = Option(scheme.queryString).map(Shape(_)).orNull

  def withName(name: String): this.type = {
    scheme.withName(name)
    this
  }

  def withType(`type`: String): this.type = {
    scheme.withType(`type`)
    this
  }

  def withDisplayName(displayName: String): this.type = {
    scheme.withDisplayName(displayName)
    this
  }

  def withDescription(description: String): this.type = {
    scheme.withDescription(description)
    this
  }

  def withHeaders(headers: js.Iterable[Parameter]): this.type = {
    scheme.withHeaders(headers.toSeq.map(_.element))
    this
  }

  def withQueryParameters(queryParameters: js.Iterable[Parameter]): this.type = {
    scheme.withQueryParameters(queryParameters.toSeq.map(_.element))
    this
  }

  def withResponses(responses: js.Iterable[Response]): this.type = {
    scheme.withResponses(responses.toSeq.map(_.element))
    this
  }

  def withSettings(settings: Settings): this.type = {
    scheme.withSettings(settings.element)
    this
  }

  def withQueryString(queryString: Shape): this.type = {
    scheme.withQueryString(queryString.shape)
    this
  }

  override private[amf] def element: amf.plugins.domain.webapi.models.security.SecurityScheme = scheme

  def withHeader(name: String): Parameter = Parameter(scheme.withHeader(name))

  def withQueryParameter(name: String): Parameter = Parameter(scheme.withQueryParameter(name))

  def withResponse(name: String): Response = Response(scheme.withResponse(name))

  def withDefaultSettings(): Settings = Settings(scheme.withDefaultSettings())

  def withOAuth1Settings(): OAuth1Settings = OAuth1Settings(scheme.withOAuth1Settings())

  def withOAuth2Settings(): OAuth2Settings = OAuth2Settings(scheme.withOAuth2Settings())

  def withApiKeySettings(): ApiKeySettings = ApiKeySettings(scheme.withApiKeySettings())

  override def linkCopy(): SecurityScheme = SecurityScheme(scheme.linkCopy())

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map { case ss: amf.plugins.domain.webapi.models.security.SecurityScheme => SecurityScheme(ss) }
}
