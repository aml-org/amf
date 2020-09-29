package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.metamodel.Field
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorMessageBindings
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091MessageBindingModel,
  HttpMessageBindingModel,
  KafkaMessageBindingModel,
  MessageBindingsModel,
  MqttMessageBindingModel
}
import amf.plugins.domain.webapi.models.bindings.amqp.Amqp091MessageBinding
import amf.plugins.domain.webapi.models.bindings.http.HttpMessageBinding
import amf.plugins.domain.webapi.models.bindings.kafka.KafkaMessageBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.MqttMessageBinding
import amf.plugins.domain.webapi.models.bindings.{MessageBinding, MessageBindings}
import org.yaml.model.{YMap, YMapEntry}

case class AsyncMessageBindingsParser(entryLike: YMapEntryLike, parent: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike, parent) {

  override type Binding  = MessageBinding
  override type Bindings = MessageBindings
  override protected val bindingsField: Field = MessageBindingsModel.Bindings

  override protected def createParser(entryLike: YMapEntryLike): AsyncBindingsParser =
    AsyncMessageBindingsParser(entryLike, parent)

  override protected def createBindings(): MessageBindings = MessageBindings()

  def handleRef(fullRef: String): MessageBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messageBindings")
    ctx.declarations
      .findMessageBindings(label, SearchScope.Named)
      .map(messageBindings => nameAndAdopt(messageBindings.link(label), entryLike.key))
      .getOrElse(remote(fullRef, entryLike, parent))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): MessageBindings =
    new ErrorMessageBindings(fullRef, entryLike.asMap)

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

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _, binding.id))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "httpMessageBinding")

    binding
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = KafkaMessageBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("key", entry => parseSchema(KafkaMessageBindingModel.MessageKey, binding, entry, binding.id + "/key"))
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
