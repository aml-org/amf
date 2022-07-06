package amf.apicontract.internal.spec.async.emitters.bindings

import org.mulesoft.common.client.lexical.Position
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.apicontract.internal.metamodel.domain.bindings.{MqttServerBindingModel, MqttServerLastWillModel}
import amf.apicontract.client.scala.model.domain.bindings.ServerBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiServerBindingsEmitter(binding: ServerBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: ServerBinding): Option[EntryEmitter] = binding match {
    case binding: MqttServerBinding => Some(new MqttServerBindingEmitter(binding, ordering))
    case _                          => None
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttServerBindingEmitter(binding: MqttServerBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("mqtt"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(MqttServerBindingModel.ClientId).foreach(f => result += ValueEmitter("clientId", f))
        fs.entry(MqttServerBindingModel.CleanSession).foreach(f => result += ValueEmitter("cleanSession", f))
        fs.entry(MqttServerBindingModel.KeepAlive).foreach(f => result += ValueEmitter("keepAlive", f))
        fs.entry(MqttServerBindingModel.LastWill)
          .foreach(_ => result += new LastWillEmitter(binding.lastWill, ordering))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)

  class LastWillEmitter(lastWill: MqttServerLastWill, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: YDocument.EntryBuilder): Unit = {
      b.entry(
        YNode("lastWill"),
        _.obj { emitter =>
          val fs     = lastWill.fields
          val result = ListBuffer[EntryEmitter]()

          fs.entry(MqttServerLastWillModel.Topic).foreach(f => result += ValueEmitter("topic", f))
          fs.entry(MqttServerLastWillModel.Qos).foreach(f => result += ValueEmitter("qos", f))
          fs.entry(MqttServerLastWillModel.Retain).foreach(f => result += ValueEmitter("retain", f))
          fs.entry(MqttServerLastWillModel.Message).foreach(f => result += ValueEmitter("message", f))

          traverse(ordering.sorted(result), emitter)
        }
      )
    }

    override def position(): Position = pos(lastWill.annotations)
  }
}
