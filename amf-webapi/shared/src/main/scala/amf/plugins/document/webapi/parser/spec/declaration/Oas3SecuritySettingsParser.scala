package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.VirtualObject
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import org.yaml.model.{YMap, YMapEntry}
import amf.plugins.domain.webapi.metamodel.security.{
  OAuth2SettingsModel,
  HttpSettingsModel,
  OAuth2FlowModel,
  OpenIdConnectSettingsModel
}
import amf.plugins.domain.webapi.models.security._
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.core.utils.{Lazy, AmfStrings}

class Oas3SecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: OasWebApiContext)
    extends OasLikeSecuritySettingsParser(map, scheme) {

  override def parse(): Option[Settings] = {
    produceSettings.map { settings =>
      val parsedSettings = settings match {
        case s: OAuth1Settings        => parseOauth1Settings(s)
        case s: OAuth2Settings        => parseOauth2Settings(s)
        case s: ApiKeySettings        => parseApiKeySettings(s)
        case s: OpenIdConnectSettings => parseOpenIdConnectSettings(s)
        case s: HttpSettings          => parseHttpSettings(s)
        case defaultSettings =>
          map
            .key("settings".asOasExtension)
            .map(entry => parseDynamicSettings(entry.value.as[YMap], defaultSettings))
            .getOrElse(defaultSettings)
      }
      parseAnnotations(parsedSettings)
    }
  }

  def parseOpenIdConnectSettings(settings: OpenIdConnectSettings): OpenIdConnectSettings = {
    map.key("openIdConnectUrl", OpenIdConnectSettingsModel.Url in settings)
    map.key("settings".asOasExtension, entry => parseDynamicSettings(entry.value.as[YMap], settings))

    settings
  }

  def parseHttpSettings(settings: HttpSettings): HttpSettings = {
    map.key("scheme", HttpSettingsModel.Scheme in settings)
    map.key("bearerFormat", HttpSettingsModel.BearerFormat in settings)

    settings
  }

  override def parseOauth2Settings(settings: OAuth2Settings): OAuth2Settings = {
    map.key("flows", parseFlows(_, settings))

    map.key(
      "settings".asOasExtension,
      entry => parseDynamicSettings(entry.value.as[YMap], settings, "authorizationGrants")
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
  override def vendorSpecificSettingsProducers(): SettingsProducers = Oas3SettingsProducers
}
