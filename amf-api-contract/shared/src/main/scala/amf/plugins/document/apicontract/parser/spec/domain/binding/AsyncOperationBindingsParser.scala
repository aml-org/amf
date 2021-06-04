package amf.plugins.document.apicontract.parser.spec.domain.binding

import amf.core.metamodel.Field
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorOperationBindings
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.domain.apicontract.metamodel.bindings.{
  Amqp091OperationBindingModel,
  HttpOperationBindingModel,
  KafkaOperationBindingModel,
  MqttOperationBindingModel,
  OperationBindingsModel
}
import amf.plugins.domain.apicontract.models.bindings.amqp.Amqp091OperationBinding
import amf.plugins.domain.apicontract.models.bindings.http.HttpOperationBinding
import amf.plugins.domain.apicontract.models.bindings.kafka.KafkaOperationBinding
import amf.plugins.domain.apicontract.models.bindings.mqtt.MqttOperationBinding
import amf.plugins.domain.apicontract.models.bindings.{OperationBinding, OperationBindings}
import org.yaml.model.{YMap, YMapEntry}

case class AsyncOperationBindingsParser(entryLike: YMapEntryLike, parent: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike, parent) {
  override type Binding  = OperationBinding
  override type Bindings = OperationBindings
  override val bindingsField: Field = OperationBindingsModel.Bindings

  override protected def createBindings(): OperationBindings = OperationBindings()

  protected def createParser(entryOrMap: YMapEntryLike): AsyncBindingsParser =
    AsyncOperationBindingsParser(entryOrMap, parent)

  def handleRef(fullRef: String): OperationBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "operationBindings")
    ctx.declarations
      .findOperationBindings(label, SearchScope.Named)
      .map(operationBindings =>
        nameAndAdopt(operationBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, entryLike, parent))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): OperationBindings =
    new ErrorOperationBindings(fullRef, entryLike.asMap)

  override protected def parseHttp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): OperationBinding = {
    val binding = HttpOperationBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("type", HttpOperationBindingModel.OperationType in binding)
    if (binding.operationType.is("request")) map.key("method", HttpOperationBindingModel.Method in binding)
    map.key("query", entry => parseSchema(HttpOperationBindingModel.Query, binding, entry, binding.id))
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

    map.key("groupId",
            entry => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry, binding.id + "/group-id"))
    map.key("clientId",
            entry => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry, binding.id + "/client-id"))
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
