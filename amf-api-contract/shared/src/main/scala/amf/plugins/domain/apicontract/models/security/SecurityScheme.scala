package amf.plugins.domain.apicontract.models.security

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.internal.annotations.{Inferred, SourceAST}
import amf.core.internal.metamodel.domain.common.DisplayNameField.DisplayName
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.plugins.domain.apicontract.metamodel.security.ParametrizedSecuritySchemeModel.Description
import amf.plugins.domain.apicontract.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.apicontract.metamodel.security.SecuritySchemeModel.{Settings => SettingsField, _}
import amf.plugins.domain.apicontract.models.{Parameter, Response}
import org.yaml.model.YPart

class SecurityScheme(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement
    with WithSettings {

  def `type`: StrField                = fields.field(Type)
  def displayName: StrField           = fields.field(DisplayName)
  def description: StrField           = fields.field(Description)
  def headers: Seq[Parameter]         = fields.field(Headers)
  def queryParameters: Seq[Parameter] = fields.field(QueryParameters)
  def responses: Seq[Response]        = fields.field(Responses)
  def settings: Settings              = fields.field(SettingsField)
  def queryString: Shape              = fields.field(QueryString)

  def withType(`type`: String): this.type                             = set(Type, `type`)
  def withDisplayName(displayName: String): this.type                 = set(DisplayName, displayName)
  def withDescription(description: String): this.type                 = set(Description, description)
  def withHeaders(headers: Seq[Parameter]): this.type                 = setArray(Headers, headers)
  def withQueryParameters(queryParameters: Seq[Parameter]): this.type = setArray(QueryParameters, queryParameters)
  def withResponses(responses: Seq[Response]): this.type              = setArray(Responses, responses)
  def withSettings(settings: Settings): this.type                     = set(SettingsField, settings)
  def withQueryString(queryString: Shape): this.type                  = set(QueryString, queryString)

  def normalizeType(): Unit = {
    `type`.option() match {
      case Some(value) =>
        val normalized = value match {
//          case "OAuth 1.0"             => "OAuth 1.0"
//          case "OAuth 2.0"             => "OAuth 2.0"
//          case "Basic Authentication"  => "Basic Authentication"
//          case "Digest Authentication" => "Digest Authentication"
//          case "Pass Through"          => "Pass Through"

//          Normalize oas security scheme types.
          case "oauth2"              => "OAuth 2.0"
          case "basic"               => "Basic Authentication"
          case "apiKey" | "x-apiKey" => "Api Key"
          case other                 => other
        }
        // TODO had to reject sourceAST because it is used by CustomShaclValidator (IDKW)
        set(Type,
            AmfScalar(normalized, `type`.annotations().reject(_.isInstanceOf[SourceAST]) += Inferred()),
            fields.getValue(Type).annotations)
      case _ => // ignore
    }
  }

  override def adopted(parent: String, cycle: Seq[String] = Seq()): this.type =
    if (Option(parent).isDefined && parent.contains("#")) {
      withId(parent + "/" + componentId.urlComponentEncoded)
    } else {
      withId(parent + "#" + componentId.urlComponentEncoded)
    }

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withQueryParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(QueryParameters, result)
    result
  }

  def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withDefaultSettings(): Settings = {
    val settings = Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth1Settings(): OAuth1Settings = {
    val settings = OAuth1Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth2Settings(): OAuth2Settings = {
    val settings = OAuth2Settings()
    set(SettingsField, settings)
    settings
  }

  def withApiKeySettings(): ApiKeySettings = {
    val settings = ApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  def withHttpApiKeySettings(): HttpApiKeySettings = {
    val settings = HttpApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  def withHttpSettings(): HttpSettings = {
    val settings = HttpSettings()
    set(SettingsField, settings)
    settings
  }

  def withOpenIdConnectSettings(): OpenIdConnectSettings = {
    val settings = OpenIdConnectSettings()
    set(SettingsField, settings)
    settings
  }

  def withObject(): ApiKeySettings = {
    val settings = ApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  def cloneScheme(parent: String): SecurityScheme = {
    val scheme: SecurityScheme = SecurityScheme(annotations)
    val cloned                 = scheme.withName(name.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Settings => s.cloneSettings(cloned.id)
          case a: AmfArray =>
            AmfArray(a.values.map {
              case p: Parameter => p.cloneParameter(cloned.id)
              case r: Response  => r.cloneResponse(cloned.id)
              case o            => o
            }, a.annotations)
          case o => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def linkCopy(): SecurityScheme = SecurityScheme().withId(id)

  override def meta: SecuritySchemeModel.type = SecuritySchemeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = name.option().getOrElse("fragment").urlComponentEncoded

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = SecurityScheme.apply
  override def nameField: Field                                                                 = Name
}

object SecurityScheme {
  def apply(): SecurityScheme = apply(Annotations())

  def apply(ast: YPart): SecurityScheme = apply(Annotations(ast))

  def apply(annotations: Annotations): SecurityScheme = SecurityScheme(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SecurityScheme = new SecurityScheme(fields, annotations)
}
