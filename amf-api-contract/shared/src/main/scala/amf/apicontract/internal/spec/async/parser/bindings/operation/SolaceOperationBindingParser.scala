package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceOperationBinding, SolaceOperationDestination, SolaceOperationQueue, SolaceOperationTopic}
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceOperationBindingModel, SolaceOperationDestinationModel, SolaceOperationQueueModel, SolaceOperationTopicModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}
import amf.core.internal.parser.YMapOps

object SolaceOperationBindingParser extends BindingParser[SolaceOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): SolaceOperationBinding = {
    val binding = SolaceOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("destinations").foreach { entry =>
      val destinations = entry.value.as[Seq[YMap]].map(parseDestination)
      binding.setWithoutId(SolaceOperationBindingModel.Destinations, AmfArray(destinations, Annotations(entry.value)), Annotations(entry))
    }

    parseBindingVersion(binding, SolaceOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "SolaceOperationBinding")

    binding
  }

  private def parseDestination(map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): SolaceOperationDestination = {
    val destination = SolaceOperationDestination(Annotations(map))

    map.key("destinationType", SolaceOperationDestinationModel.DestinationType in destination)

    map.key("deliveryMode") match {
      case Some(value) => Some(value).foreach(SolaceOperationDestinationModel.DeliveryMode in destination)
      case None => setDefaultValue(destination, SolaceOperationDestinationModel.DeliveryMode, AmfScalar("persistent"))
    }

    parseQueue(destination, map)
    parseTopic(destination, map)

    destination
  }

  private def parseQueue(destination: SolaceOperationDestination, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "queue",
      { entry =>
        val queue    = SolaceOperationQueue(Annotations(entry.value))
        val queueMap = entry.value.as[YMap]

        queueMap.key("name", SolaceOperationQueueModel.Name in queue)

        queueMap.key("topicSubscriptions", SolaceOperationQueueModel.TopicSubscriptions in queue)

        queueMap.key("accessType", SolaceOperationQueueModel.AccessType in queue)

        queueMap.key("maxMsgSpoolSize", SolaceOperationQueueModel.MaxMsgSpoolSize in queue)

        queueMap.key("maxTtl", SolaceOperationQueueModel.MaxTtl in queue)

        ctx.closedShape(queue, queueMap, "SolaceOperationQueue")

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

        destination.setWithoutId(SolaceOperationDestinationModel.Topic, topic, Annotations(entry))
      }
    )
  }
}
