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
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQChannelBindingParser extends BindingParser[IBMMQChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQChannelBinding = {
    val binding = IBMMQChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("destinationType") match {
      case Some(value) => Some(value).foreach(IBMMQChannelBindingModel.DestinationType in binding)
      case None        => setDefaultValue(binding, IBMMQChannelBindingModel.DestinationType, AmfScalar("topic"))
    }

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

        queueMap.key("isPartitioned") match {
          case Some(value) => Some(value).foreach(IBMMQChannelQueueModel.IsPartitioned in queue)
          case None        => setDefaultValue(binding, IBMMQChannelQueueModel.IsPartitioned, AmfScalar(false))
        }

        queueMap.key("exclusive") match {
          case Some(value) => Some(value).foreach(IBMMQChannelQueueModel.Exclusive in queue)
          case None        => setDefaultValue(binding, IBMMQChannelQueueModel.Exclusive, AmfScalar(false))
        }

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

        topicMap.key("durablePermitted") match {
          case Some(value) => Some(value).foreach(IBMMQChannelTopicModel.DurablePermitted in topic)
          case None        => setDefaultValue(binding, IBMMQChannelTopicModel.DurablePermitted, AmfScalar(true))
        }

        topicMap.key("lastMsgRetained") match {
          case Some(value) => Some(value).foreach(IBMMQChannelTopicModel.LastMsgRetained in topic)
          case None        => setDefaultValue(binding, IBMMQChannelTopicModel.LastMsgRetained, AmfScalar(false))
        }

        ctx.closedShape(topic, topicMap, "IBMMQChannelTopic")

        binding.setWithoutId(IBMMQChannelBindingModel.Topic, topic, Annotations(entry))
      }
    )
  }
}
