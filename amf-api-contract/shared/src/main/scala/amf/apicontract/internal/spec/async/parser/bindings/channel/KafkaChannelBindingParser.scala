package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaChannelBinding
import amf.apicontract.internal.metamodel.domain.bindings.KafkaChannelBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaChannelBindingParser extends BindingParser[KafkaChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaChannelBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaChannelBinding", ctx.specSettings.spec)

    val binding: KafkaChannelBinding = bindingVersion match {
      case "0.3.0" | "0.4.0" | "latest" => KafkaChannelBinding(Annotations(entry))
      case invalidVersion => // "0.1.0" | "0.2.0" don't parse because kafka channel binding wasn't defined until 0.3.0
        val defaultBinding = KafkaChannelBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Kafka Channel Binding")
        defaultBinding
    }

    map.key("topic", KafkaChannelBindingModel.Topic in binding)
    map.key("partitions", KafkaChannelBindingModel.Partitions in binding)
    map.key("replicas", KafkaChannelBindingModel.Replicas in binding)

    parseBindingVersion(binding, KafkaChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaChannelBinding")

    binding
  }
}
