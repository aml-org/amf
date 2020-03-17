package amf.plugins.document.webapi.parser.spec.async.emitters.bindings

import amf.core.emitter.BaseEmitters.{ArrayEmitter, ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters
import amf.plugins.document.webapi.parser.spec.declaration.emitters.AsyncSchemaEmitter
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091OperationBindingModel,
  HttpOperationBindingModel,
  KafkaOperationBindingModel,
  MqttOperationBindingModel
}
import amf.plugins.domain.webapi.models.bindings.OperationBinding
import amf.plugins.domain.webapi.models.bindings.amqp.Amqp091OperationBinding
import amf.plugins.domain.webapi.models.bindings.http.HttpOperationBinding
import amf.plugins.domain.webapi.models.bindings.kafka.KafkaOperationBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.MqttOperationBinding
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiOperationBindingsEmitter(binding: OperationBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: OperationBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091OperationBinding => Some(new Amqp091OperationBindingEmitter(binding, ordering))
    case binding: HttpOperationBinding    => Some(new HttpOperationBindingEmitter(binding, ordering))
    case binding: KafkaOperationBinding   => Some(new KafkaOperationBindingEmitter(binding, ordering))
    case binding: MqttOperationBinding    => Some(new MqttOperationBindingEmitter(binding, ordering))
    case _                                => None
  }

  override def position(): Position = pos(binding.annotations)
}

class HttpOperationBindingEmitter(binding: HttpOperationBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("http"),
      _.obj { emitter =>
        val fs     = binding.fields
        val result = ListBuffer[EntryEmitter]()

        fs.entry(HttpOperationBindingModel.OperationType).foreach(f => result += ValueEmitter("type", f))
        fs.entry(HttpOperationBindingModel.Method).foreach(f => result += ValueEmitter("method", f))
        fs.entry(HttpOperationBindingModel.Query)
          .foreach(f => result += emitters.AsyncSchemaEmitter("query", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaOperationBindingEmitter(binding: KafkaOperationBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("kafka"),
      _.obj { emitter =>
        val fs     = binding.fields
        val result = ListBuffer[EntryEmitter]()

        fs.entry(KafkaOperationBindingModel.GroupId).foreach(f => result += ValueEmitter("groupId", f))
        fs.entry(KafkaOperationBindingModel.ClientId).foreach(f => result += ValueEmitter("clientId", f))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttOperationBindingEmitter(binding: MqttOperationBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("mqtt"),
      _.obj { emitter =>
        val fs     = binding.fields
        val result = ListBuffer[EntryEmitter]()

        fs.entry(MqttOperationBindingModel.Qos).foreach(f => result += ValueEmitter("qos", f))
        fs.entry(MqttOperationBindingModel.Retain).foreach(f => result += ValueEmitter("retain", f))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class Amqp091OperationBindingEmitter(binding: Amqp091OperationBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("amqp"),
      _.obj { emitter =>
        val fs     = binding.fields
        val result = ListBuffer[EntryEmitter]()

        fs.entry(Amqp091OperationBindingModel.Expiration).foreach(f => result += ValueEmitter("expiration", f))
        fs.entry(Amqp091OperationBindingModel.UserId).foreach(f => result += ValueEmitter("userId", f))
        fs.entry(Amqp091OperationBindingModel.CC).foreach(f => result += ArrayEmitter("cc", f, ordering))
        fs.entry(Amqp091OperationBindingModel.Priority).foreach(f => result += ValueEmitter("priority", f))
        fs.entry(Amqp091OperationBindingModel.DeliveryMode).foreach(f => result += ValueEmitter("deliveryMode", f))
        fs.entry(Amqp091OperationBindingModel.Mandatory).foreach(f => result += ValueEmitter("mandatory", f))
        fs.entry(Amqp091OperationBindingModel.BCC).foreach(f => result += ArrayEmitter("bcc", f, ordering))
        fs.entry(Amqp091OperationBindingModel.ReplyTo).foreach(f => result += ValueEmitter("replyTo", f))
        fs.entry(Amqp091OperationBindingModel.Timestamp).foreach(f => result += ValueEmitter("timestamp", f))
        fs.entry(Amqp091OperationBindingModel.Ack).foreach(f => result += ValueEmitter("ack", f))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}
