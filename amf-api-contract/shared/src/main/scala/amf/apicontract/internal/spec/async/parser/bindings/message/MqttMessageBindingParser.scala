package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttMessageBinding,
  MqttMessageBinding010,
  MqttMessageBinding020
}
import amf.apicontract.internal.metamodel.domain.bindings.{MqttMessageBinding020Model, MqttMessageBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object MqttMessageBindingParser extends BindingParser[MqttMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): MqttMessageBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "MqttMessageBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "latest" => MqttMessageBinding020(Annotations(entry))
      case "0.1.0"            => MqttMessageBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = MqttMessageBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Mqtt Server Binding")
        defaultBinding
    }

    bindingVersion match {
      case "0.2.0" | "latest" =>
        map.key("payloadFormatIndicator", MqttMessageBinding020Model.PayloadFormatIndicator in binding)
        map.key("correlationData").foreach { entry =>
          ctx.link(entry.value) match {
            case Left(fullRef) =>
              handleRef(fullRef, "schemas", entry, MqttMessageBinding020Model.CorrelationData, binding)
            case Right(_) => parseSchema(MqttMessageBinding020Model.CorrelationData, binding, entry)
          }
        }
        map.key("contentType", MqttMessageBinding020Model.ContentType in binding)
        map.key("responseTopic", MqttMessageBinding020Model.ResponseTopic in binding)
        ctx.closedShape(binding, map, "mqttMessageBinding020")
      case _ =>
        ctx.closedShape(binding, map, "mqttMessageBinding010")
    }

    parseBindingVersion(binding, MqttMessageBindingModel.BindingVersion, map)

    binding
  }
}
