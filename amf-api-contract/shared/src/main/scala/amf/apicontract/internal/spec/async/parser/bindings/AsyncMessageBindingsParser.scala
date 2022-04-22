package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding
import amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.{MessageBinding, MessageBindings}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorMessageBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

case class AsyncMessageBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {

  override type Binding  = MessageBinding
  override type Bindings = MessageBindings
  override protected val bindingsField: Field = MessageBindingsModel.Bindings

  override protected def createParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser =
    AsyncMessageBindingsParser(entryLike)

  override protected def createBindings(): MessageBindings = MessageBindings()

  def handleRef(fullRef: String): MessageBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messageBindings")
    ctx.declarations
      .findMessageBindings(label, SearchScope.Named)
      .map(messageBindings =>
        nameAndAdopt(messageBindings.link(AmfScalar(label), entryLike.annotations, Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): MessageBindings =
    new ErrorMessageBindings(fullRef, entryLike.asMap)

  override protected def parseAmqp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = Amqp091MessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("contentEncoding", Amqp091MessageBindingModel.ContentEncoding in binding)
    map.key("messageType", Amqp091MessageBindingModel.MessageType in binding)
    parseBindingVersion(binding, Amqp091MessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "amqpMessageBinding")

    binding
  }

  override protected def parseHttp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = HttpMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _, binding.id))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "httpMessageBinding")

    binding
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = KafkaMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("key", entry => parseSchema(KafkaMessageBindingModel.MessageKey, binding, entry, binding.id + "/key"))
    parseBindingVersion(binding, KafkaMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaMessageBinding")

    binding
  }

  override protected def parseMqtt(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBinding = {
    val binding = MqttMessageBinding(Annotations(entry))

    val map = entry.value.as[YMap]

    parseBindingVersion(binding, MqttMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttMessageBinding")

    binding
  }
}
