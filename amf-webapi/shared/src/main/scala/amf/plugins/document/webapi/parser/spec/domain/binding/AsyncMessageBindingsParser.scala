package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorMessageBindings
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
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.plugins.document.webapi.parser.spec.domain.ConversionHelpers._

object AsyncMessageBindingsParser extends AsyncBindingsParser {
  override type Binding  = MessageBinding
  override type Bindings = MessageBindings

  def buildAndPopulate(entryOrMap: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBindings = {
    val map: YMap       = entryOrMap
    val messageBindings = MessageBindings(map)
    nameAndAdopt(messageBindings, entryOrMap.left.toOption, parent)
    parseBindings(messageBindings, map)
  }

  private def parseBindings(obj: MessageBindings, map: YMap)(implicit ctx: AsyncWebApiContext): MessageBindings = {
    val bindings: Seq[MessageBinding] = parseElements(map, obj.id)
    obj.withBindings(bindings)
  }

  def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String, parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messageBindings")
    ctx.declarations
      .findMessageBindings(label, SearchScope.Named)
      .map(messageBindings => nameAndAdopt(messageBindings.link(label), entryOrNode.left.toOption, parent))
      .getOrElse(remote(fullRef, entryOrNode, parent))
  }

  private def remote(fullRef: String, entryOrNode: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): MessageBindings = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(bindingsNode) =>
        val external = AsyncMessageBindingsParser.parse(Right(bindingsNode), parent)
        nameAndAdopt(external.link(fullRef), entryOrNode.left.toOption, parent)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", entryOrNode)
        nameAndAdopt(new ErrorMessageBindings(fullRef, entryOrNode).link(fullRef), entryOrNode.left.toOption, parent)
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
