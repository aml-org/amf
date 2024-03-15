package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.pulsar.{PulsarChannelBinding, PulsarChannelRetention}
import amf.apicontract.internal.metamodel.domain.bindings.{PulsarChannelBindingModel, PulsarChannelRetentionModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object PulsarChannelBindingParser extends BindingParser[PulsarChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): PulsarChannelBinding = {
    val binding = PulsarChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("namespace") match {
      case Some(value) => Some(value).foreach(PulsarChannelBindingModel.Namespace in binding)
      case None        => missingRequiredFieldViolation(ctx, binding, "namespace", "Pulsar Channel Binding")
    }

    map.key("persistence") match {
      case Some(value) => Some(value).foreach(PulsarChannelBindingModel.Persistence in binding)
      case None        => missingRequiredFieldViolation(ctx, binding, "persistence", "Pulsar Channel Binding")
    }

    map.key("compaction", PulsarChannelBindingModel.Compaction in binding)
    map.key("geo-replication", PulsarChannelBindingModel.GeoReplication in binding)
    parseRetention(binding, map)
    map.key("ttl", PulsarChannelBindingModel.Ttl in binding)
    map.key("deduplication", PulsarChannelBindingModel.Deduplication in binding)

    parseBindingVersion(binding, PulsarChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "PulsarChannelBinding")

    binding
  }

  private def parseRetention(binding: PulsarChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "retention",
      { entry =>
        val retention    = PulsarChannelRetention(Annotations(entry.value))
        val retentionMap = entry.value.as[YMap]

        retentionMap.key("time") match {
          case Some(value) => Some(value).foreach(PulsarChannelRetentionModel.Time in retention)
          case None        => setDefaultValue(binding, PulsarChannelRetentionModel.Time, AmfScalar(0))
        }

        retentionMap.key("size") match {
          case Some(value) => Some(value).foreach(PulsarChannelRetentionModel.Size in retention)
          case None        => setDefaultValue(binding, PulsarChannelRetentionModel.Size, AmfScalar(0))
        }

        ctx.closedShape(retention, retentionMap, "PulsarChannelRetention")

        binding.setWithoutId(PulsarChannelBindingModel.Retention, retention, Annotations(entry))
      }
    )
  }
}
