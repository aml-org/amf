package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding
import amf.apicontract.internal.metamodel.domain.bindings.KafkaOperationBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaOperationBindingParser extends BindingParser[KafkaOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaOperationBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaOperationBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: KafkaOperationBinding = bindingVersion match {
      case "0.1.0" | "latest" => KafkaOperationBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = KafkaOperationBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "KafkaOperationBinding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    map.key("groupId", entry => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry))
    map.key("clientId", entry => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry))

    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaOperationBinding")

    binding
  }
}
