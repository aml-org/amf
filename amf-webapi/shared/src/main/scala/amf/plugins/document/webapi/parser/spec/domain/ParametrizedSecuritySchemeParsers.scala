package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import org.yaml.model.{YMap, YNode, YType}

object RamlParametrizedSecuritySchemeParser {
  def parse(producer: String => ParametrizedSecurityScheme)(node: YNode)(
      implicit ctx: WebApiContext): ParametrizedSecurityScheme = {
    RamlParametrizedSecuritySchemeParser(node, producer).parse()
  }
}

case class RamlParametrizedSecuritySchemeParser(node: YNode, producer: String => ParametrizedSecurityScheme)(
    implicit ctx: WebApiContext) {
  def parse(): ParametrizedSecurityScheme = node.tagType match {
    case YType.Null => producer("null").add(Annotations(node))
    case YType.Str =>
      val name: String = node
      val scheme       = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) => {
          scheme.fields.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations())
          scheme
        }
        case None =>
          ctx.violation(scheme.id, s"Security scheme '$name' not found in declarations.", node)
          scheme
      }

    case YType.Map =>
      val schemeEntry = node.as[YMap].entries.head
      val name        = schemeEntry.key
      val scheme      = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)

          val settings = RamlSecuritySettingsParser(schemeEntry.value.as[YMap], declaration.`type`, scheme).parse()

          scheme.set(ParametrizedSecuritySchemeModel.Settings, settings)
        case None =>
          ctx.violation("", s"Security scheme '$name' not found in declarations (and name cannot be 'null').", node)
      }

      scheme
    case _ => throw new Exception(s"Invalid type ${node.tagType}")
  }
}

object RamlSecuritySettingsParser {
  def parse(scheme: SecurityScheme)(node: YNode)(implicit ctx: WebApiContext): Settings = {
    RamlSecuritySettingsParser(node.as[YMap], scheme.`type`, scheme).parse()
  }
}

case class RamlSecuritySettingsParser(map: YMap, `type`: String, scheme: WithSettings)(implicit val ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Settings = {
    val result = `type` match {
      case "OAuth 1.0" => oauth1()
      case "OAuth 2.0" => oauth2()
      case "x-apiKey"  => apiKey()
      case _           => dynamicSettings(scheme.withDefaultSettings())
    }

    AnnotationParser(result, map)(ctx).parse()

    result.add(Annotations(map))
  }

  def dynamicSettings(settings: Settings, properties: String*): Settings = {
    val entries = map.entries.filterNot { entry =>
      val key: String = entry.key
      properties.contains(key) || isRamlAnnotation(key)
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
      val value = ScalarNode(entry.value)
      s.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
    })

    map.key("in", entry => {
      val value = ScalarNode(entry.value)
      s.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
    })

    dynamicSettings(s, "name", "in")
  }

  private def oauth2() = {
    val settings = scheme.withOAuth2Settings()

    map.key("authorizationUri", OAuth2SettingsModel.AuthorizationUri in settings)
    map.key("accessTokenUri", (OAuth2SettingsModel.AccessTokenUri in settings).allowingAnnotations)
    map.key("(flow)", OAuth2SettingsModel.Flow in settings)
    map.key("authorizationGrants", OAuth2SettingsModel.AuthorizationGrants in settings)

    map.key(
      "scopes",
      entry => {
        val value = ArrayNode(entry.value)
          .text()
          .values
          .map(v => Scope().set(ScopeModel.Name, v).adopted(scheme.id))
        settings.setArray(OAuth2SettingsModel.Scopes, value, Annotations(entry))
      }
    )

    dynamicSettings(settings, "authorizationUri", "accessTokenUri", "authorizationGrants", "scopes")
  }

  private def oauth1() = {
    val settings = scheme.withOAuth1Settings()

    map.key("requestTokenUri", (OAuth1SettingsModel.RequestTokenUri in settings).allowingAnnotations)
    map.key("authorizationUri", (OAuth1SettingsModel.AuthorizationUri in settings).allowingAnnotations)
    map.key("tokenCredentialsUri", (OAuth1SettingsModel.TokenCredentialsUri in settings).allowingAnnotations)
    map.key("signatures", OAuth1SettingsModel.Signatures in settings)

    dynamicSettings(settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
  }
}
