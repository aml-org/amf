package amf.spec.oas

/**
  * Created by hernan.najles on 10/17/17.
  */
case class OasSecuritySchemeType(text: String, isOas: Boolean) {}

object OasSecuritySchemeTypeMapping {

  object OAuth20Oas   extends OasSecuritySchemeType("oauth2", true)
  object BasicAuthOas extends OasSecuritySchemeType("basic", true)
  object ApiKeyOas    extends OasSecuritySchemeType("apiKey", true)

  def fromText(text: String): OasSecuritySchemeType = text match {
    case "OAuth 2.0"            => OasSecuritySchemeType("oauth2", true)
    case "Basic Authentication" => OasSecuritySchemeType("basic", true)
    case "x-apiKey"             => OasSecuritySchemeType("apiKey", true)
    case s                      => OasSecuritySchemeType(s, false)
  }
}
