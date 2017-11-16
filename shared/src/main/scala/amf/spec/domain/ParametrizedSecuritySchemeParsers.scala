package amf.spec.domain

import amf.domain.Annotations
import amf.domain.security.{ParametrizedSecurityScheme, Scope, Settings, WithSettings}
import amf.metadata.domain.security._
import amf.parser.YMapOps
import amf.spec.{ParserContext, SearchScope}
import amf.spec.common._
import org.yaml.model.{YMap, YNode, YType}

/**
  *
  */
case class RamlParametrizedSecuritySchemeParser(s: YNode, producer: String => ParametrizedSecurityScheme)(
    implicit ctx: ParserContext) {
  def parse(): ParametrizedSecurityScheme = s.tagType match {
    case YType.Null => producer("null").add(Annotations(s))
    case YType.Str =>
      val name: String = s
      val scheme       = producer(name).add(Annotations(s))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) => scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration.id)
        case None =>
          ctx.violation(scheme.id, s"Security scheme '$name' not found in declarations.", s)
          scheme
      }

    case YType.Map =>
      val schemeEntry = s.as[YMap].entries.head
      val name        = schemeEntry.key
      val scheme      = producer(name).add(Annotations(s))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration.id)

          val settings = RamlSecuritySettingsParser(schemeEntry.value.as[YMap], declaration.`type`, scheme).parse()

          scheme.set(ParametrizedSecuritySchemeModel.Settings, settings)
        case None =>
          ctx.violation("", s"Security scheme '$name' not found in declarations (and name cannot be 'null').", s)
      }

      scheme
    case _ => throw new Exception(s"Invalid type ${s.tagType}")
  }
}

case class RamlSecuritySettingsParser(map: YMap, `type`: String, scheme: WithSettings)(
  implicit val ctx: ParserContext){
  def parse(): Settings = {
    val result = `type` match {
      case "OAuth 1.0" => oauth1()
      case "OAuth 2.0" => oauth2()
      case "x-apiKey"  => apiKey()
      case _           => dynamicSettings(scheme.withDefaultSettings())
    }

    AnnotationParser(() => result, map).parse()

    result.add(Annotations(map))
  }

  def dynamicSettings(settings: Settings, properties: String*): Settings = {
    val entries = map.entries.filterNot { entry =>
      val key: String = entry.key
      properties.contains(key) || WellKnownAnnotation.isRamlAnnotation(key)
    }

    if (entries.nonEmpty) {
      val node = DataNodeParser(YNode(YMap(entries)), parent = Some(settings.id)).parse()
      settings.set(SettingsModel.AdditionalProperties, node)
    }
    settings
  }

  private def apiKey() = {
    val s = scheme.withApiKeySettings()
    map.key("name", entry => {
      val value = ValueNode(entry.value)
      s.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
    })

    map.key("in", entry => {
      val value = ValueNode(entry.value)
      s.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
    })

    dynamicSettings(s, "name", "in")
  }

  private def oauth2() = {
    val settings = scheme.withOAuth2Settings()
    map.key("authorizationUri", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth2SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
    })

    map.key("accessTokenUri", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth2SettingsModel.AccessTokenUri, value.string(), Annotations(entry))
    })

    map.key("(flow)", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth2SettingsModel.Flow, value.string(), Annotations(entry))
    })

    map.key(
      "authorizationGrants",
      entry => {
        val value = ArrayNode(entry.value)
        settings.set(OAuth2SettingsModel.AuthorizationGrants, value.strings(), Annotations(entry))
      }
    )

    map.key(
      "scopes",
      entry => {
        val value = ArrayNode(entry.value)
          .strings()
          .values
          .map(v => Scope().set(ScopeModel.Name, v).adopted(scheme.id))
        settings.setArray(OAuth2SettingsModel.Scopes, value, Annotations(entry))
      }
    )

    dynamicSettings(settings, "authorizationUri", "accessTokenUri", "authorizationGrants", "scopes")
  }

  private def oauth1() = {
    val settings = scheme.withOAuth1Settings()
    map.key("requestTokenUri", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth1SettingsModel.RequestTokenUri, value.string(), Annotations(entry))
    })

    map.key("authorizationUri", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth1SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
    })

    map.key("tokenCredentialsUri", entry => {
      val value = ValueNode(entry.value)
      settings.set(OAuth1SettingsModel.TokenCredentialsUri, value.string(), Annotations(entry))
    })

    map.key("signatures", entry => {
      val value = ArrayNode(entry.value)
      settings.set(OAuth1SettingsModel.Signatures, value.strings(), Annotations(entry))
    })

    dynamicSettings(settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
  }
}
