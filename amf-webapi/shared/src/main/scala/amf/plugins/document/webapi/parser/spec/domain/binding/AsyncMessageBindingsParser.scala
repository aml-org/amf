package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091MessageBindingModel,
  HttpMessageBindingModel,
  KafkaMessageBindingModel,
  MqttMessageBindingModel
}
import amf.plugins.domain.webapi.models.bindings.{MessageBinding, MessageBindings}
import amf.plugins.domain.webapi.models.bindings.amqp.Amqp091MessageBinding
import amf.plugins.domain.webapi.models.bindings.http.HttpMessageBinding
import amf.plugins.domain.webapi.models.bindings.kafka.KafkaMessageBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.MqttMessageBinding
import org.yaml.model.{YMap, YMapEntry, YScalar}

object AsyncMessageBindingsParser extends AsyncBindingsParser {
  override type Binding  = MessageBinding
  override type Bindings = MessageBindings

  override def parse(entryOrMap: Either[YMapEntry, YMap], parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBindings = {
    entryOrMap match {
      case Left(entry) =>
        val map = entry.value.as[YMap]
        val bindingsObj =
          MessageBindings(map).withName(entry.key.as[YScalar].text, Annotations(entry.key)).adopted(parent)
        val bindings = parseElements(map, bindingsObj.id)
        bindingsObj.withBindings(bindings)
      case Right(map) =>
        val bindingsObj: MessageBindings  = MessageBindings(map).adopted(parent)
        val bindings: Seq[MessageBinding] = parseElements(map, bindingsObj.id)
        bindingsObj.withBindings(bindings)
    }
  }

  override protected def parseAmqp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = Amqp091MessageBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("contentEncoding", Amqp091MessageBindingModel.ContentEncoding in binding)
    map.key("messageType", Amqp091MessageBindingModel.MessageType in binding)
    parseBindingVersion(binding, Amqp091MessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "amqpMessageBinding")

    binding
  }

  override protected def parseHttp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = HttpMessageBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _, parent))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "httpMessageBinding")

    binding
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = KafkaMessageBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("key", KafkaMessageBindingModel.Key in binding)
    parseBindingVersion(binding, KafkaMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "kafkaMessageBinding")

    binding
  }

  override protected def parseMqtt(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = MqttMessageBinding(Annotations(entry)).adopted(parent)

    val map = entry.value.as[YMap]

    parseBindingVersion(binding, MqttMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "mqttMessageBinding")

    binding
  }
}
