package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceOperationBinding, SolaceOperationBinding010, SolaceOperationBinding020, SolaceOperationBinding030, SolaceOperationBinding040, SolaceOperationDestination, SolaceOperationDestination010, SolaceOperationDestination020, SolaceOperationDestination030, SolaceOperationDestination040, SolaceOperationQueue, SolaceOperationQueue010, SolaceOperationQueue030, SolaceOperationTopic}
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceOperationBinding010Model, SolaceOperationBinding020Model, SolaceOperationBinding030Model, SolaceOperationBinding040Model, SolaceOperationBindingModel, SolaceOperationDestination020Model, SolaceOperationDestinationModel, SolaceOperationQueue030Model, SolaceOperationQueueModel, SolaceOperationTopicModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}
import amf.core.internal.parser.YMapOps

object SolaceOperationBindingParser extends BindingParser[SolaceOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): SolaceOperationBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "SolaceOperationBinding", ctx.specSettings.spec)
    val map            = entry.value.as[YMap]
    val binding: SolaceOperationBinding = bindingVersion match {
      case "0.4.0" | "latest" => SolaceOperationBinding040(Annotations(entry))
      case "0.3.0"  => SolaceOperationBinding030(Annotations(entry))
      case "0.2.0"  => SolaceOperationBinding020(Annotations(entry))
      case "0.1.0"  => SolaceOperationBinding010(Annotations(entry))
      case invalidVersion => //default to 0.3.0
        val defaultBinding = SolaceOperationBinding030(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Solace Operation Binding")
        defaultBinding
    }

    map.key("destinations").foreach { entry =>
      val destinations = entry.value.as[Seq[YMap]].map(yMap => parseDestination(yMap, bindingVersion))
      val field = bindingVersion match {
        case "0.4.0" | "latest" => SolaceOperationBinding040Model.Destinations
        case "0.3.0" => SolaceOperationBinding030Model.Destinations
        case "0.2.0" => SolaceOperationBinding020Model.Destinations
        case "0.1.0" => SolaceOperationBinding010Model.Destinations
        case _ => SolaceOperationBinding030Model.Destinations // default to 0.3.0
      }
      binding.setWithoutId(
        field,
        AmfArray(destinations, Annotations(entry.value)),
        Annotations(entry)
      )
    }

    map.key("timeToLive").foreach(SolaceOperationBinding040Model.TimeToLive in binding)
    map.key("priority").foreach(SolaceOperationBinding040Model.Priority in binding)
    map.key("dmqEligible").foreach(SolaceOperationBinding040Model.DmqEligible in binding)

    parseBindingVersion(binding, SolaceOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "SolaceOperationBinding010")

    binding
  }

  private def parseDestination(map: YMap, bindingVersion: String)(implicit
      ctx: AsyncWebApiContext
  ): SolaceOperationDestination = {
    val destination = bindingVersion match {
      case "0.4.0" | "latest" => SolaceOperationDestination040(Annotations(map))
      case "0.3.0"            => SolaceOperationDestination030(Annotations(map))
      case "0.2.0"            => SolaceOperationDestination020(Annotations(map))
      case "0.1.0"            => SolaceOperationDestination010(Annotations(map))
      case _                  => SolaceOperationDestination030(Annotations(map))
    }

    map.key("destinationType", SolaceOperationDestinationModel.DestinationType in destination)

    map.key("deliveryMode") match {
      case Some(value) => Some(value).foreach(SolaceOperationDestinationModel.DeliveryMode in destination)
      case None => setDefaultValue(destination, SolaceOperationDestinationModel.DeliveryMode, AmfScalar("persistent"))
    }

    parseQueue(destination, map, bindingVersion)
    bindingVersion match {
      case "0.1.0" => ctx.closedShape(destination, map, s"SolaceOperationDestination010")
      case "0.2.0" | "0.3.0" | "0.4.0" | "latest" =>
        parseTopic(destination, map)
        ctx.closedShape(destination, map, s"SolaceOperationDestination020")
      case _ =>
        parseTopic(destination, map)
        ctx.closedShape(destination, map, "SolaceOperationDestination020")
    }
    destination
  }

  private def parseQueue(destination: SolaceOperationDestination, map: YMap, bindingVersion: String)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "queue",
      { entry =>
        val queueMap = entry.value.as[YMap]
        val queue = bindingVersion match {
          case "0.2.0" | "0.1.0" => SolaceOperationQueue010(Annotations(entry.value))
          case "0.4.0" | "0.3.0" | "latest" =>
            val queue030 = SolaceOperationQueue030(Annotations(entry.value))
            queueMap.key("maxMsgSpoolSize", SolaceOperationQueue030Model.MaxMsgSpoolSize in queue030)
            queueMap.key("maxTtl", SolaceOperationQueue030Model.MaxTtl in queue030)
            queue030
          case _ =>
            SolaceOperationQueue030(Annotations(entry.value))
        }

        queueMap.key("name", SolaceOperationQueueModel.Name in queue)

        queueMap.key("topicSubscriptions", SolaceOperationQueueModel.TopicSubscriptions in queue)

        queueMap.key("accessType", SolaceOperationQueueModel.AccessType in queue)

        bindingVersion match {
          case  "0.2.0" | "0.1.0" => ctx.closedShape(queue, queueMap, "SolaceOperationQueue010")
          case "0.4.0" | "0.3.0" | "latest" =>
            ctx.closedShape(queue, queueMap, "SolaceOperationQueue030")
          case _ =>
            ctx.closedShape(queue, queueMap, "SolaceOperationQueue030")
        }

        destination.setWithoutId(SolaceOperationDestinationModel.Queue, queue, Annotations(entry))
      }
    )
  }

  private def parseTopic(destination: SolaceOperationDestination, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "topic",
      { entry =>
        val topic    = SolaceOperationTopic(Annotations(entry.value))
        val topicMap = entry.value.as[YMap]

        topicMap.key("topicSubscriptions", SolaceOperationTopicModel.TopicSubscriptions in topic)

        ctx.closedShape(topic, topicMap, "SolaceOperationTopic")

        destination.setWithoutId(SolaceOperationDestination020Model.Topic, topic, Annotations(entry))
      }
    )
  }
}
