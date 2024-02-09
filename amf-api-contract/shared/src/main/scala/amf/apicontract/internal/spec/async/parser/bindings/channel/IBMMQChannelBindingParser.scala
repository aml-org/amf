package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{
  IBMMQChannelBinding,
  IBMMQChannelQueue,
  IBMMQChannelTopic
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  IBMMQChannelBindingModel,
  IBMMQChannelQueueModel,
  IBMMQChannelTopicModel,
  WebSocketsChannelBindingModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQChannelBindingParser extends BindingParser[IBMMQChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQChannelBinding = {
    val binding = IBMMQChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("destinationType", IBMMQChannelBindingModel.DestinationType in binding)
    map.key("maxMsgLength", IBMMQChannelBindingModel.MaxMsgLength in binding)

    parseQueue(binding, map)
    parseTopic(binding, map)

    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "IBMMQChannelBinding")

    binding
  }

  private def parseQueue(binding: IBMMQChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "queue",
      { entry =>
        val queue    = IBMMQChannelQueue(Annotations(entry.value))
        val queueMap = entry.value.as[YMap]

        queueMap.key("objectName", IBMMQChannelQueueModel.ObjectName in queue)
        queueMap.key("isPartitioned", IBMMQChannelQueueModel.IsPartitioned in queue)
        queueMap.key("exclusive", IBMMQChannelQueueModel.Exclusive in queue)

        ctx.closedShape(queue, queueMap, "IBMMQChannelQueue")

        binding.setWithoutId(IBMMQChannelBindingModel.Queue, queue, Annotations(entry))
      }
    )
  }

  private def parseTopic(binding: IBMMQChannelBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "topic",
      { entry =>
        val topic    = IBMMQChannelTopic(Annotations(entry.value))
        val topicMap = entry.value.as[YMap]

        topicMap.key("string", IBMMQChannelTopicModel.String in topic)
        topicMap.key("objectName", IBMMQChannelTopicModel.ObjectName in topic)
        topicMap.key("durablePermitted", IBMMQChannelTopicModel.DurablePermitted in topic)
        topicMap.key("lastMsgRetained", IBMMQChannelTopicModel.LastMsgRetained in topic)

        ctx.closedShape(topic, topicMap, "IBMMQChannelTopic")

        binding.setWithoutId(IBMMQChannelBindingModel.Topic, topic, Annotations(entry))
      }
    )
  }
}
