package amf.plugins.document.webapi.parser.spec.declaration
import amf.plugins.domain.webapi.models.security.{Settings, WithSettings}

trait SettingsProducers {
  type SettingsType     = String
  type SettingsProducer = () => Settings
  def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]]
}

object OasLikeCommonSettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = Map(
    "OAuth 1.0" -> Some(scheme.withOAuth1Settings),
    "OAuth 2.0" -> Some(scheme.withOAuth2Settings),
    "Api Key"   -> Some(scheme.withApiKeySettings)
  )
}

object Oas2SettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = {
    val common = OasLikeCommonSettingsProducers.`for`(scheme)
    val specific: Map[SettingsType, Option[SettingsProducer]] = Map(
      "Basic Authentication"  -> None,
      "Digest Authentication" -> None
    )
    common ++ specific
  }
}

object Oas3SettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = {
    val common = OasLikeCommonSettingsProducers.`for`(scheme)
    val specific: Map[SettingsType, Option[SettingsProducer]] = Map(
      "openIdConnect" -> Some(scheme.withOpenIdConnectSettings),
      "http"          -> Some(scheme.withHttpSettings)
    )
    common ++ specific
  }
}

object Async2SettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = {
    val common = OasLikeCommonSettingsProducers.`for`(scheme)
    val specific: Map[SettingsType, Option[SettingsProducer]] = Map(
      "openIdConnect"        -> Some(scheme.withOpenIdConnectSettings),
      "http"                 -> Some(scheme.withHttpSettings),
      "userPassword"         -> None,
      "X509"                 -> None,
      "symmetricEncryption"  -> None,
      "asymmetricEncryption" -> None,
      "httpApiKey"           -> Some(scheme.withHttpApiKeySettings)
    )
    common ++ specific
  }
}
