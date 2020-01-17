package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.metamodel.Field
import amf.core.parser.{ScalarNode, YMapOps}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.webapi.metamodel.security.{
  HttpSettingsModel,
  OAuth2FlowModel,
  OAuth2SettingsModel,
  OpenIdConnectSettingsModel
}
import amf.plugins.domain.webapi.models.security._
import amf.validations.ParserSideValidations.{MissingOAuthFlowField, InvalidOAuth2FlowName}
import org.yaml.model.{YMap, YMapEntry}

object Oas3SecuritySettingsParser {
  case class ParticularFlow(name: String, requiredFields: List[FlowField])
  case class FlowField(name: String, field: Field)

  val authorizationUrl: FlowField = FlowField("authorizationUrl", OAuth2FlowModel.AuthorizationUri)
  val tokenUrl: FlowField         = FlowField("tokenUrl", OAuth2FlowModel.AccessTokenUri)
  val refreshUrl: FlowField       = FlowField("refreshUrl", OAuth2FlowModel.RefreshUri)
  val scopes: FlowField           = FlowField("scopes", OAuth2FlowModel.Scopes)

  val flows: Map[String, ParticularFlow] = Seq(
    ParticularFlow("implicit", List(authorizationUrl, scopes)),
    ParticularFlow("password", List(tokenUrl, scopes)),
    ParticularFlow("clientCredentials", List(tokenUrl, scopes)),
    ParticularFlow("authorizationCode", List(authorizationUrl, tokenUrl, scopes))
  ).map(x => (x.name, x)).toMap
}

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

    validateFlowFields(flow)

    settings.add(OAuth2SettingsModel.Flows, flow)
  }

  override def vendorSpecificSettingsProducers(): SettingsProducers = Oas3SettingsProducers

  def validateFlowFields(flow: OAuth2Flow): Unit = {
    val flowName              = flow.flow.value()
    val requiredFieldsPerFlow = Oas3SecuritySettingsParser.flows
    val requiredFlowsOption   = requiredFieldsPerFlow.get(flowName)
    if (requiredFlowsOption.isEmpty)
      ctx.eh.violation(InvalidOAuth2FlowName, flow.id, s"Flow name $flowName is not a valid OAuth2 flow")
    else {
      val missingFields =
        requiredFlowsOption.get.requiredFields.filter(flowField => flow.fields.entry(flowField.field).isEmpty)
      missingFields.foreach { flowField =>
        ctx.eh.violation(MissingOAuthFlowField, flow.id, s"Missing ${flowField.name} for $flowName flow")
      }
    }
  }
}
