package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091OperationBindingModel,
  HttpOperationBindingModel,
  KafkaOperationBindingModel,
  MqttOperationBindingModel
}
import amf.plugins.domain.webapi.models.bindings.{OperationBinding, OperationBindings}
import amf.plugins.domain.webapi.models.bindings.amqp.Amqp091OperationBinding
import amf.plugins.domain.webapi.models.bindings.http.HttpOperationBinding
import amf.plugins.domain.webapi.models.bindings.kafka.KafkaOperationBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.MqttOperationBinding
import org.yaml.model.{YMap, YMapEntry, YScalar}

object AsyncOperationBindingsParser extends AsyncBindingsParser {
  override type Binding  = OperationBinding
  override type Bindings = OperationBindings

  override def parse(entryOrMap: Either[YMapEntry, YMap], parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBindings = {
    entryOrMap match {
      case Left(entry) =>
        val map = entry.value.as[YMap]
        val bindingsObj =
          OperationBindings(map).withName(entry.key.as[YScalar].text, Annotations(entry.key)).adopted(parent)
        val bindings = parseElements(map, bindingsObj.id)
        bindingsObj.withBindings(bindings)
      case Right(map) =>
        val bindingsObj: OperationBindings  = OperationBindings(map).adopted(parent)
        val bindings: Seq[OperationBinding] = parseElements(map, bindingsObj.id)
        bindingsObj.withBindings(bindings)
    }
  }
  override protected def parseHttp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBinding = {
    val binding = HttpOperationBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("type", HttpOperationBindingModel.Type in binding)
    if (binding.`type`.is("request")) map.key("method", HttpOperationBindingModel.Method in binding)
    map.key("query", entry => parseSchema(HttpOperationBindingModel.Query, binding, entry, parent)) // TODO validate as object
    parseBindingVersion(binding, HttpOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "httpOperationBinding")

    binding
  }

  override protected def parseAmqp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBinding = {
    val binding = Amqp091OperationBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("expiration", Amqp091OperationBindingModel.Expiration in binding)
    map.key("userId", Amqp091OperationBindingModel.UserId in binding)
    map.key("cc", Amqp091OperationBindingModel.CC in binding)
    map.key("priority", Amqp091OperationBindingModel.Priority in binding)
    map.key("deliveryMode", Amqp091OperationBindingModel.DeliveryMode in binding)
    map.key("mandatory", Amqp091OperationBindingModel.Mandatory in binding)
    map.key("bcc", Amqp091OperationBindingModel.BCC in binding)
    map.key("replyTo", Amqp091OperationBindingModel.ReplyTo in binding)
    map.key("timestamp", Amqp091OperationBindingModel.Timestamp in binding)
    map.key("ack", Amqp091OperationBindingModel.Ack in binding)

    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "amqpOperationBinding")

    binding
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBinding = {
    val binding = KafkaOperationBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("groupId", KafkaOperationBindingModel.GroupId in binding)
    map.key("clientId", KafkaOperationBindingModel.ClientId in binding)
    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "kafkaOperationBinding")

    binding
  }

  override protected def parseMqtt(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBinding = {
    val binding = MqttOperationBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("qos", MqttOperationBindingModel.Qos in binding)
    map.key("retain", MqttOperationBindingModel.Retain in binding)
    parseBindingVersion(binding, MqttOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "mqttOperationBinding")

    binding
  }
}
