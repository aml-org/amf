package amf.apicontract.internal.spec.async.emitters.bindings

import amf.apicontract.client.scala.model.domain.bindings.MessageBinding
import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding
import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubMessageBinding,
  GooglePubSubSchemaDefinition
}
import amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091MessageBindingModel,
  AnypointMQMessageBindingModel,
  GooglePubSubMessageBindingModel,
  GooglePubSubSchemaDefinitionModel,
  HttpMessageBindingModel,
  IBMMQMessageBindingModel,
  KafkaMessageBindingModel
}
import amf.apicontract.internal.spec.async.emitters.domain
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.{
  Amqp,
  AnypointMQ,
  GooglePubSub,
  Http,
  IBMMQ,
  Kafka,
  Mqtt
}
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.apicontract.internal.spec.spec
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, ObjectNode, Shape}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.parser.domain.{Annotations, FieldEntry, Value}
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiMessageBindingsEmitter(binding: MessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: MessageBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091MessageBinding      => Some(new Amqp091MessageEmitter(binding, ordering))
    case binding: HttpMessageBinding         => Some(new HttpMessageEmitter(binding, ordering))
    case binding: KafkaMessageBinding        => Some(new KafkaMessageEmitter(binding, ordering))
    case binding: MqttMessageBinding         => Some(new MqttMessageEmitter(binding, ordering))
    case binding: IBMMQMessageBinding        => Some(new IBMMQMessageEmitter(binding, ordering))
    case binding: AnypointMQMessageBinding   => Some(new AnypointMQMessageEmitter(binding, ordering))
    case binding: GooglePubSubMessageBinding => Some(new GooglePubSubMessageBindingEmitter(binding, ordering))
    case _                                   => None
  }

  override def position(): Position = pos(binding.annotations)
}

class HttpMessageEmitter(binding: HttpMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(Http),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(HttpMessageBindingModel.Headers)
          .foreach(f => result += domain.AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaMessageEmitter(binding: KafkaMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(Kafka),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(KafkaMessageBindingModel.MessageKey)
          .foreach(f => result += domain.AsyncSchemaEmitter("key", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttMessageEmitter(binding: MqttMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(Mqtt),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class Amqp091MessageEmitter(binding: Amqp091MessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(Amqp),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        fs.entry(Amqp091MessageBindingModel.ContentEncoding).foreach(f => result += ValueEmitter("contentEncoding", f))
        fs.entry(Amqp091MessageBindingModel.MessageType).foreach(f => result += ValueEmitter("messageType", f))
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class IBMMQMessageEmitter(binding: IBMMQMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(IBMMQ),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(IBMMQMessageBindingModel.MessageType).foreach(f => result += ValueEmitter("type", f))
        fs.entry(IBMMQMessageBindingModel.Headers).foreach { f =>
          val valuesString = f.value.value.asInstanceOf[AmfArray].values.mkString(",")
          val field =
            FieldEntry(IBMMQMessageBindingModel.Headers, Value(AmfScalar(valuesString), Annotations.synthesized()))
          result += ValueEmitter("headers", field)
        }
        fs.entry(IBMMQMessageBindingModel.Description).foreach(f => result += ValueEmitter("description", f))
        fs.entry(IBMMQMessageBindingModel.Expiry).foreach(f => result += ValueEmitter("expiry", f))
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class AnypointMQMessageEmitter(binding: AnypointMQMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode(AnypointMQ),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(AnypointMQMessageBindingModel.Headers)
          .foreach(f => result += domain.AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class GooglePubSubMessageBindingEmitter(binding: GooglePubSubMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(GooglePubSub),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(GooglePubSubMessageBindingModel.Attributes)
          .foreach(f =>
            result += EntryPartEmitter(
              "attributes",
              DataNodeEmitter(binding.attributes, ordering)(spec.eh),
              position = pos(f.value.annotations)
            )
          )
        fs.entry(GooglePubSubMessageBindingModel.OrderingKey).foreach(f => result += ValueEmitter("orderingKey", f))
        Option(binding.schema).foreach(schema => result += new GooglePubSubSchemaDefinitionEmitter(schema, ordering))

        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)

      }
    )
  }
  override def position(): Position = pos(binding.annotations)
}
class GooglePubSubSchemaDefinitionEmitter(binding: GooglePubSubSchemaDefinition, ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("schema"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(GooglePubSubSchemaDefinitionModel.Name).foreach(f => result += ValueEmitter("name", f))
        fs.entry(GooglePubSubSchemaDefinitionModel.FieldType).foreach(f => result += ValueEmitter("type", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }
  override def position(): Position = pos(binding.annotations)
}
