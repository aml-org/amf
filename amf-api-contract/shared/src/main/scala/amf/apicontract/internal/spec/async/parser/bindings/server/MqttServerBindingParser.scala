package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttServerBinding,
  MqttServerBinding010,
  MqttServerBinding020,
  MqttServerLastWill
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  MqttServerBinding020Model,
  MqttServerBindingModel,
  MqttServerLastWillModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object MqttServerBindingParser extends BindingParser[MqttServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): MqttServerBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "MqttServerBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "latest" => MqttServerBinding020(Annotations(entry))
      case "0.1.0"            => MqttServerBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = MqttServerBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Mqtt Server Binding")
        defaultBinding
    }

    map.key("clientId", MqttServerBindingModel.ClientId in binding)
    map.key("cleanSession", MqttServerBindingModel.CleanSession in binding)
    map.key("keepAlive", MqttServerBindingModel.KeepAlive in binding)

    parseLastWill(binding, map)

    parseBindingVersion(binding, MqttServerBindingModel.BindingVersion, map)

    bindingVersion match {
      case "0.2.0" | "latest" =>
        map.key("sessionExpiryInterval").foreach { entry =>
          parseScalarOrRefOrSchema(
            binding,
            entry,
            MqttServerBinding020Model.SessionExpiryInterval,
            MqttServerBinding020Model.SessionExpiryIntervalSchema
          )
        }
        map.key("maximumPacketSize").foreach { entry =>
          parseScalarOrRefOrSchema(
            binding,
            entry,
            MqttServerBinding020Model.MaximumPacketSize,
            MqttServerBinding020Model.MaximumPacketSizeSchema
          )
        }
        ctx.closedShape(binding, map, "mqttServerBinding020")
      case _ =>
        ctx.closedShape(binding, map, "mqttServerBinding010")
    }

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
