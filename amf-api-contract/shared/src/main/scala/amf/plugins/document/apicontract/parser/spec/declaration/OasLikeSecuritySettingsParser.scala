package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.spec.contexts.WebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, DataNodeParser, SpecParserOps}
import amf.plugins.domain.apicontract.metamodel.security._
import amf.plugins.domain.apicontract.models.security._
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

abstract class OasLikeSecuritySettingsParser(map: YMap, scheme: SecurityScheme)(implicit ctx: WebApiContext)
    extends SpecParserOps {

  protected def parseAnnotations(settings: Settings): Settings = {
    AnnotationParser(settings, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    settings.add(Annotations(map))
  }

  def vendorSpecificSettingsProducers(): SettingsProducers

  def produceSettings: Option[Settings] = {
    val settingsProducers = vendorSpecificSettingsProducers().`for`(scheme)

    val settingsType = scheme.`type`.value()

    settingsProducers get settingsType match {
      case Some(producerOption) => producerOption.map(producer => producer())
      case None                 => Some(scheme.withDefaultSettings())
    }
  }

  def parseDynamicSettings(xSettings: YMap, settings: Settings, properties: String*): Settings = {
    val entries = xSettings.entries.filterNot { entry =>
      val key: String = entry.key.as[YScalar].text
      properties.contains(key) || isOasAnnotation(key)
    }

    if (entries.nonEmpty) {
      val node = DataNodeParser(YNode(YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))),
                                parent = Some(settings.id))(WebApiShapeParserContextAdapter(ctx)).parse()
      settings.set(SettingsModel.AdditionalProperties, node)
    }

    AnnotationParser(scheme, xSettings)(WebApiShapeParserContextAdapter(ctx)).parse()

    settings
  }

  def parseApiKeySettings(settings: ApiKeySettings): ApiKeySettings = {

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
      entry => parseDynamicSettings(entry.value.as[YMap], settings, "name", "in")
    )

    settings
  }

  def parseOauth1Settings(settings: OAuth1Settings): OAuth1Settings = {

    map.key(
      "settings".asOasExtension,
      entry => {
        val map = entry.value.as[YMap]

        map.key("requestTokenUri", OAuth1SettingsModel.RequestTokenUri in settings)
        map.key("authorizationUri", OAuth1SettingsModel.AuthorizationUri in settings)
        map.key("tokenCredentialsUri", OAuth1SettingsModel.TokenCredentialsUri in settings)
        map.key("signatures", OAuth1SettingsModel.Signatures in settings)

        parseDynamicSettings(map, settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
      }
    )

    settings
  }

  def parseOauth2Settings(settings: OAuth2Settings): OAuth2Settings

  def parseScopes(flow: OAuth2Flow, map: YMap): Unit = map.key("scopes").foreach { entry =>
    val scopeMap = entry.value.as[YMap]
    val scopes   = scopeMap.entries.filterNot(entry => isOasAnnotation(entry.key)).map(parseScope(_, flow.id))
    flow.set(OAuth2FlowModel.Scopes, AmfArray(scopes, Annotations(entry.value)), Annotations(entry))
  }

  def parseScope(scopeEntry: YMapEntry, parentId: String): Scope = {
    val name: String        = scopeEntry.key.as[YScalar].text
    val description: String = scopeEntry.value

    Scope(scopeEntry)
      .set(ScopeModel.Name, AmfScalar(name, Annotations(scopeEntry.key)), Annotations.inferred())
      .set(ScopeModel.Description, AmfScalar(description, Annotations(scopeEntry.key)), Annotations.inferred())
      .adopted(parentId)
  }

  def parse(): Option[Settings]
}
