package amf.plugins.document.apicontract.parser.spec.domain.binding

import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorChannelBindings
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.domain.apicontract.metamodel.bindings._
import amf.plugins.domain.apicontract.models.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091Queue
}
import amf.plugins.domain.apicontract.models.bindings.websockets.WebSocketsChannelBinding
import amf.plugins.domain.apicontract.models.bindings.{ChannelBinding, ChannelBindings}
import org.yaml.model.{YMap, YMapEntry}

case class AsyncChannelBindingsParser(entryLike: YMapEntryLike, parent: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike, parent) {

  override type Binding            = ChannelBinding
  override protected type Bindings = ChannelBindings
  override protected val bindingsField: Field = ChannelBindingsModel.Bindings

  override protected def createBindings(): ChannelBindings = ChannelBindings()

  override protected def createParser(entryLike: YMapEntryLike): AsyncBindingsParser =
    AsyncChannelBindingsParser(entryLike, parent)

  def handleRef(fullRef: String): ChannelBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "channelBindings")
    ctx.declarations
      .findChannelBindings(label, SearchScope.Named)
      .map(channelBindings =>
        nameAndAdopt(channelBindings.link(AmfScalar(label), entryLike.annotations, Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, entryLike, parent))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): ChannelBindings =
    new ErrorChannelBindings(fullRef, entryLike.asMap)

  override protected def parseAmqp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): ChannelBinding = {
    val binding = Amqp091ChannelBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("is", Amqp091ChannelBindingModel.Is in binding)

    // Default channel type is 'routingKey'.
    if (binding.is.isNullOrEmpty) {
      binding.set(Amqp091ChannelBindingModel.Is, AmfScalar("routingKey"), Annotations.synthesized())
    }
    parseQueue(binding, map)
    parseExchange(binding, map)

    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)
    ctx.closedShape(binding.id, map, "amqpChannelBinding")
    binding
  }

  private def parseExchange(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "exchange", { entry =>
        val exchange    = Amqp091ChannelExchange(Annotations(entry.value)).adopted(binding.id)
        val exchangeMap = entry.value.as[YMap]

        exchangeMap.key("name", Amqp091ChannelExchangeModel.Name in exchange) // TODO validate maxlength 255
        exchangeMap.key("type", Amqp091ChannelExchangeModel.Type in exchange)
        exchangeMap.key("durable", Amqp091ChannelExchangeModel.Durable in exchange)
        exchangeMap.key("autoDelete", Amqp091ChannelExchangeModel.AutoDelete in exchange)

        parseVHost(exchange, Amqp091ChannelExchangeModel.VHost, exchangeMap)

        ctx.closedShape(exchange.id, exchangeMap, "amqpExchangeChannelBinding")

        binding.set(Amqp091ChannelBindingModel.Exchange, exchange, Annotations(entry))
      }
    )
  }

  private def parseQueue(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "queue", { entry =>
        val queue    = Amqp091Queue(Annotations(entry.value)).adopted(binding.id)
        val queueMap = entry.value.as[YMap]

        queueMap.key("name", Amqp091QueueModel.Name in queue) // TODO validate maxlength 255
        queueMap.key("durable", Amqp091QueueModel.Durable in queue)
        queueMap.key("exclusive", Amqp091QueueModel.Exclusive in queue)
        queueMap.key("autoDelete", Amqp091QueueModel.AutoDelete in queue)

        parseVHost(queue, Amqp091QueueModel.VHost, queueMap)

        ctx.closedShape(queue.id, queueMap, "amqpQueueChannelBinding")

        binding.set(Amqp091ChannelBindingModel.Queue, queue, Annotations(entry))
      }
    )
  }

  private def parseVHost(element: DomainElement, field: Field, map: YMap)(implicit ctx: AsyncWebApiContext) = {
    map.key("vhost", field in element)

    // Default vhost is '/'.
    if (!element.fields.exists(field)) {
      element.set(field, AmfScalar("/"), Annotations.synthesized())
    }
  }

  override protected def parseWs(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): ChannelBinding = {
    val binding = WebSocketsChannelBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("method", WebSocketsChannelBindingModel.Method in binding)
    map.key("query", entry => parseSchema(WebSocketsChannelBindingModel.Query, binding, entry, binding.id + "/query"))
    map.key("headers",
            entry => parseSchema(WebSocketsChannelBindingModel.Headers, binding, entry, binding.id + "/headers"))
    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "wsChannelBinding")

    binding
  }
}
