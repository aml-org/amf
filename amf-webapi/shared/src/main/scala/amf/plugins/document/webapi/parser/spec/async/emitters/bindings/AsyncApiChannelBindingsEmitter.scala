package amf.plugins.document.webapi.parser.spec.async.emitters.bindings

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters
import amf.plugins.document.webapi.parser.spec.declaration.emitters.async
import amf.plugins.document.webapi.parser.spec.declaration.emitters.async.AsyncSchemaEmitter
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchangeModel,
  Amqp091QueueModel,
  WebSocketsChannelBindingModel
}
import amf.plugins.domain.webapi.models.bindings.ChannelBinding
import amf.plugins.domain.webapi.models.bindings.amqp.{Amqp091ChannelBinding, Amqp091ChannelExchange, Amqp091Queue}
import amf.plugins.domain.webapi.models.bindings.websockets.WebSocketsChannelBinding
import org.yaml.model.{YDocument, YNode}
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

class AsyncApiChannelBindingsEmitter(binding: ChannelBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: ChannelBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091ChannelBinding    => Some(new Amqp091ChannelBindingEmitter(binding, ordering))
    case binding: WebSocketsChannelBinding => Some(new WebSocketChannelBindingEmitter(binding, ordering))
    case _                                 => None
  }

  override def position(): Position = pos(binding.annotations)
}

class WebSocketChannelBindingEmitter(binding: WebSocketsChannelBinding, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("ws"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(WebSocketsChannelBindingModel.Method).foreach(f => result += ValueEmitter("method", f))
        fs.entry(WebSocketsChannelBindingModel.Query)
          .foreach(
            f => result += async.AsyncSchemaEmitter("query", f.element.asInstanceOf[Shape], ordering, Seq())
          )
        fs.entry(WebSocketsChannelBindingModel.Headers)
          .foreach(
            f => result += async.AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq())
          )
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
      YNode("amqp"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        fs.entry(Amqp091ChannelBindingModel.Is).foreach { f =>
          if (!f.element.annotations.contains(classOf[SynthesizedField])) result += ValueEmitter("is", f)
          result ++= emitSpecific(f.element.toString)
        }
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def emitSpecific(is: String): Seq[EntryEmitter] = is match {
    case "routingKey" => Seq(new Amqp091ChannelExchangeEmitter(binding.exchange, ordering))
    case "queue"      => Seq(new Amqp091ChannelQueueEmitter(binding.queue, ordering))
    case _            => Nil
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
        fs.entry(Amqp091ChannelExchangeModel.VHost).foreach(f => result += ValueEmitter("vhost", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

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
        fs.entry(Amqp091QueueModel.VHost).foreach { f =>
          if (!isSynthesized(f)) result += ValueEmitter("vhost", f)
        }
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def isSynthesized(f: FieldEntry): Boolean = f.value.annotations.contains(classOf[SynthesizedField])

  override def position(): Position = pos(binding.annotations)
}
