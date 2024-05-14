package amf.apicontract.internal.spec.async.emitters.bindings

import amf.apicontract.client.scala.model.domain.bindings.ChannelBinding
import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091ChannelBinding,
  Amqp091ChannelExchange,
  Amqp091Queue
}
import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQChannelBinding
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubChannelBinding,
  GooglePubSubMessageStoragePolicy,
  GooglePubSubSchemaSettings
}
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{
  IBMMQChannelBinding,
  IBMMQChannelQueue,
  IBMMQChannelTopic
}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  HasTopicConfiguration,
  KafkaChannelBinding,
  KafkaChannelBinding040,
  KafkaChannelBinding050,
  KafkaTopicConfiguration
}
import amf.apicontract.client.scala.model.domain.bindings.pulsar.{PulsarChannelBinding, PulsarChannelRetention}
import amf.apicontract.client.scala.model.domain.bindings.websockets.WebSocketsChannelBinding
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.spec.async.emitters.domain
import amf.apicontract.internal.spec.async.parser.bindings.Bindings._
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiChannelBindingsEmitter(binding: ChannelBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: ChannelBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091ChannelBinding      => Some(new Amqp091ChannelBindingEmitter(binding, ordering))
    case binding: WebSocketsChannelBinding   => Some(new WebSocketChannelBindingEmitter(binding, ordering))
    case binding: IBMMQChannelBinding        => Some(new IBMMQChannelBindingEmitter(binding, ordering))
    case binding: AnypointMQChannelBinding   => Some(new AnypointMQChannelBindingEmitter(binding, ordering))
    case binding: PulsarChannelBinding       => Some(new PulsarChannelBindingEmitter(binding, ordering))
    case binding: GooglePubSubChannelBinding => Some(new GooglePubSubChannelBindingEmitter(binding, ordering))
    case binding: KafkaChannelBinding        => Some(new KafkaChannelBindingEmitter(binding, ordering))
    case _                                   => None
  }

  override def position(): Position = pos(binding.annotations)
}

