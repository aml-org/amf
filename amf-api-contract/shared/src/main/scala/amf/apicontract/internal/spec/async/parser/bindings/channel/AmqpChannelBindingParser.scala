package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091Queue
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchangeModel,
  Amqp091QueueModel,
  WebSocketsChannelBindingModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object Amqp091ChannelBindingParser extends BindingParser[Amqp091ChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Amqp091ChannelBinding = {
    val binding = Amqp091ChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("is", Amqp091ChannelBindingModel.Is in binding)

    // Default channel type is 'routingKey'.
    if (binding.is.isNullOrEmpty) {
      binding.setWithoutId(Amqp091ChannelBindingModel.Is, AmfScalar("routingKey"), Annotations.synthesized())
    }
    parseQueue(binding, map)
    parseExchange(binding, map)

    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)
    ctx.closedShape(binding, map, "amqpChannelBinding")
    binding
  }

  private def parseExchange(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "exchange",
      { entry =>
        val exchange    = Amqp091ChannelExchange(Annotations(entry.value))
        val exchangeMap = entry.value.as[YMap]

        exchangeMap.key("name", Amqp091ChannelExchangeModel.Name in exchange) // TODO validate maxlength 255
        exchangeMap.key("type", Amqp091ChannelExchangeModel.Type in exchange)
        exchangeMap.key("durable", Amqp091ChannelExchangeModel.Durable in exchange)
        exchangeMap.key("autoDelete", Amqp091ChannelExchangeModel.AutoDelete in exchange)

        parseVHost(exchange, Amqp091ChannelExchangeModel.VHost, exchangeMap)

        ctx.closedShape(exchange, exchangeMap, "amqpExchangeChannelBinding")

        binding.setWithoutId(Amqp091ChannelBindingModel.Exchange, exchange, Annotations(entry))
      }
    )
  }

  private def parseQueue(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "queue",
      { entry =>
        val queue    = Amqp091Queue(Annotations(entry.value))
        val queueMap = entry.value.as[YMap]

        queueMap.key("name", Amqp091QueueModel.Name in queue) // TODO validate maxlength 255
        queueMap.key("durable", Amqp091QueueModel.Durable in queue)
        queueMap.key("exclusive", Amqp091QueueModel.Exclusive in queue)
        queueMap.key("autoDelete", Amqp091QueueModel.AutoDelete in queue)

        parseVHost(queue, Amqp091QueueModel.VHost, queueMap)

        ctx.closedShape(queue, queueMap, "amqpQueueChannelBinding")

        binding.setWithoutId(Amqp091ChannelBindingModel.Queue, queue, Annotations(entry))
      }
    )
  }

  private def parseVHost(element: DomainElement, field: Field, map: YMap)(implicit ctx: AsyncWebApiContext) = {
    map.key("vhost", field in element)

    // Default vhost is '/'.
    if (!element.fields.exists(field)) {
      element.setWithoutId(field, AmfScalar("/"), Annotations.synthesized())
    }
  }
}
