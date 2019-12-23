package amf.plugins.document.webapi.parser.spec.declaration
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorSecurityScheme
import amf.core.model.domain.AmfScalar
import amf.plugins.features.validation.CoreValidations
import amf.core.annotations.{LexicalInformation, VirtualObject}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.core.utils.Lazy
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, DataNodeParser}
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import org.yaml.model.{YNode, YScalar, YPart, YMapEntry, YType, YMap}
import amf.validations.ParserSideValidations.{
  MissingSecuritySchemeErrorSpecification,
  CrossSecurityWarningSpecification
}
import amf.plugins.domain.webapi.metamodel.security.{
  OAuth1SettingsModel,
  SecuritySchemeModel,
  ApiKeySettingsModel,
  HttpSettingsModel,
  OpenIdConnectSettingsModel,
  SettingsModel,
  OAuth2FlowModel,
  OAuth2SettingsModel,
  ScopeModel
}
import amf.plugins.domain.webapi.models.security.{
  Settings,
  OpenIdConnectSettings,
  SecurityScheme,
  Scope,
  HttpSettings,
  OAuth2Flow,
  OAuth2Settings
}
import amf.plugins.document.webapi.parser.spec.toRaml
import amf.core.parser.{Annotations, ScalarNode, Range, SearchScope}
import amf.core.parser.YMapOps
import amf.core.utils.AmfStrings

case class OasSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasWebApiContext)
    extends SecuritySchemeParser(part, adopt) {
  def parse(): SecurityScheme = {
    val node = getNode

    ctx.link(node) match {
      case Left(link) => parseReferenced(link, node, adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme())
        val map    = value.as[YMap]

        // 3 stages
        // 2 pipes

        map.key("type", SecuritySchemeModel.Type in scheme)

        scheme.`type`.option() match {
          case Some(s) if s.startsWith("x-") =>
            ctx.warning(
              CrossSecurityWarningSpecification,
              scheme.id,
              Some(SecuritySchemeModel.Type.value.iri()),
              s"RAML 1.0 extension security scheme type '$s' detected in OAS 2.0 spec",
              scheme.`type`.annotations().find(classOf[LexicalInformation]),
              Some(ctx.rootContextDocument)
            )
          case Some("OAuth 1.0" | "OAuth 2.0" | "Basic Authentication" | "Digest Authentication" | "Pass Through") =>
            ctx.warning(
              CrossSecurityWarningSpecification,
              scheme.id,
              Some(SecuritySchemeModel.Type.value.iri()),
              s"RAML 1.0 security scheme type detected in OAS 2.0 spec",
              scheme.`type`.annotations().find(classOf[LexicalInformation]),
              Some(ctx.rootContextDocument)
            )
          case _ =>
        }

        map.key(
          "type",
          value => {
            // we need to check this because of the problem parsing nulls like empty strings of value null
            if (value.value.tagType == YType.Null && scheme.`type`.option().contains("")) {
              ctx.violation(
                MissingSecuritySchemeErrorSpecification,
                scheme.id,
                Some(SecuritySchemeModel.Type.value.iri()),
                "Security Scheme must have a mandatory value from 'oauth2', 'basic' or 'apiKey'",
                Some(LexicalInformation(Range(map.range))),
                Some(ctx.rootContextDocument)
              )
            }
          }
        )

        scheme.normalizeType() // normalize the common type

        map.key("displayName".asOasExtension, SecuritySchemeModel.DisplayName in scheme)
        map.key("description", SecuritySchemeModel.Description in scheme)

        RamlDescribedByParser("describedBy".asOasExtension, map, scheme)(toRaml(ctx)).parse()

        ctx.factory
          .securitySettingsParser(map, scheme)
          .parse()
          .foreach(scheme.set(SecuritySchemeModel.Settings, _, Annotations(map)))

        AnnotationParser(scheme, map).parse()

        scheme
    }
  }

  def parseReferenced(parsedUrl: String, node: YNode, adopt: SecurityScheme => SecurityScheme): SecurityScheme = {
    ctx.declarations
      .findSecurityScheme(parsedUrl, SearchScope.Fragments)
      .map(securityScheme => {
        val scheme: SecurityScheme = securityScheme.link(parsedUrl, Annotations(node))
        adopt(scheme)
        scheme
      })
      .getOrElse {
        ctx.obtainRemoteYNode(parsedUrl) match {
          case Some(schemeNode) =>
            OasSecuritySchemeParser(schemeNode, adopt).parse()
          case None =>
            ctx.violation(CoreValidations.UnresolvedReference,
                          "",
                          s"Cannot find security scheme reference $parsedUrl",
                          Annotations(node))
            adopt(ErrorSecurityScheme(parsedUrl, node))
        }
      }
  }
}

