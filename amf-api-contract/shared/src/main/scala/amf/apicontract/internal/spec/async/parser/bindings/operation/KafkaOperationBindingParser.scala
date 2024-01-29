package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding
import amf.apicontract.internal.metamodel.domain.bindings.{KafkaOperationBindingModel, MqttOperationBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaOperationBindingParser extends BindingParser[KafkaOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaOperationBinding = {
    val binding = KafkaOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key(
      "groupId",
      entry => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry)
    )
    map.key(
      "clientId",
      entry => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry)
    )
    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaOperationBinding")

    binding
  }
}
