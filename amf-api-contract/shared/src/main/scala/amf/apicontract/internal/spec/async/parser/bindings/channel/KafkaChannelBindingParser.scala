package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  KafkaChannelBinding,
  KafkaChannelBinding030,
  KafkaChannelBinding040,
  KafkaTopicConfiguration
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  KafkaChannelBinding040Model,
  KafkaChannelBindingModel,
  KafkaTopicConfigurationModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaChannelBindingParser extends BindingParser[KafkaChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaChannelBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaChannelBinding", ctx.specSettings.spec)

    val binding: KafkaChannelBinding = bindingVersion match {
      case "0.4.0" | "latest" => KafkaChannelBinding040(Annotations(entry))
      case "0.3.0"            => KafkaChannelBinding030(Annotations(entry))
      case invalidVersion => // "0.1.0" | "0.2.0" don't parse because kafka channel binding wasn't defined until 0.3.0
        val defaultBinding = KafkaChannelBinding030(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Kafka Channel Binding")
        defaultBinding
    }

    map.key("topic", KafkaChannelBindingModel.Topic in binding)
    map.key("partitions", KafkaChannelBindingModel.Partitions in binding)
    map.key("replicas", KafkaChannelBindingModel.Replicas in binding)

    parseBindingVersion(binding, KafkaChannelBindingModel.BindingVersion, map)

    bindingVersion match {
      case "0.4.0" | "latest" =>
        parseTopicConfiguration(binding, map)
        ctx.closedShape(binding, map, "kafkaChannelBinding040")
      case _ =>
        ctx.closedShape(binding, map, "kafkaChannelBinding030")
    }

    binding
  }

  private def parseTopicConfiguration(binding: KafkaChannelBinding, map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "topicConfiguration",
      { entry =>
        val topicConf    = KafkaTopicConfiguration(Annotations(entry.value))
        val topicConfMap = entry.value.as[YMap]

        topicConfMap.key("cleanup.policy") match {
          case Some(value) => Some(value).foreach(KafkaTopicConfigurationModel.CleanupPolicy in topicConf)
          case None =>
            setDefaultValue(
              topicConf,
              KafkaTopicConfigurationModel.CleanupPolicy,
              AmfArray(Seq(AmfScalar("delete")))
            )
        }
        topicConfMap.key("retention.ms") match {
          case Some(value) => Some(value).foreach(KafkaTopicConfigurationModel.RetentionMs in topicConf)
          case None =>
            setDefaultValue(topicConf, KafkaTopicConfigurationModel.RetentionMs, AmfScalar(604800000)) // 7 days
        }
        topicConfMap.key("retention.bytes") match {
          case Some(value) => Some(value).foreach(KafkaTopicConfigurationModel.RetentionBytes in topicConf)
          case None        => setDefaultValue(topicConf, KafkaTopicConfigurationModel.RetentionBytes, AmfScalar(-1))
        }
        topicConfMap.key("delete.retention.ms") match {
          case Some(value) => Some(value).foreach(KafkaTopicConfigurationModel.DeleteRetentionMs in topicConf)
          case None =>
            setDefaultValue(topicConf, KafkaTopicConfigurationModel.DeleteRetentionMs, AmfScalar(86400000)) // 1  day
        }
        topicConfMap.key("max.message.bytes") match {
          case Some(value) => Some(value).foreach(KafkaTopicConfigurationModel.MaxMessageBytes in topicConf)
          case None => setDefaultValue(topicConf, KafkaTopicConfigurationModel.MaxMessageBytes, AmfScalar(1048588))
        }

        ctx.closedShape(topicConf, topicConfMap, "kafkaTopicConfiguration")

        binding.setWithoutId(KafkaChannelBinding040Model.TopicConfiguration, topicConf, Annotations(entry))
      }
    )
  }
}
