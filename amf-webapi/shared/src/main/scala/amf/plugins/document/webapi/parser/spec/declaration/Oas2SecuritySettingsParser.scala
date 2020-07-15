package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.VirtualObject
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.core.utils.{AmfStrings, Lazy}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.webapi.metamodel.security.{OAuth2FlowModel, OAuth2SettingsModel}
import amf.plugins.domain.webapi.models.security._
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
        parsedSettings.annotations += VirtualObject()
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

    // shouldn't be just parseScopes(flow.getOrCreate, map)?
    map.key("scopes").foreach(_ => parseScopes(flow.getOrCreate, map))

    map.key(
      "settings".asOasExtension,
      entry => {
        val xSettings = entry.value.as[YMap]

        xSettings.key("authorizationGrants", OAuth2SettingsModel.AuthorizationGrants in settings)

        parseDynamicSettings(xSettings, settings, "authorizationGrants")
      }
    )

    AnnotationParser(settings, map).parseOrphanNode("scopes")

    flow.option.foreach { f =>
      f.adopted(settings.id)
      settings.add(OAuth2SettingsModel.Flows, f)
    }

    settings
  }
  override def vendorSpecificSettingsProducers(): SettingsProducers = Oas2SettingsProducers
}
