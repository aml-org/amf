package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttMessageBinding, MqttOperationBinding}
import amf.apicontract.internal.metamodel.domain.bindings.{MqttMessageBindingModel, MqttOperationBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object MqttOperationBindingParser extends BindingParser[MqttOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): MqttOperationBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "MqttOperationBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "latest" => MqttOperationBinding(Annotations(entry))
      case "0.1.0"            => MqttOperationBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = MqttOperationBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Mqtt Server Binding")
        defaultBinding
    }

    map.key("qos", MqttOperationBindingModel.Qos in binding)
    map.key("retain", MqttOperationBindingModel.Retain in binding)
    parseBindingVersion(binding, MqttOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttOperationBinding")

    binding
  }
}
