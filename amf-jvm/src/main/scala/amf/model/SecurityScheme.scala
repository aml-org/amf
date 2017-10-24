package amf.model

import scala.collection.JavaConverters._

/**
  * JVM SecurityScheme model class.
  */
case class SecurityScheme private[model] (private val scheme: amf.domain.security.SecurityScheme)
    extends DomainElement
    with Linkable {
  def this() = this(amf.domain.security.SecurityScheme())

  val name: String                               = scheme.name
  val `type`: String                             = scheme.`type`
  val displayName: String                        = scheme.displayName
  val description: String                        = scheme.description
  val headers: java.util.List[Parameter]         = scheme.headers.map(Parameter).asJava
  val queryParameters: java.util.List[Parameter] = scheme.queryParameters.map(Parameter).asJava
  val responses: java.util.List[Response]        = scheme.responses.map(Response).asJava
  val settings: Settings                         = Settings(scheme.settings)

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

  def withHeaders(headers: java.util.List[Parameter]): this.type = {
    scheme.withHeaders(headers.asScala.map(_.element))
    this
  }

  def withQueryParameters(queryParameters: java.util.List[Parameter]): this.type = {
    scheme.withQueryParameters(queryParameters.asScala.map(_.element))
    this
  }

  def withResponses(responses: java.util.List[Response]): this.type = {
    scheme.withResponses(responses.asScala.map(_.element))
    this
  }

  def withSettings(settings: Settings): this.type = {
    scheme.withSettings(settings.element)
    this
  }

  override private[amf] def element: amf.domain.security.SecurityScheme = scheme

  def withHeader(name: String): Parameter = Parameter(scheme.withHeader(name))

  def withQueryParameter(name: String): Parameter = Parameter(scheme.withQueryParameter(name))

  def withResponse(name: String): Response = Response(scheme.withResponse(name))

  def withDefaultSettings(): Settings = Settings(scheme.withDefaultSettings())

  def withOAuth1Settings(): OAuth1Settings = OAuth1Settings(scheme.withOAuth1Settings())

  def withOAuth2Settings(): OAuth2Settings = OAuth2Settings(scheme.withOAuth2Settings())

  def withApiKeySettings(): ApiKeySettings = ApiKeySettings(scheme.withApiKeySettings())

  override def linkCopy(): SecurityScheme = SecurityScheme(scheme.linkCopy())

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map { case ss: amf.domain.security.SecurityScheme => SecurityScheme(ss) }
}
