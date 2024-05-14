package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubChannelBinding,
  GooglePubSubChannelBinding010,
  GooglePubSubChannelBinding020,
  GooglePubSubMessageStoragePolicy,
  GooglePubSubSchemaSettings
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  GooglePubSubChannelBinding010Model,
  GooglePubSubChannelBindingModel,
  GooglePubSubMessageStoragePolicyModel,
  GooglePubSubSchemaSettingsModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object GooglePubSubChannelBindingParser extends BindingParser[GooglePubSubChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): GooglePubSubChannelBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "GooglePubSubChannelBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "latest" => GooglePubSubChannelBinding020(Annotations(entry))
      case "0.1.0"            => GooglePubSubChannelBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = GooglePubSubChannelBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "GooglePubSub Channel Binding")
        defaultBinding
    }

    map.key(
      "labels",
      entry => {
        val valueDataNode = DataNodeParser(entry.value).parse()
        binding.setWithoutId(GooglePubSubChannelBindingModel.Labels, valueDataNode, Annotations(entry))
      }
    )
    map.key("messageRetentionDuration", GooglePubSubChannelBindingModel.MessageRetentionDuration in binding)
    parseMessageStoragePolicy(binding, map)
    parseSchemaSettings(binding, map)
    parseBindingVersion(binding, GooglePubSubChannelBindingModel.BindingVersion, map)

    bindingVersion match {
      case "0.2.0" | "latest" =>
        ctx.closedShape(binding, map, "GooglePubSubChannelBinding020")
      case _ =>
        map.key("topic", GooglePubSubChannelBinding010Model.Topic in binding)
        ctx.closedShape(binding, map, "GooglePubSubChannelBinding010")
    }

    binding
  }

  private def parseSchemaSettings(binding: GooglePubSubChannelBinding, map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "schemaSettings",
      { entry =>
        val schemaSettings    = GooglePubSubSchemaSettings(Annotations(entry.value))
        val schemaSettingsMap = entry.value.as[YMap]

        schemaSettingsMap.key("encoding", GooglePubSubSchemaSettingsModel.Encoding in schemaSettings)
        schemaSettingsMap.key("firstRevisionId", GooglePubSubSchemaSettingsModel.FirstRevisionId in schemaSettings)
        schemaSettingsMap.key("lastRevisionId", GooglePubSubSchemaSettingsModel.LastRevisionId in schemaSettings)
        schemaSettingsMap.key("name", GooglePubSubSchemaSettingsModel.Name in schemaSettings)

        ctx.closedShape(schemaSettings, schemaSettingsMap, "GooglePubSubSchemaSettings")
        binding.setWithoutId(GooglePubSubChannelBindingModel.SchemaSettings, schemaSettings, Annotations(entry))
      }
    )
  }

  private def parseMessageStoragePolicy(binding: GooglePubSubChannelBinding, map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "messageStoragePolicy",
      { entry =>
        val policy    = GooglePubSubMessageStoragePolicy(Annotations(entry.value))
        val policyMap = entry.value.as[YMap]
        policyMap.key(
          "allowedPersistenceRegions",
          GooglePubSubMessageStoragePolicyModel.AllowedPersistenceRegions in policy
        )

        ctx.closedShape(policy, policyMap, "GooglePubSubMessageStoragePolicy")
        binding.setWithoutId(GooglePubSubChannelBindingModel.MessageStoragePolicy, policy, Annotations(entry))
      }
    )
  }
}
