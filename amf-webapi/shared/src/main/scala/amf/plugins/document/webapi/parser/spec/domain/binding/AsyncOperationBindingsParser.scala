package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorOperationBindings
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
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.plugins.document.webapi.parser.spec.domain.ConversionHelpers._

object AsyncOperationBindingsParser extends AsyncBindingsParser {
  override type Binding  = OperationBinding
  override type Bindings = OperationBindings

  def buildAndPopulate(entryOrMap: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBindings = {
    val map: YMap         = entryOrMap
    val operationBindings = OperationBindings(map)
    nameAndAdopt(operationBindings, entryOrMap.left.toOption, parent)
    parseBindings(operationBindings, map)
  }

  private def parseBindings(obj: OperationBindings, map: YMap)(implicit ctx: AsyncWebApiContext): OperationBindings = {
    val bindings: Seq[OperationBinding] = parseElements(map, obj.id)
    obj.withBindings(bindings)
  }

  def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "operationBindings")
    ctx.declarations
      .findOperationBindings(label, SearchScope.Named)
      .map(operationBindings => nameAndAdopt(operationBindings.link(label), entryOrNode.left.toOption, parent))
      .getOrElse(remote(fullRef, entryOrNode, parent))
  }

  private def remote(fullRef: String, entryOrNode: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBindings = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(bindingsNode) =>
        val external = AsyncOperationBindingsParser.parse(Right(bindingsNode), parent)
        nameAndAdopt(external.link(fullRef), entryOrNode.left.toOption, parent)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", entryOrNode)
        nameAndAdopt(new ErrorOperationBindings(fullRef, entryOrNode).link(fullRef), entryOrNode.left.toOption, parent)
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
