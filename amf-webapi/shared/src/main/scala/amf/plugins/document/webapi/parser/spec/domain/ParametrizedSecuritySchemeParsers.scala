package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.NullSecurity
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, _}
import amf.core.utils.{Lazy, AmfStrings}
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
          ctx.eh.violation(
            UnknownSecuritySchemeErrorSpecification,
            scheme.id,
            s"Security scheme '$name' not found in declarations (and name cannot be 'null').",
            node
          )
      }

      scheme
    case YType.Include =>
      ctx.eh.violation(
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
          ctx.eh.violation(
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
    val flow     = new Lazy[OAuth2Flow](() => OAuth2Flow(map).adopted(settings.id))

    map.key("authorizationUri", entry => (OAuth2FlowModel.AuthorizationUri in flow.getOrCreate).apply(entry))
    map.key("accessTokenUri",
            entry => (OAuth2FlowModel.AccessTokenUri in flow.getOrCreate).allowingAnnotations.apply(entry))
    map.key("flow".asRamlAnnotation, entry => (OAuth2FlowModel.Flow in flow.getOrCreate).apply(entry))
    map.key("authorizationGrants", (OAuth2SettingsModel.AuthorizationGrants in settings).allowingSingleValue)

    val ScopeParser = (n: YNode) => {
      val element = ScalarNode(n).text()
      scheme match {
        case ss: ParametrizedSecurityScheme =>
          ss.scheme.settings match {
            case se: OAuth2Settings if isValidScope(se.flows.headOption, element.toString()) =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
            case _: OAuth2Settings =>
              val scope = Scope().adopted(flow.getOrCreate.id)
              ctx.eh.violation(
                UnknownScopeErrorSpecification,
                scope.id,
                s"Scope '${element.toString}' not found in settings of declared secured by ${ss.scheme.name.value()}.",
                n
              )
              scope
            case _ =>
              Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
          }
        case _ =>
          Scope().set(ScopeModel.Name, ScalarNode(n).text()).adopted(flow.getOrCreate.id)
      }
    }

    map.key("scopes").foreach { entry =>
      (OAuth2FlowModel.Scopes in flow.getOrCreate using ScopeParser).allowingSingleValue.apply(entry)
    }

    flow.option.foreach(f => settings.setArray(OAuth2SettingsModel.Flows, Seq(f)))

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

  private def isValidScope(maybeFlow: Option[OAuth2Flow], scope: String): Boolean =
    maybeFlow.exists(flow => flow.scopes.isEmpty || flow.scopes.map(_.name.value()).contains(scope))

}