class WebSocketChannelBindingEmitter(binding: WebSocketsChannelBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(WebSockets),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(WebSocketsChannelBindingModel.Method).foreach(f => result += ValueEmitter("method", f))
        fs.entry(WebSocketsChannelBindingModel.Query)
          .foreach(f => result += domain.AsyncSchemaEmitter("query", f.element.asInstanceOf[Shape], ordering, Seq()))
        fs.entry(WebSocketsChannelBindingModel.Headers)
          .foreach(f => result += domain.AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class Amqp091ChannelBindingEmitter(binding: Amqp091ChannelBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(Amqp),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        fs.entry(Amqp091ChannelBindingModel.Is).foreach { f =>
          if (!f.element.annotations.contains(classOf[SynthesizedField])) result += ValueEmitter("is", f)
          result ++= emitExchangeAndQueueProperties
        }
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def emitExchangeAndQueueProperties: Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    Option(binding.exchange).foreach(exchange => result += new Amqp091ChannelExchangeEmitter(exchange, ordering))
    Option(binding.queue).foreach(queue => result += new Amqp091ChannelQueueEmitter(queue, ordering))
    result.toList
  }

  override def position(): Position = pos(binding.annotations)
}

class Amqp091ChannelExchangeEmitter(binding: Amqp091ChannelExchange, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("exchange"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(Amqp091ChannelExchangeModel.Name).foreach(f => result += ValueEmitter("name", f))
        fs.entry(Amqp091ChannelExchangeModel.Type).foreach(f => result += ValueEmitter("type", f))
        fs.entry(Amqp091ChannelExchangeModel.Durable).foreach(f => result += ValueEmitter("durable", f))
        fs.entry(Amqp091ChannelExchangeModel.AutoDelete).foreach(f => result += ValueEmitter("autoDelete", f))
        fs.entry(Amqp091ChannelExchange020Model.VHost).foreach { f =>
          if (!isSynthesized(f)) result += ValueEmitter("vhost", f)
        }

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def isSynthesized(f: FieldEntry): Boolean = f.value.annotations.contains(classOf[SynthesizedField])

  override def position(): Position = pos(binding.annotations)
}

class Amqp091ChannelQueueEmitter(binding: Amqp091Queue, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("queue"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(Amqp091QueueModel.Name).foreach(f => result += ValueEmitter("name", f))
        fs.entry(Amqp091QueueModel.Exclusive).foreach(f => result += ValueEmitter("exclusive", f))
        fs.entry(Amqp091QueueModel.Durable).foreach(f => result += ValueEmitter("durable", f))
        fs.entry(Amqp091QueueModel.AutoDelete).foreach(f => result += ValueEmitter("autoDelete", f))
        fs.entry(Amqp091Queue020Model.VHost).foreach { f =>
          if (!isSynthesized(f)) result += ValueEmitter("vhost", f)
        }
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def isSynthesized(f: FieldEntry): Boolean = f.value.annotations.contains(classOf[SynthesizedField])

  override def position(): Position = pos(binding.annotations)
}

class IBMMQChannelBindingEmitter(binding: IBMMQChannelBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(IBMMQ),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(IBMMQChannelBindingModel.DestinationType).foreach(f => result += ValueEmitter("destinationType", f))
        fs.entry(IBMMQChannelBindingModel.MaxMsgLength).foreach(f => result += ValueEmitter("maxMsgLength", f))

        result ++= emitQueueAndTopicProperties

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def emitQueueAndTopicProperties: Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    Option(binding.queue).foreach(queue => result += new IBMMQChannelQueueEmitter(queue, ordering))
    Option(binding.topic).foreach(topic => result += new IBMMQChannelTopicEmitter(topic, ordering))
    result.toList
  }

  override def position(): Position = pos(binding.annotations)
}

class IBMMQChannelQueueEmitter(binding: IBMMQChannelQueue, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("queue"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(IBMMQChannelQueueModel.ObjectName).foreach(f => result += ValueEmitter("objectName", f))
        fs.entry(IBMMQChannelQueueModel.IsPartitioned).foreach(f => result += ValueEmitter("isPartitioned", f))
        fs.entry(IBMMQChannelQueueModel.Exclusive).foreach(f => result += ValueEmitter("exclusive", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class IBMMQChannelTopicEmitter(binding: IBMMQChannelTopic, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("topic"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(IBMMQChannelTopicModel.String).foreach(f => result += ValueEmitter("string", f))
        fs.entry(IBMMQChannelTopicModel.ObjectName).foreach(f => result += ValueEmitter("objectName", f))
        fs.entry(IBMMQChannelTopicModel.DurablePermitted).foreach(f => result += ValueEmitter("durablePermitted", f))
        fs.entry(IBMMQChannelTopicModel.LastMsgRetained).foreach(f => result += ValueEmitter("lastMsgRetained", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class AnypointMQChannelBindingEmitter(binding: AnypointMQChannelBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(AnypointMQ),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(AnypointMQChannelBindingModel.Destination).foreach(f => result += ValueEmitter("destination", f))
        fs.entry(AnypointMQChannelBindingModel.DestinationType)
          .foreach(f => result += ValueEmitter("destinationType", f))

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class GooglePubSubChannelBindingEmitter(binding: GooglePubSubChannelBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(GooglePubSub),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(GooglePubSubChannelBindingModel.Labels)
          .foreach(f =>
            result += EntryPartEmitter(
              "labels",
              DataNodeEmitter(binding.labels, ordering)(spec.eh),
              position = pos(f.value.annotations)
            )
          )
        fs.entry(GooglePubSubChannelBindingModel.MessageRetentionDuration)
          .foreach(f => result += ValueEmitter("messageRetentionDuration", f))
        fs.entry(GooglePubSubChannelBinding010Model.Topic).foreach(f => result += ValueEmitter("topic", f))
        Option(binding.messageStoragePolicy).foreach(policy =>
          result += new GooglePubSubStoragePolicyEmitter(policy, ordering)
        )
        Option(binding.schemaSettings).foreach(settings =>
          result += new GooglePubSubSchemaSettingsEmitter(settings, ordering)
        )

        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)

      }
    )
  }
  override def position(): Position = pos(binding.annotations)
}

class GooglePubSubStoragePolicyEmitter(binding: GooglePubSubMessageStoragePolicy, ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("messageStoragePolicy"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(GooglePubSubMessageStoragePolicyModel.AllowedPersistenceRegions)
          .foreach(f => result += ArrayEmitter("allowedPersistenceRegions", f, ordering))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }
  override def position(): Position = pos(binding.annotations)
}
class GooglePubSubSchemaSettingsEmitter(binding: GooglePubSubSchemaSettings, ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("schemaSettings"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(GooglePubSubSchemaSettingsModel.Encoding).foreach(f => result += ValueEmitter("encoding", f))
        fs.entry(GooglePubSubSchemaSettingsModel.FirstRevisionId)
          .foreach(f => result += ValueEmitter("firstRevisionId", f))
        fs.entry(GooglePubSubSchemaSettingsModel.LastRevisionId)
          .foreach(f => result += ValueEmitter("lastRevisionId", f))
        fs.entry(GooglePubSubSchemaSettingsModel.Name).foreach(f => result += ValueEmitter("name", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }
  override def position(): Position = pos(binding.annotations)
}

class PulsarChannelBindingEmitter(binding: PulsarChannelBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Pulsar),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(PulsarChannelBindingModel.Namespace).foreach(f => result += ValueEmitter("namespace", f))
        fs.entry(PulsarChannelBindingModel.Persistence).foreach(f => result += ValueEmitter("persistence", f))
        fs.entry(PulsarChannelBindingModel.Compaction).foreach(f => result += ValueEmitter("compaction", f))
        fs.entry(PulsarChannelBindingModel.GeoReplication)
          .foreach(f => result += ArrayEmitter("geo-replication", f, ordering))
        Option(binding.retention).foreach(retention => result += new PulsarChannelRetentionEmitter(retention, ordering))
        fs.entry(PulsarChannelBindingModel.Ttl).foreach(f => result += ValueEmitter("ttl", f))
        fs.entry(PulsarChannelBindingModel.Deduplication).foreach(f => result += ValueEmitter("deduplication", f))

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class PulsarChannelRetentionEmitter(binding: PulsarChannelRetention, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("retention"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(PulsarChannelRetentionModel.Time).foreach(f => result += ValueEmitter("time", f))
        fs.entry(PulsarChannelRetentionModel.Size).foreach(f => result += ValueEmitter("size", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaChannelBindingEmitter(binding: KafkaChannelBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Kafka),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(KafkaChannelBindingModel.Topic).foreach(f => result += ValueEmitter("topic", f))
        fs.entry(KafkaChannelBindingModel.Partitions).foreach(f => result += ValueEmitter("partitions", f))
        fs.entry(KafkaChannelBindingModel.Replicas).foreach(f => result += ValueEmitter("replicas", f))

        binding match {
          case bindingWithTopic: HasTopicConfiguration =>
            Option(bindingWithTopic.topicConfiguration).foreach(topicConfiguration =>
              result += new KafkaTopicConfigurationEmitter(topicConfiguration, ordering)
            )
          case _ => // ignore
        }

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaTopicConfigurationEmitter(topicConfiguration: KafkaTopicConfiguration, ordering: SpecOrdering)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("topicConfiguration"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = topicConfiguration.fields

        fs.entry(KafkaTopicConfigurationModel.CleanupPolicy)
          .foreach(f => result += ArrayEmitter("cleanup.policy", f, ordering))
        fs.entry(KafkaTopicConfigurationModel.RetentionMs).foreach(f => result += ValueEmitter("retention.ms", f))
        fs.entry(KafkaTopicConfigurationModel.RetentionBytes).foreach(f => result += ValueEmitter("retention.bytes", f))
        fs.entry(KafkaTopicConfigurationModel.DeleteRetentionMs)
          .foreach(f => result += ValueEmitter("delete.retention.ms", f))
        fs.entry(KafkaTopicConfigurationModel.MaxMessageBytes)
          .foreach(f => result += ValueEmitter("max.message.bytes", f))
        fs.entry(KafkaTopicConfiguration050Model.ConfluentKeySchemaValidation)
          .foreach(f => result += ValueEmitter("confluent.key.schema.validation", f))
        fs.entry(KafkaTopicConfiguration050Model.ConfluentKeySubjectNameStrategy)
          .foreach(f => result += ValueEmitter("confluent.key.subject.name.strategy", f))
        fs.entry(KafkaTopicConfiguration050Model.ConfluentValueSchemaValidation)
          .foreach(f => result += ValueEmitter("confluent.value.schema.validation", f))
        fs.entry(KafkaTopicConfiguration050Model.ConfluentValueSubjectNameStrategy)
          .foreach(f => result += ValueEmitter("confluent.value.subject.name.strategy", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(topicConfiguration.annotations)
}
