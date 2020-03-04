package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.metamodel.bindings.{MqttServerBindingModel, MqttServerLastWillModel}
import amf.plugins.domain.webapi.models.bindings.ServerBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import org.yaml.model.{YMap, YMapEntry, YNode}

object AsyncServerBindingsParser extends AsyncBindingsParser {
  override type T = ServerBinding

  override protected def parseMqtt(entry: YMapEntry, parent: String, key: Option[YNode])(
      implicit ctx: AsyncWebApiContext): ServerBinding = {
    val binding = MqttServerBinding(Annotations(entry))
    nameAndAdopt(binding, parent, key)
    val map = entry.value.as[YMap]

    map.key("clientId", MqttServerBindingModel.ClientId in binding)
    map.key("cleanSession", MqttServerBindingModel.CleanSession in binding)
    map.key("keepAlive", MqttServerBindingModel.KeepAlive in binding)

    parseLastWill(binding, map)

    parseBindingVersion(binding, MqttServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "mqttServerBinding")

    binding
  }

  private def parseLastWill(binding: MqttServerBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "lastWill", { entry =>
        val lastWill    = MqttServerLastWill(Annotations(entry)).adopted(binding.id)
        val lastWillMap = entry.value.as[YMap]

        lastWillMap.key("topic", MqttServerLastWillModel.Topic in lastWill)
        lastWillMap.key("qos", MqttServerLastWillModel.Qos in lastWill)
        lastWillMap.key("retain", MqttServerLastWillModel.Retain in lastWill)

        ctx.closedShape(lastWill.id, lastWillMap, "mqttServerLastWill")

        binding.set(MqttServerBindingModel.LastWill, lastWill)
      }
    )
  }
}
