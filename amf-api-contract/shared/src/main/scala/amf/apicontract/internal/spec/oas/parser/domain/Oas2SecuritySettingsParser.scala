package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security.{OAuth2FlowModel, OAuth2SettingsModel}
import amf.apicontract.internal.spec.common.parser.{
  Oas2SettingsProducers,
  SettingsProducers,
  WebApiContext,
  WebApiShapeParserContextAdapter
}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.annotations.VirtualElement
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, Lazy}
import amf.shapes.internal.spec.common.parser.AnnotationParser
import org.yaml.model.YMap

class Oas2SecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: WebApiContext)
    extends OasLikeSecuritySettingsParser(map, scheme) {

  override def parse(): Option[Settings] =
    produceSettings
      .map { settings =>
        val parsedSettings = settings match {
          case s: OAuth1Settings => parseOauth1Settings(s)
          case s: OAuth2Settings => parseOauth2Settings(s)
          case s: ApiKeySettings => parseApiKeySettings(s)
          case defaultSettings =>
            map
              .key("settings".asOasExtension)
              .map(entry => parseDynamicSettings(entry.value.as[YMap], defaultSettings))
              .getOrElse(defaultSettings)
        }
        parsedSettings.annotations += VirtualElement()
        parseAnnotations(parsedSettings)
      }

  override def parseOauth2Settings(settings: OAuth2Settings): OAuth2Settings = {
    val flow = new Lazy[OAuth2Flow](() => OAuth2Flow(map).adopted(settings.id))

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

        parseDynamicSettings(xSettings, settings, "authorizationGrants")
      }
    )

    AnnotationParser(settings, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("scopes")

    flow.option.foreach { f =>
      f.adopted(settings.id)
      settings.set(OAuth2SettingsModel.Flows, AmfArray(Seq(f), Annotations.virtual()), Annotations.inferred())
    }

    settings
  }
  override def vendorSpecificSettingsProducers(): SettingsProducers = Oas2SettingsProducers
}
