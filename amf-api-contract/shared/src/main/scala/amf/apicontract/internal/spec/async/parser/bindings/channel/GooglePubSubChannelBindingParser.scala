package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{GooglePubSubChannelBinding, GooglePubSubMessageStoragePolicy, GooglePubSubSchemaSettings}
import amf.apicontract.internal.metamodel.domain.bindings.{GooglePubSubChannelBindingModel, GooglePubSubMessageStoragePolicyModel, GooglePubSubSchemaSettingsModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object GooglePubSubChannelBindingParser extends BindingParser[GooglePubSubChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): GooglePubSubChannelBinding = {
    val binding = GooglePubSubChannelBinding(Annotations(entry))
    val map = entry.value.as[YMap]

    map.key("labels", GooglePubSubChannelBindingModel.Labels in binding)
    map.key("messageRetentionDuration", GooglePubSubChannelBindingModel.MessageRetentionDuration in binding)
    map.key("topic", GooglePubSubChannelBindingModel.Topic in binding)

    parseMessageStoragePolicy(binding, map)
    parseSchemaSettings(binding, map)
    parseBindingVersion(binding, GooglePubSubChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "GooglePubSubChannelBinding")
    binding

  }
  private def parseSchemaSettings(binding: GooglePubSubChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit ={
    map.key("schemaSettings",{ entry =>
      val schemaSettings = GooglePubSubSchemaSettings(Annotations(entry.value))
      val schemaSettingsMap = entry.value.as[YMap]

      schemaSettingsMap.key("encoding",GooglePubSubSchemaSettingsModel.Encoding in schemaSettings)
      schemaSettingsMap.key("firstRevisionId",GooglePubSubSchemaSettingsModel.FirstRevisionId in schemaSettings)
      schemaSettingsMap.key("lastRevisionId",GooglePubSubSchemaSettingsModel.LastRevisionId in schemaSettings)
      schemaSettingsMap.key("name",GooglePubSubSchemaSettingsModel.Name in schemaSettings)

      ctx.closedShape(schemaSettings, schemaSettingsMap, "GooglePubSubChannelMessageStoragePolicy")
      binding.setWithoutId(GooglePubSubChannelBindingModel.SchemaSettings, schemaSettings, Annotations(entry))
    }
   )
  }

  private def parseMessageStoragePolicy(binding: GooglePubSubChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit ={
    map.key("messageStoragePolicy", { entry =>
      val policy = GooglePubSubMessageStoragePolicy(Annotations(entry.value))
      val policyMap = entry.value.as[YMap]
      policyMap.key("allowedPersistenceRegions", GooglePubSubMessageStoragePolicyModel.AllowedPersistenceRegions in policy)

      ctx.closedShape(policy, policyMap, "GooglePubSubChannelMessageStoragePolicy")
      binding.setWithoutId(GooglePubSubChannelBindingModel.MessageStoragePolicy, policy, Annotations(entry))
     }
    )
  }

}
