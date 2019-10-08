package amf.plugins.document.webapi.parser.spec.oas

case class OasSecuritySchemeType(text: String, isOas: Boolean)

object OasSecuritySchemeTypeMapping {

  object OAuth20Oas    extends OasSecuritySchemeType("oauth2", true)
  object BasicAuthOas  extends OasSecuritySchemeType("basic", true)
  object ApiKeyOas     extends OasSecuritySchemeType("apiKey", true)
  object Http          extends OasSecuritySchemeType("http", true)
  object OpenIdConnect extends OasSecuritySchemeType("openIdConnect", true)

  def fromText(text: String): OasSecuritySchemeType = text match {
    case "OAuth 2.0"            => OAuth20Oas
    case "Basic Authentication" => BasicAuthOas
    case "Api Key"              => ApiKeyOas
    case "http"                 => Http
    case "openIdConnect"        => OpenIdConnect
    case s                      => OasSecuritySchemeType(s, isOas = false)
  }
}
