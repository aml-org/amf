package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.NullSecurity
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, _}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import amf.validations.ParserSideValidations.{UnknownScopeErrorSpecification, UnknownSecuritySchemeErrorSpecification}
import org.yaml.model._

object RamlParametrizedSecuritySchemeParser {
  def parse(producer: String => ParametrizedSecurityScheme)(node: YNode)(
      implicit ctx: WebApiContext): ParametrizedSecurityScheme = {
    RamlParametrizedSecuritySchemeParser(node, producer).parse()
  }
}

case class RamlParametrizedSecuritySchemeParser(node: YNode, producer: String => ParametrizedSecurityScheme)(
    implicit ctx: WebApiContext) {
  def parse(): ParametrizedSecurityScheme = node.tagType match {
    case YType.Null => producer("null").add(Annotations(node) += NullSecurity())
    case YType.Map =>
      val schemeEntry = node.as[YMap].entries.head
      val name        = schemeEntry.key.as[YScalar].text
      val scheme      = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)

          val settings =
            RamlSecuritySettingsParser(schemeEntry.value.as[YMap], declaration.`type`.value(), scheme).parse()

          scheme.set(ParametrizedSecuritySchemeModel.Settings, settings)
        case None =>
          ctx.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$name' not found in declarations (and name cannot be 'null').",
            node
          )
      }

      scheme
    case YType.Include =>
      ctx.violation(
        UnknownSecuritySchemeErrorSpecification,
        "",
        "'securedBy' property doesn't accept !include tag, only references to security schemes.",
        node
      )
      producer("invalid").add(Annotations(node))
    case _ =>
      val name: String = node.as[YScalar].text
      val scheme       = producer(name).add(Annotations(node))

      ctx.declarations.findSecurityScheme(name, SearchScope.Named) match {
        case Some(declaration) =>
          scheme.fields.setWithoutId(ParametrizedSecuritySchemeModel.Scheme, declaration, Annotations())
          scheme
        case None =>
          ctx.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$name' not found in declarations.",
            node
          )
          scheme
      }
  }
}

object RamlSecuritySettingsParser {
  def parse(scheme: SecurityScheme)(node: YNode)(implicit ctx: WebApiContext): Settings = {
    RamlSecuritySettingsParser(node.as[YMap], scheme.`type`.value(), scheme).parse()
  }
}

case class RamlSecuritySettingsParser(map: YMap, `type`: String, scheme: DomainElement with WithSettings)(
    implicit val ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Settings = {
    val result = `type` match {
      case "OAuth 1.0"   => oauth1()
      case "OAuth 2.0"   => oauth2()
      case `apiKeyConst` => apiKey()
      case _             => dynamicSettings(scheme.withDefaultSettings())
    }

    AnnotationParser(result, map, List(VocabularyMappings.securitySettings))(ctx).parse()

    result.add(Annotations(map))
  }

  val apiKeyConst: String = "apiKey".asOasExtension

  def dynamicSettings(settings: Settings, properties: String*): Settings = {
    val entries: IndexedSeq[YMapEntry] = map.entries.filterNot { entry =>
      val key: String = entry.key.as[YScalar].text
      properties.contains(key) || isRamlAnnotation(key)
    }

    if (entries.nonEmpty) {
      val node = DataNodeParser(YNode(YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))),
                                parent = Some(settings.id)).parse()
      settings.set(SettingsModel.AdditionalProperties, node)
    }
    settings
  }

  private def apiKey() = {
    val settings = scheme.withApiKeySettings()
    map.key("name", ApiKeySettingsModel.Name in settings)
    map.key("in", ApiKeySettingsModel.In in settings)
    dynamicSettings(settings, "name", "in")
  }

  private def oauth2() = {
    val settings = scheme.withOAuth2Settings()

    map.key("authorizationUri", OAuth2SettingsModel.AuthorizationUri in settings)
    map.key("accessTokenUri", (OAuth2SettingsModel.AccessTokenUri in settings).allowingAnnotations)
    map.key("flow".asRamlAnnotation, OAuth2SettingsModel.Flow in settings)
    map.key("authorizationGrants", (OAuth2SettingsModel.AuthorizationGrants in settings).allowingSingleValue)

    val ScopeParser = (n: YNode) => {
      val element = ScalarNode(n).text()
      scheme match {
        case ss: ParametrizedSecurityScheme =>
          ss.scheme.settings match {
            case se: OAuth2Settings if se.scopes.map(_.name.value()).contains(element.toString) =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(scheme.id)
            case _: OAuth2Settings =>
              val scope = Scope().adopted(scheme.id)
              ctx.violation(
                UnknownScopeErrorSpecification,
                scope.id,
                s"Scope '${element.toString}' not found in settings of declared secured by ${ss.scheme.name.value()}.",
                n
              )
              scope
            case _ =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(scheme.id)
          }
        case _ =>
          Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(scheme.id)
      }
    }

    map.key("scopes", (OAuth2SettingsModel.Scopes in settings using ScopeParser).allowingSingleValue)

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