trait OasSecuritySettingsParser extends SpecParserOps {
  def parse(): Option[Settings]
}

/** OAS3 security scheme settings. Extends OAS2 ones because we reuse the existing OAS2 ones. */
class Oas3SecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: OasWebApiContext)
    extends Oas2SecuritySettingsParser(map, scheme) {

  override def parse(): Option[Settings] =
    parseAnnotations(super.parse() match {
      case resolved @ Some(_) => resolved
      case None               => parseSettings()
    })

  private def parseSettings(): Option[Settings] = scheme.`type`.value() match {
    case "openIdConnect" => Some(openIdConnect())
    case "http"          => Some(http())
    case _               => None
  }

  private def openIdConnect(): OpenIdConnectSettings = {
    val settings = scheme.withOpenIdConnectSettings()

    map.key("openIdConnectUrl", OpenIdConnectSettingsModel.Url in settings)
    map.key("settings".asOasExtension, entry => dynamicSettings(entry.value.as[YMap], settings))

    settings
  }

  private def http(): HttpSettings = {
    val settings = scheme.withHttpSettings()

    map.key("scheme", HttpSettingsModel.Scheme in settings)
    map.key("bearerFormat", HttpSettingsModel.BearerFormat in settings)

    settings
  }

  override protected def oauth2(): OAuth2Settings = {
    val settings = scheme.withOAuth2Settings()

    map.key("flows", parseFlows(_, settings))

    map.key(
      "settings".asOasExtension,
      entry => dynamicSettings(entry.value.as[YMap], settings, "authorizationGrants")
    )

    AnnotationParser(settings, map).parseOrphanNode("flows")

    settings
  }

  private def parseFlows(entry: YMapEntry, settings: OAuth2Settings): Unit =
    entry.value.as[YMap].entries.foreach(parseFlow(settings, _))

  private def parseFlow(settings: OAuth2Settings, flowEntry: YMapEntry) = {
    val flow    = OAuth2Flow(flowEntry)
    val flowMap = flowEntry.value.as[YMap]
    val flowKey = ScalarNode(flowEntry.key).string()

    flow.set(OAuth2FlowModel.Flow, flowKey)

    flow.adopted(settings.id)

    flowMap.key("authorizationUrl", OAuth2FlowModel.AuthorizationUri in flow)
    flowMap.key("tokenUrl", OAuth2FlowModel.AccessTokenUri in flow)
    flowMap.key("refreshUrl", OAuth2FlowModel.RefreshUri in flow)

    parseScopes(flow, flowMap)

    settings.add(OAuth2SettingsModel.Flows, flow)
  }
}

