package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.security.{Settings, WithSettings}

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

object Oas31SettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = {
    val common = Oas3SettingsProducers.`for`(scheme)
    val specific: Map[SettingsType, Option[SettingsProducer]] = Map(
      "mutualTLS" -> Some(scheme.withMutualTLSSettings)
    )
    common ++ specific
  }
}

object Async2SettingsProducers extends SettingsProducers {
  override def `for`(scheme: WithSettings): Map[SettingsType, Option[SettingsProducer]] = {
    val common = OasLikeCommonSettingsProducers.`for`(scheme)
    val specific: Map[SettingsType, Option[SettingsProducer]] = Map(
      "userPassword"         -> None,
      "X509"                 -> None,
      "symmetricEncryption"  -> None,
      "asymmetricEncryption" -> None,
      "httpApiKey"           -> Some(scheme.withHttpApiKeySettings),
      "http"                 -> Some(scheme.withHttpSettings),
      "openIdConnect"        -> Some(scheme.withOpenIdConnectSettings),
      "plain"                -> None,
      "scramSha256"          -> None,
      "scramSha512"          -> None,
      "gssapi"               -> None
    )
    common ++ specific
  }
}
