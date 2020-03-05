package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorChannelBindings
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchangeModel,
  Amqp091QueueModel,
  WebSocketsChannelBindingModel
}
import amf.plugins.domain.webapi.models.bindings.{ChannelBinding, ChannelBindings}
import amf.plugins.domain.webapi.models.bindings.amqp.{Amqp091ChannelBinding, Amqp091ChannelExchange, Amqp091Queue}
import amf.plugins.domain.webapi.models.bindings.websockets.WebSocketsChannelBinding
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.plugins.document.webapi.parser.spec.domain.ConversionHelpers._

object AsyncChannelBindingsParser extends AsyncBindingsParser {
  override type Binding            = ChannelBinding
  override protected type Bindings = ChannelBindings

  def buildAndPopulate(entryOrMap: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): ChannelBindings = {
    val map: YMap       = entryOrMap
    val channelBindings = ChannelBindings(map)
    nameAndAdopt(channelBindings, entryOrMap.left.toOption, parent)
    parseBindings(channelBindings, map)
  }

  private def parseBindings(obj: ChannelBindings, map: YMap)(implicit ctx: AsyncWebApiContext): ChannelBindings = {
    val bindings: Seq[ChannelBinding] = parseElements(map, obj.id)
    obj.withBindings(bindings)
  }

  def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String, parent: String)(
      implicit ctx: AsyncWebApiContext): ChannelBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "channelBindings")
    ctx.declarations
      .findChannelBindings(label, SearchScope.Named)
      .map(channelBindings => nameAndAdopt(channelBindings.link(label), entryOrNode.left.toOption, parent))
      .getOrElse(remote(fullRef, entryOrNode, parent))
  }

  private def remote(fullRef: String, entryOrNode: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): ChannelBindings = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(bindingsNode) =>
        val external = AsyncChannelBindingsParser.parse(Right(bindingsNode), parent)
        nameAndAdopt(external.link(fullRef), entryOrNode.left.toOption, parent)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", entryOrNode)
        nameAndAdopt(new ErrorChannelBindings(fullRef, entryOrNode).link(fullRef), entryOrNode.left.toOption, parent)
    }
  }

  override protected def parseAmqp(entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): ChannelBinding = {
    val binding = Amqp091ChannelBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("is", Amqp091ChannelBindingModel.Is in binding)

    // Default channel type is 'routingKey'.
    if (binding.is.isNullOrEmpty) {
      binding.set(Amqp091ChannelBindingModel.Is, AmfScalar("routingKey"), Annotations(SynthesizedField()))
    }

    binding.is.value() match {
      case "queue"      => parseQueue(binding, map)
      case "routingKey" => parseExchange(binding, map)
      case _            => // will fail in raw validations
    }

    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)

    binding
  }

  private def parseExchange(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    ctx.closedShape(binding.id, map, "amqpIsExchangeChannelBinding")
    map.key(
      "exchange", { entry =>
        val exchange    = Amqp091ChannelExchange(Annotations(entry)).adopted(binding.id)
        val exchangeMap = entry.value.as[YMap]

        exchangeMap.key("name", Amqp091ChannelExchangeModel.Name in exchange) // TODO validate maxlength 255
        exchangeMap.key("type", Amqp091ChannelExchangeModel.Type in exchange)
        exchangeMap.key("durable", Amqp091ChannelExchangeModel.Durable in exchange)
        exchangeMap.key("autoDelete", Amqp091ChannelExchangeModel.AutoDelete in exchange)

        parseVHost(exchange, Amqp091ChannelExchangeModel.VHost, exchangeMap)

        ctx.closedShape(exchange.id, exchangeMap, "amqpExchangeChannelBinding")

        binding.set(Amqp091ChannelBindingModel.Exchange, exchange)
      }
    )
  }

  private def parseQueue(binding: Amqp091ChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    ctx.closedShape(binding.id, map, "amqpIsQueueChannelBinding")
    map.key(
      "queue", { entry =>
        val queue    = Amqp091Queue(Annotations(entry)).adopted(binding.id)
        val queueMap = entry.value.as[YMap]

        queueMap.key("name", Amqp091QueueModel.Name in queue) // TODO validate maxlength 255
        queueMap.key("durable", Amqp091QueueModel.Durable in queue)
        queueMap.key("exclusive", Amqp091QueueModel.Exclusive in queue)
        queueMap.key("autoDelete", Amqp091QueueModel.AutoDelete in queue)

        parseVHost(queue, Amqp091QueueModel.VHost, queueMap)

        ctx.closedShape(queue.id, queueMap, "amqpQueueChannelBinding")

        binding.set(Amqp091ChannelBindingModel.Queue, queue)
      }
    )
  }

  private def parseVHost(element: DomainElement, field: Field, map: YMap)(implicit ctx: AsyncWebApiContext) = {
    map.key("vhost", field in element)

    // Default vhost is '/'.
    if (!element.fields.exists(field)) {
      element.set(field, AmfScalar("/"), Annotations(SynthesizedField()))
    }
  }

  override protected def parseWs(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): ChannelBinding = {
    val binding = WebSocketsChannelBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("method", WebSocketsChannelBindingModel.Method in binding)
    map.key("query", entry => parseSchema(WebSocketsChannelBindingModel.Query, binding, entry, parent))     // TODO validate as object
    map.key("headers", entry => parseSchema(WebSocketsChannelBindingModel.Headers, binding, entry, parent)) // TODO validate as object
    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "wsChannelBinding")

    binding
  }
}
