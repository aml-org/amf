package amf.plugins.document.webapi.parser.spec.async.emitters.bindings

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AsyncSchemaEmitter
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091MessageBindingModel,
  HttpMessageBindingModel,
  KafkaMessageBindingModel
}
import amf.plugins.domain.webapi.models.bindings.MessageBinding
import amf.plugins.domain.webapi.models.bindings.amqp.Amqp091MessageBinding
import amf.plugins.domain.webapi.models.bindings.http.HttpMessageBinding
import amf.plugins.domain.webapi.models.bindings.kafka.KafkaMessageBinding
import amf.plugins.domain.webapi.models.bindings.mqtt.MqttMessageBinding
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiMessageBindingsEmitter(binding: MessageBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: MessageBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091MessageBinding => Some(new Amqp091MessageEmitter(binding, ordering))
    case binding: HttpMessageBinding    => Some(new HttpMessageEmitter(binding, ordering))
    case binding: KafkaMessageBinding   => Some(new KafkaMessageEmitter(binding, ordering))
    case binding: MqttMessageBinding    => Some(new MqttMessageEmitter(binding, ordering))
    case _                              => None
  }

  override def position(): Position = pos(binding.annotations)
}

class HttpMessageEmitter(binding: HttpMessageBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("http"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(HttpMessageBindingModel.Headers)
          .foreach(f => result += AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaMessageEmitter(binding: KafkaMessageBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("kafka"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(KafkaMessageBindingModel.MessageKey)
          .foreach(f => result += ValueEmitter("key", f)) // TODO: should emit also enums ??
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttMessageEmitter(binding: MqttMessageBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("mqtt"),
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

class Amqp091MessageEmitter(binding: Amqp091MessageBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("amqp"),
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
