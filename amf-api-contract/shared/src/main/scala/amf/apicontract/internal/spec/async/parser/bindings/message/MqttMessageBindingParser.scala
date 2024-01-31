package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.MqttMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object MqttMessageBindingParser extends BindingParser[MqttMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): MqttMessageBinding = {
    val binding = MqttMessageBinding(Annotations(entry))

    val map = entry.value.as[YMap]

    parseBindingVersion(binding, MqttMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttMessageBinding")

    binding
  }
}
