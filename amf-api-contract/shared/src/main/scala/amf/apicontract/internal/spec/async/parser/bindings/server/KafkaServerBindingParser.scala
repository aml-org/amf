package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaServerBinding
import amf.apicontract.internal.metamodel.domain.bindings.KafkaServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaServerBindingParser extends BindingParser[KafkaServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaServerBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaServerBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: KafkaServerBinding = bindingVersion match {
      case "0.1.0" | "0.2.0" | "0.3.0" | "latest" => KafkaServerBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = KafkaServerBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Kafka Binding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    map.key("schemaRegistryUrl", KafkaServerBindingModel.SchemaRegistryUrl in binding)
    map.key("schemaRegistryVendor", KafkaServerBindingModel.SchemaRegistryVendor in binding)

    parseBindingVersion(binding, KafkaServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaServerBinding")

    binding
  }
}
