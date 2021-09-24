package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security._
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.{
  Async2SettingsProducers,
  SettingsProducers,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.oas.parser.domain.{OAuth2FlowValidations, OasLikeSecuritySettingsParser}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.spec.common.parser.AnnotationParser
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
      settings.setWithoutId(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
    })

    map.key("in", entry => {
      val value = ScalarNode(entry.value)
      settings.setWithoutId(HttpApiKeySettingsModel.In, value.string(), Annotations(entry))
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

    AnnotationParser(settings, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("flows")

    settings
  }

  private def parseFlows(entry: YMapEntry, settings: OAuth2Settings): Unit = {
    val flows = entry.value.as[YMap].entries.map(parseFlow(settings, _))
    flows.foreach(OAuth2FlowValidations.validateFlowFields(_, ctx.eh, entry))
    settings.fields.setWithoutId(OAuth2SettingsModel.Flows,
                        AmfArray(flows, Annotations(entry.value)),
                        Annotations(entry.value))
  }

  private def parseFlow(settings: OAuth2Settings, flowEntry: YMapEntry) = {
    val flow    = OAuth2Flow(flowEntry)
    val flowMap = flowEntry.value.as[YMap]
    val flowKey = ScalarNode(flowEntry.key).string()

    flow.setWithoutId(OAuth2FlowModel.Flow, flowKey, Annotations(flowEntry.key))

    flowMap.key("authorizationUrl", OAuth2FlowModel.AuthorizationUri in flow)
    flowMap.key("tokenUrl", OAuth2FlowModel.AccessTokenUri in flow)
    flowMap.key("refreshUrl", OAuth2FlowModel.RefreshUri in flow)

    parseScopes(flow, flowMap)
    flow
  }
  override def vendorSpecificSettingsProducers(): SettingsProducers = Async2SettingsProducers
}
