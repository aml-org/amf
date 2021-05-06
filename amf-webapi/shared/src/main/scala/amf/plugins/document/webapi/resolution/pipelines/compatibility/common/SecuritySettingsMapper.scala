package amf.plugins.document.webapi.resolution.pipelines.compatibility.common

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.security.{
  ApiKeySettings,
  OAuth1Settings,
  OAuth2Settings,
  OpenIdConnectSettings,
  SecurityScheme
}

abstract class SecuritySettingsMapper() extends TransformationStep {

  def fixOauth2(oauth2: OAuth2Settings): Unit

  def fixApiKey(security: SecurityScheme, apiKey: ApiKeySettings): Unit = {
    if (security.queryParameters.nonEmpty) {
      apiKey.withIn("query")
      apiKey.withName(security.queryParameters.head.name.value())
    } else if (security.headers.nonEmpty) {
      apiKey.withIn("header")
      apiKey.withName(security.headers.head.name.value())
    } else {
      apiKey.withIn("query")
      apiKey.withName("")
    }
  }

  private def fixSettings(security: SecurityScheme): Unit = {
    security.settings match {
      case oauth2: OAuth2Settings => fixOauth2(oauth2)
      case apiKey: ApiKeySettings => fixApiKey(security, apiKey)
      case null if security.`type`.option().getOrElse("") == "x-amf-apiKey" =>
        fixApiKey(security, security.withApiKeySettings())
      case _ => // ignore
    }
  }

  private def fixSettings(d: DeclaresModel): Unit = d.declares.foreach {
    case security: SecurityScheme => fixSettings(security)
    case _                        => // ignore
  }

  def removeUnsupportedSchemes(d: DeclaresModel): DeclaresModel = {
    val filteredDeclarations = d.declares.filter {
      case sec: SecurityScheme =>
        sec.settings match {
          case _: OAuth1Settings        => false
          case _: OpenIdConnectSettings => false
          case _                        => true

        }
      case _ => true
    }

    d.withDeclares(filteredDeclarations)
  }

  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = model match {
    case d: DeclaresModel =>
      try {
        fixSettings(d)
      } catch {
        case _: Throwable => // ignore: we don't want this to break anything
      }
      model
    case _ => model
  }
}
