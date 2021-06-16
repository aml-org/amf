package amf.apicontract.internal.spec.oas.parser

import amf.apicontract.internal.spec.common.parser.{Oas3SettingsProducers, SettingsProducers, WebApiShapeParserContextAdapter}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.Oas3SettingsProducers
import amf.apicontract.internal.metamodel.domain.security.{HttpSettingsModel, OAuth2FlowModel, OAuth2SettingsModel, OpenIdConnectSettingsModel}
import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import org.yaml.model.{YMap, YMapEntry}

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

    AnnotationParser(settings, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("flows")

    settings
  }

  private def parseFlows(entry: YMapEntry, settings: OAuth2Settings): Unit = {
    val flows = entry.value.as[YMap].entries.map(parseFlow(settings.id, _))
    settings.set(OAuth2SettingsModel.Flows, AmfArray(flows, Annotations(entry.value)), Annotations(entry))
  }

  private def parseFlow(parent: String, flowEntry: YMapEntry) = {
    val flow    = OAuth2Flow(flowEntry)
    val flowMap = flowEntry.value.as[YMap]
    val flowKey = ScalarNode(flowEntry.key).string()

    flow.set(OAuth2FlowModel.Flow, flowKey, Annotations(flowEntry.key))

    flow.adopted(parent)

    flowMap.key("authorizationUrl", OAuth2FlowModel.AuthorizationUri in flow)
    flowMap.key("tokenUrl", OAuth2FlowModel.AccessTokenUri in flow)
    flowMap.key("refreshUrl", OAuth2FlowModel.RefreshUri in flow)

    parseScopes(flow, flowMap)

    OAuth2FlowValidations.validateFlowFields(flow, ctx.eh, flowEntry)

    ctx.closedShape(flow.id, flowMap, flow.flow.value())
    flow
  }

  override def vendorSpecificSettingsProducers(): SettingsProducers = Oas3SettingsProducers
}
