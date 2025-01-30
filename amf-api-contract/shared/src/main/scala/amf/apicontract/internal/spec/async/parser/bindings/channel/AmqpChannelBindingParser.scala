package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelBinding010,
  Amqp091ChannelBinding020,
  Amqp091ChannelExchange010,
  Amqp091ChannelExchange020,
  Amqp091Queue010,
  Amqp091Queue020
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091ChannelBinding010Model,
  Amqp091ChannelBinding020Model,
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchange020Model,
  Amqp091ChannelExchangeModel,
  Amqp091Queue020Model,
  Amqp091QueueModel,
  WebSocketsChannelBindingModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object AmqpChannelBindingParser extends BindingParser[Amqp091ChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Amqp091ChannelBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "Amqp091ChannelBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: Amqp091ChannelBinding = bindingVersion match {
      case "0.3.0" | "latest" => // 0.3.0 only changes operation binding, channel binding is the same as 0.2.0
        Amqp091ChannelBinding020(Annotations(entry))
      case "0.2.0" => Amqp091ChannelBinding020(Annotations(entry))
      case "0.1.0" => Amqp091ChannelBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = Amqp091ChannelBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Amqp091ChannelBinding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    map.key("is") match {
      case Some(value) => Some(value).foreach(Amqp091ChannelBindingModel.Is in binding)
      case None        => setDefaultValue(binding, Amqp091ChannelBindingModel.Is, AmfScalar("routingKey"))
    }

    parseQueue(binding, map, bindingVersion)
    parseExchange(binding, map, bindingVersion)

    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)
    ctx.closedShape(binding, map, "amqpChannelBinding")
    binding
  }

  private def parseExchange(binding: Amqp091ChannelBinding, map: YMap, bindingVersion: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "exchange",
      { entry =>
        val exchange = bindingVersion match {
          case "0.2.0" | "latest" => Amqp091ChannelExchange020(Annotations(entry.value))
          case _                  => Amqp091ChannelExchange010(Annotations(entry.value))
        }

        val exchangeMap = entry.value.as[YMap]

        exchangeMap.key("name", Amqp091ChannelExchangeModel.Name in exchange)
        exchangeMap.key("type", Amqp091ChannelExchangeModel.Type in exchange)
        exchangeMap.key("durable", Amqp091ChannelExchangeModel.Durable in exchange)
        exchangeMap.key("autoDelete", Amqp091ChannelExchangeModel.AutoDelete in exchange)

        bindingVersion match {
          case "0.2.0" | "latest" =>
            parseVHost(exchange, Amqp091ChannelExchange020Model.VHost, exchangeMap)
            ctx.closedShape(exchange, exchangeMap, "amqpExchangeChannelBinding020")
            binding.setWithoutId(Amqp091ChannelBinding020Model.Exchange, exchange, Annotations(entry))
          case _ =>
            ctx.closedShape(exchange, exchangeMap, "amqpExchangeChannelBinding010")
            binding.setWithoutId(Amqp091ChannelBinding010Model.Exchange, exchange, Annotations(entry))
        }
      }
    )
  }

  private def parseQueue(binding: Amqp091ChannelBinding, map: YMap, bindingVersion: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "queue",
      { entry =>
        val queue = bindingVersion match {
          case "0.2.0" | "latest" => Amqp091Queue020(Annotations(entry.value))
          case _                  => Amqp091Queue010(Annotations(entry.value))
        }
        val queueMap = entry.value.as[YMap]

        queueMap.key("name", Amqp091QueueModel.Name in queue)
        queueMap.key("durable", Amqp091QueueModel.Durable in queue)
        queueMap.key("exclusive", Amqp091QueueModel.Exclusive in queue)
        queueMap.key("autoDelete", Amqp091QueueModel.AutoDelete in queue)

        bindingVersion match {
          case "0.2.0" | "latest" =>
            parseVHost(queue, Amqp091Queue020Model.VHost, queueMap)
            ctx.closedShape(queue, queueMap, "amqpQueueChannelBinding020")
            binding.setWithoutId(Amqp091ChannelBinding020Model.Queue, queue, Annotations(entry))
          case _ =>
            ctx.closedShape(queue, queueMap, "amqpQueueChannelBinding010")
            binding.setWithoutId(Amqp091ChannelBinding010Model.Queue, queue, Annotations(entry))
        }
      }
    )
  }

  private def parseVHost(element: DomainElement, field: Field, map: YMap)(implicit ctx: AsyncWebApiContext) = {
    map.key("vhost") match {
      case Some(value) => Some(value).foreach(field in element)
      case None        => element.setWithoutId(field, AmfScalar("/"), Annotations.synthesized())
    }
  }
}
