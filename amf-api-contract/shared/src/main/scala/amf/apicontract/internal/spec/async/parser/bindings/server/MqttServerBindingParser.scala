package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.apicontract.internal.metamodel.domain.bindings.{MqttServerBindingModel, MqttServerLastWillModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object MqttServerBindingParser extends BindingParser[MqttServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): MqttServerBinding = {
    val binding = MqttServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("clientId", MqttServerBindingModel.ClientId in binding)
    map.key("cleanSession", MqttServerBindingModel.CleanSession in binding)
    map.key("keepAlive", MqttServerBindingModel.KeepAlive in binding)

    parseLastWill(binding, map)

    parseBindingVersion(binding, MqttServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttServerBinding")

    binding
  }

  private def parseLastWill(binding: MqttServerBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "lastWill",
      { entry =>
        val lastWill    = MqttServerLastWill(Annotations(entry.value))
        val lastWillMap = entry.value.as[YMap]

        lastWillMap.key("topic", MqttServerLastWillModel.Topic in lastWill)
        lastWillMap.key("qos", MqttServerLastWillModel.Qos in lastWill)
        lastWillMap.key("retain", MqttServerLastWillModel.Retain in lastWill)
        lastWillMap.key("message", MqttServerLastWillModel.Message in lastWill)

        ctx.closedShape(lastWill, lastWillMap, "mqttServerLastWill")
        binding.setWithoutId(MqttServerBindingModel.LastWill, lastWill, Annotations(entry))
      }
    )
  }
}
