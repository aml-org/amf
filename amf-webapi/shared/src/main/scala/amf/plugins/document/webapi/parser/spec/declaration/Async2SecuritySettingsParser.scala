package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, OAuth2FlowValidations}
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security._
import org.yaml.model.{YMap, YMapEntry}

import scala.language.implicitConversions

class Async2SecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: AsyncWebApiContext)
    extends OasLikeSecuritySettingsParser(map, scheme) {

  override def parse(): Option[Settings] = {
    produceSettings.map { settings =>
      val parsedSettings = settings match {
        case s: OAuth1Settings        => parseOauth1Settings(s)
        case s: OAuth2Settings        => parseOauth2Settings(s)
        case s: ApiKeySettings        => parseApiKeySettings(s)
        case s: HttpApiKeySettings    => parseHttpApiKeySettings(s)
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

  def parseHttpApiKeySettings(settings: HttpApiKeySettings): HttpApiKeySettings = {

    map.key("name", entry => {
      val value = ScalarNode(entry.value)
      settings.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
    })

    map.key("in", entry => {
      val value = ScalarNode(entry.value)
      settings.set(HttpApiKeySettingsModel.In, value.string(), Annotations(entry))
    })

    map.key(
      "settings".asOasExtension,
      entry => parseDynamicSettings(entry.value.as[YMap], settings, "name", "in")
    )

    settings
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
    map.key("flows").foreach(parseFlows(_, settings))

    map.key(
      "settings".asOasExtension,
      entry => parseDynamicSettings(entry.value.as[YMap], settings, "authorizationGrants")
    )

    AnnotationParser(settings, map).parseOrphanNode("flows")

    settings
  }

  private def parseFlows(entry: YMapEntry, settings: OAuth2Settings): Unit = {
    val flows = entry.value.as[YMap].entries.map(parseFlow(settings, _))
    flows.foreach(OAuth2FlowValidations.validateFlowFields(_, ctx.eh, entry))
    settings.fields.set(settings.id,
                        OAuth2SettingsModel.Flows,
                        AmfArray(flows, Annotations(entry.value)),
                        Annotations(entry.value))
  }

  private def parseFlow(settings: OAuth2Settings, flowEntry: YMapEntry) = {
    val flow    = OAuth2Flow(flowEntry)
    val flowMap = flowEntry.value.as[YMap]
    val flowKey = ScalarNode(flowEntry.key).string()

    flow.set(OAuth2FlowModel.Flow, flowKey, Annotations(flowEntry.key))

    flow.adopted(settings.id)

    flowMap.key("authorizationUrl", OAuth2FlowModel.AuthorizationUri in flow)
    flowMap.key("tokenUrl", OAuth2FlowModel.AccessTokenUri in flow)
    flowMap.key("refreshUrl", OAuth2FlowModel.RefreshUri in flow)

    parseScopes(flow, flowMap)
    flow
  }
  override def vendorSpecificSettingsProducers(): SettingsProducers = Async2SettingsProducers
}
