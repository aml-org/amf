package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.KafkaMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaMessageBindingParser extends BindingParser[KafkaMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaMessageBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaMessageBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: KafkaMessageBinding = bindingVersion match {
      case "0.1.0" | "latest" => KafkaMessageBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = KafkaMessageBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "KafkaMessageBinding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    map.key("key", entry => parseSchema(KafkaMessageBindingModel.MessageKey, binding, entry))

    parseBindingVersion(binding, KafkaMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaMessageBinding")

    binding
  }
}