case class Oas2SecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: OasWebApiContext)
    extends OasSecuritySettingsParser {
  override def parse(): Option[Settings] = {
    val result = scheme.`type`.value() match {
      case "OAuth 1.0" => Some(oauth1())
      case "OAuth 2.0" => Some(oauth2())
      case "Api Key"   => Some(apiKey())
      case _ =>
        map
          .key("settings".asOasExtension)
          .map(entry => dynamicSettings(entry.value.as[YMap], scheme.withDefaultSettings()))
    }
    result.foreach(_.annotations += VirtualObject())
    parseAnnotations(result)
  }

  protected def parseAnnotations(result: Option[Settings]): Option[Settings] = result.map { ss =>
    AnnotationParser(ss, map).parse()
    ss.add(Annotations(map))
  }

  protected def dynamicSettings(xSettings: YMap, settings: Settings, properties: String*): Settings = {
    val entries = xSettings.entries.filterNot { entry =>
      val key: String = entry.key.as[YScalar].text
      properties.contains(key) || isOasAnnotation(key)
    }

    if (entries.nonEmpty) {
      val node = DataNodeParser(YNode(YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))),
                                parent = Some(settings.id)).parse()
      settings.set(SettingsModel.AdditionalProperties, node)
    }

    AnnotationParser(scheme, xSettings).parse()

    settings
  }

  private def apiKey() = {
    val settings = scheme.withApiKeySettings()

    map.key("name", entry => {
      val value = ScalarNode(entry.value)
      settings.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
    })

    map.key("in", entry => {
      val value = ScalarNode(entry.value)
      settings.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
    })

    map.key(
      "settings".asOasExtension,
      entry => dynamicSettings(entry.value.as[YMap], settings, "name", "in")
    )

    settings
  }

  protected def oauth2(): OAuth2Settings = {
    val settings = scheme.withOAuth2Settings()
    val flow     = new Lazy[OAuth2Flow](() => OAuth2Flow(map).adopted(settings.id))

    map.key("authorizationUrl",
            entry =>
              flow.getOrCreate.set(OAuth2FlowModel.AuthorizationUri,
                                   ScalarNode(entry.value).string(),
                                   Annotations(entry.value)))
    map.key("tokenUrl",
            entry =>
              flow.getOrCreate.set(OAuth2FlowModel.AccessTokenUri,
                                   ScalarNode(entry.value).string(),
                                   Annotations(entry.value)))

    map.key(
      "flow",
      entry => {
        val value = ScalarNode(entry.value)
        flow.getOrCreate.set(OAuth2FlowModel.Flow, value.string(), Annotations(entry))
      }
    )

    map.key("scopes").foreach(_ => parseScopes(flow.getOrCreate, map))

    map.key(
      "settings".asOasExtension,
      entry => {
        val xSettings = entry.value.as[YMap]

        xSettings.key("authorizationGrants", OAuth2SettingsModel.AuthorizationGrants in settings)

        dynamicSettings(xSettings, settings, "authorizationGrants")
      }
    )

    AnnotationParser(settings, map).parseOrphanNode("scopes")

    flow.option.foreach { f =>
      f.adopted(settings.id)
      settings.add(OAuth2SettingsModel.Flows, f)
    }

    settings
  }

  protected def parseScopes(flow: OAuth2Flow, map: YMap): Unit = map.key("scopes").foreach { entry =>
    val scopeMap = entry.value.as[YMap]
    val scopes   = scopeMap.entries.filterNot(entry => isOasAnnotation(entry.key)).map(parseScope(_, flow.id))
    flow.setArray(OAuth2FlowModel.Scopes, scopes, Annotations(entry))
  }

  private def parseScope(scopeEntry: YMapEntry, parentId: String) = {
    val name: String        = scopeEntry.key.as[YScalar].text
    val description: String = scopeEntry.value

    Scope(scopeEntry)
      .set(ScopeModel.Name, AmfScalar(name), Annotations(scopeEntry.key))
      .set(ScopeModel.Description, AmfScalar(description), Annotations(scopeEntry.value))
      .adopted(parentId)
  }

  private def oauth1() = {
    val settings = scheme.withOAuth1Settings()

    map.key(
      "settings".asOasExtension,
      entry => {
        val map = entry.value.as[YMap]

        map.key("requestTokenUri", OAuth1SettingsModel.RequestTokenUri in settings)
        map.key("authorizationUri", OAuth1SettingsModel.AuthorizationUri in settings)
        map.key("tokenCredentialsUri", OAuth1SettingsModel.TokenCredentialsUri in settings)
        map.key("signatures", OAuth1SettingsModel.Signatures in settings)

        dynamicSettings(map, settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
      }
    )

    settings
  }
}
